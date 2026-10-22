# TP — Injection de dépendances Spring & Principe d'Ouverture/Fermeture (OCP)

> **Règle d'or du TP :** `MetierImpl` est une **classe figée**. On ne la modifie
> JAMAIS pour changer de source de données. Toute la logique de sélection est
> externalisée dans la configuration.

---

## 1. Objectifs d'apprentissage

Ce laboratoire a pour but de maîtriser les mécanismes d'injection de dépendances
de Spring pour respecter le Principe d'Ouverture/Fermeture (OCP). Vous
apprendrez à sélectionner dynamiquement des implémentations de DAO sans toucher
au code métier.

À la fin du TP, vous saurez :

- **Programmer contre une interface** (`IDao`) et non contre une classe concrète.
- Déclarer et instancier des beans avec `@Component` / `@Bean` (beans de configuration).
- Utiliser `@Primary` pour désigner le bean injecté par défaut quand plusieurs
  implémentations coexistent.
- Résoudre une dépendance par son **nom** (`@Qualifier("dao2")`).
- Piloter le choix de l'implémentation par une **propriété externe**
  (`app.properties` → `dao.target`) chargée par `@PropertySource`.
- Créer un **pointeur dynamique** (`@Bean @Primary`) qui délègue à la source
  choisie par la propriété.
- Tracer l'implémentation réellement injectée (`@PostConstruct`).
- Démontre par un test automatisé que la couche métier ne change jamais.

## 2. Prérequis techniques

- JDK 11 ou supérieur (testé sur JDK 21)
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse ou VS Code)
- Connaissances de base en Java et en Spring Framework

## 3. Le problème initial

Imaginez une application où la classe `MetierImpl` dépend directement d'une
implémentation concrète `DaoImpl` :

```java
public class MetierImpl {
    private DaoImpl dao = new DaoImpl();   // couplage fort : violation de OCP
}
```

Pour passer de la base de données au fichier, il faudrait **modifier le code
métier**. C'est interdit par l'OCP : une classe doit être **ouverte à
l'extension** (nouvelles implémentations) mais **fermée à la modification**.

**La solution Spring :** la classe métier dépend de l'interface `IDao`, et le
conteneur Spring décide quelle implémentation injecter (Inversion de Contrôle).

---

## 4. Structure du projet

```
tp-spring-di-ocp/
├── pom.xml                                   spring-context 5.3.30 + javax.annotation-api
├── src/main/java/
│   ├── Main.java                             démarrage du conteneur + affichage
│   ├── dao/
│   │   ├── IDao.java                         l'abstraction (contrat)
│   │   ├── DaoImpl.java                      bean "dao"     → base de données (prod)
│   │   ├── DaoImpl2.java                     bean "dao2"    → base de données (dev)
│   │   ├── DaoFile.java                      bean "daoFile" → fichier local
│   │   └── DaoApi.java                       bean "daoApi"  → API REST
│   ├── metier/
│   │   ├── IMetier.java                      contrat de la couche métier
│   │   └── MetierImpl.java                   ⚠ CLASSE FIGÉE (jamais modifiée)
│   └── config/
│       ├── AppConfig.java                    beans de configuration (@PropertySource)
│       ├── DynamicDaoConfig.java             ⭐ stratégie C : pointeur dynamique
│       └── DaoAliasConfig.java               stratégie B : alias via @Bean
├── src/main/resources/
│   └── app.properties                        ⭐ la propriété externe dao.target
└── src/test/java/
    ├── config/BasculeSourceTest.java         la bascule entre les 4 sources
    ├── config/AmbiguiteTest.java             l'erreur NoUniqueBeanDefinitionException
    ├── config/AliasTest.java                 le bean d'alias (2e nom pour un bean)
    ├── config/ConteneurDeTest.java           utilitaire de démarrage de conteneur
    ├── dao/DaoImplTest.java                  test des 4 implémentations
    ├── metier/MetierImplTest.java            test unitaire (sans Spring)
    ├── metier/OcpTest.java                   preuve par réflexion que OCP est respecté
    └── test/OcpSelectionTest.java            test de non-régression (dao.target=dao → 200.0)
```

---

## 5. Étapes guidées

### Étape 1 — L'abstraction `IDao`

```java
package dao;

public interface IDao {
    /**
     * Récupère une valeur numérique depuis la source de données.
     * @return la valeur récupérée
     */
    double getValue();
}
```

**Pourquoi une interface ?** Programmer contre une interface plutôt que contre
une classe concrète permet à Spring d'injecter n'importe quelle classe
implémentant `IDao`. C'est la clé de la flexibilité et du respect de l'OCP.

### Étape 2 — Les implémentations et leurs noms de beans

Chaque source est un bean `@Component` **avec un nom explicite** :

| Classe       | Nom du bean | Source simulée              | Valeur |
|--------------|-------------|-----------------------------|--------|
| `DaoImpl`    | `dao`       | base de données (prod)      | 100.0  |
| `DaoImpl2`   | `dao2`      | base de données (dev)       | 150.0  |
| `DaoFile`    | `daoFile`   | fichier local               | 180.0  |
| `DaoApi`     | `daoApi`    | API REST externe            | 220.0  |

Les noms (`dao`, `dao2`, `daoFile`, `daoApi`) sont **cruciaux** : ils servent à la
sélection par nom via `@Qualifier("dao2")` **et** à la sélection par propriété
(`dao.target=daoFile`).

### Étape 3 — La classe métier (figée) et sa trace de démarrage

```java
@Service
public class MetierImpl implements IMetier {
    private final IDao dao;

    public MetierImpl(IDao dao) {   // injection par constructeur
        this.dao = dao;
    }

    @PostConstruct
    public void init() {
        System.out.println("[TRACE] Implementation DAO injectee : "
                + dao.getClass().getSimpleName());
    }

    @Override
    public double calcul() {
        double valeur = dao.getValue();
        // Logique metier simple : double la valeur
        return valeur * 2.0;
    }
}
```

L'injection de constructeur est préférée car elle garantit que la dépendance est
toujours présente et rend la classe immuable. Aucun `new DaoImpl()` : la classe
ne connaît que l'abstraction.

`@PostConstruct` est appelée par Spring juste après l'injection : elle confirme
l'implémentation réellement injectée (`[TRACE] Implementation DAO injectee :
DaoApi`). *Note : `@PostConstruct` (JSR-250) n'est plus fourni par le JDK depuis
Java 11, la dépendance `javax.annotation:javax.annotation-api` est donc
déclarée dans le `pom.xml`.*

### Étape 4 — `@Primary` : définir un choix par défaut

Quand plusieurs beans `IDao` sont dans le conteneur, une injection par type
échoue :

```
org.springframework.beans.factory.NoUniqueBeanDefinitionException:
No qualifying bean of type 'dao.IDao' available:
expected single matching bean but found 4: dao,dao2,daoFile,daoApi
```

Spring trouve 4 beans qui implémentent `IDao` et ne sait pas lequel choisir
pour l'injection dans `MetierImpl`. Quatre stratégies permettent de résoudre
cette ambiguïté :

| Stratégie | Principe | Implémentation dans ce TP |
|---|---|---|
| `@Primary` | définir un bean **par défaut** | `dao/DaoImpl.java` → `@Primary` |
| `@Qualifier` | spécifier le **nom** du bean | `config/AppConfig.java` → `sourceSecondaire(@Qualifier("dao2") IDao)` |
| Bean de configuration (alias) | exposer un bean sous un **second nom** | `config/DaoAliasConfig.java` |
| Propriétés externes | **sélection dynamique** via configuration | `app.properties` → `dao.target` + `config/DynamicDaoConfig.java` (étapes 5-6) |

> `@Qualifier` et l'alias demandent de nommer le bean **dans le code qui pose
> la dépendance** : cela fige le choix. Les propriétés externes seules
> permettent de changer de source sans toucher au code métier.

`@Primary` indique à Spring de préférer ce bean lorsqu'il y a plusieurs
candidats : c'est l'implémentation **par défaut**.

```java
@Primary
@Component("dao")
public class DaoImpl implements IDao {
    @Override
    public double getValue() {
        System.out.println("[DAO] Lecture depuis la base de donnees (prod)");
        return 100.0;
    }
}
```

Une fois `@Primary` posé, Spring injecte `DaoImpl` dans `MetierImpl` et le
résultat vaut `200.0` (100.0 × 2).

> **Limite : `@Primary` est statique.** Pour changer le bean par défaut, il
> faudrait modifier le code et recompiler — c'est-à-dire modifier le code
> métier, ce que l'OCP interdit. L'étape 6 lève cette limite en pilotant le
> choix par une **propriété externe**.

> **À faire :** retirez `@Primary` de `DaoImpl`, relancez l'application et
> observez l'exception. Remettez-le : l'application repart. Le code métier n'a
> pas bougé d'un iota. Le test `config/AmbiguiteTest.java` reproduit les deux
> situations de façon automatisée.

### Étape 5 — Stratégie B : un alias via un bean de configuration

```java
@Configuration
public class DaoAliasConfig {

    @Bean
    @Primary
    public IDao daoAlias() {
        return new DaoApi();   // <- une seule ligne à changer
    }
}
```

Le bean `daoAlias` **pointe** vers l'implémentation souhaitée : changer de
source consiste à changer cette seule ligne, sans toucher aux DAO ni au métier.

> **Attention :** `daoAlias` et `dynamicDao` (étape 6) sont tous deux `@Primary` :
> ils ne peuvent pas être actifs en même temps. Pour utiliser la stratégie B,
> remplacez `@Import(DynamicDaoConfig.class)` par `@Import(DaoAliasConfig.class)`
> dans `AppConfig`.

### Étape 6 — Stratégie C : la sélection par propriétés externes

**a) Le fichier de propriétés** — `src/main/resources/app.properties` :

```properties
# La valeur désigne le NOM du bean DAO à utiliser
dao.target=dao        # dao | dao2 | daoFile | daoApi
```

**b) Son chargement par Spring** — `config/AppConfig.java` :

```java
@Configuration
@ComponentScan(basePackages = {"dao", "metier"})
@PropertySource("classpath:app.properties")
@Import(DynamicDaoConfig.class)
public class AppConfig { ... }
```

`@PropertySource` ajoute le fichier aux *sources de propriétés* du
`Environment` ; `@Value("${dao.target}")` peut alors le lire. (La priorité est :
ligne de commande > propriété système > `app.properties`.)

**c) Le pointeur dynamique** — `config/DynamicDaoConfig.java` :

```java
@Configuration
public class DynamicDaoConfig {

    private final ApplicationContext applicationContext;
    private final String daoTarget;

    public DynamicDaoConfig(ApplicationContext applicationContext,
                            @Value("${dao.target:dao}") String daoTarget) {
        this.applicationContext = applicationContext;
        this.daoTarget = daoTarget;
    }

    @Bean
    @Primary
    public IDao dynamicDao() {
        switch (daoTarget) {
            case "dao":     return applicationContext.getBean("dao", IDao.class);
            case "dao2":    return applicationContext.getBean("dao2", IDao.class);
            case "daoFile": return applicationContext.getBean("daoFile", IDao.class);
            case "daoApi":  return applicationContext.getBean("daoApi", IDao.class);
            default: throw new IllegalArgumentException("Cible DAO inconnue: " + daoTarget);
        }
    }
}
```

Le bean `dynamicDao` est le **seul** `@Primary` de l'application : c'est donc
lui qui est injecté dans `MetierImpl`, et il délègue à l'implémentation
choisie par la propriété. On récupère l'instance **gérée par le conteneur**
(et non une nouvelle instance) pour qu'il n'existe qu'un seul objet par source.

Conséquence : `dao.target=daoApi` → le calcul vaut `440.0` (220.0 × 2), sans
modifier une ligne de Java et sans recompiler.

### Étape 7 — Les beans de configuration (`AppConfig`)

```java
@Configuration
@ComponentScan(basePackages = {"dao", "metier"})
@PropertySource("classpath:app.properties")
@Import(DynamicDaoConfig.class)
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() { ... }

    @Bean
    public String sourceSecondaire(@Qualifier("dao2") IDao daoSecondaire,
                                   @Value("${dao.target:dao}") String cible) { ... }
}
```

`@ComponentScan` indique à Spring de scanner les paquets spécifiés pour trouver
les classes annotées ; `@Configuration` déclare cette classe comme source de
définitions de beans. Sans ce scan, Spring ne trouverait ni les DAO
(`@Component`) ni la couche métier (`@Service`).

Le composant `MetierImpl` est découvert par le scan ; Spring lui injecte le bean
`IDao` marqué `@Primary`, c'est-à-dire `dynamicDao`. `AppConfig` ne contient
**aucune** décision de sélection de source : c'est `app.properties` qui la
pilote.

### Étape 8 — La preuve par les tests

- `BasculeSourceTest` démarre le conteneur 4 fois (une par source) et vérifie que
  le bean injecté change **tandis que la classe métier reste identique** ;
  il vérifie aussi qu'une cible inconnue est rejetée.
- `AmbiguiteTest` reproduit l'`NoUniqueBeanDefinitionException` et montre que
  `@Primary` la supprime.
- `OcpTest` vérifie par réflexion que `MetierImpl` ne possède qu'un constructeur
  et qu'aucun de ses attributs n'est typé par une implémentation concrète.
- `test/OcpSelectionTest` est le test de non-régression : avec
  `dao.target=dao`, `metier.calcul()` doit valoir `200.0`.

---

## 6. Exécution

```bash
# Compiler et tester (19 tests)
mvn -B test

# Lancer avec la source par défaut (app.properties → dao.target=dao)
mvn -q compile exec:java -Dexec.mainClass=Main

# Changer de source SANS toucher au code
mvn -q compile exec:java -Dexec.mainClass=Main -Dexec.args="daoApi"
mvn -q compile exec:java -Dexec.mainClass=Main -Dexec.args="daoFile"
mvn -q compile exec:java -Dexec.mainClass=Main -Dexec.args="dao2"

# Idem avec une propriété système
mvn -q compile exec:java -Dexec.mainClass=Main -Ddao.target=daoApi
```

Sortie attendue (mode `daoApi`) :

```
============================================================
  Source de donnees active (propriete dao.target) : daoApi
============================================================
Implementations IDao presentes dans le conteneur :
   - daoApi  ->  DaoApi
   - daoFile  ->  DaoFile
   - dao  ->  DaoImpl
   - dao2  ->  DaoImpl2
   - dynamicDao  ->  DaoApi

Bean injecte dans la couche metier (pointeur dynamique) : DaoApi
[DAO] Appel a l'API REST externe
Resultat final : 440.0

Selection explicite par nom (@Qualifier) : bean 'dao2' -> DaoImpl2 (dao.target=daoApi)
```

> **Variante** : modifiez `src/main/resources/app.properties`
> (`dao.target=daoFile`) et relancez : le résultat devient `360.0`.

---

## 7. Questions / travail à rendre

1. Expliquez avec le code de `MetierImpl` pourquoi le projet respecte l'OCP.
2. Que se passe-t-il si l'on supprime `@Primary` de `dynamicDao` ? Justifiez avec
   le message d'erreur de Spring.
3. Pourquoi `dao2` n'est-il pas annoté `@Primary` ? Que se passerait-il si on le
   faisait ?
4. Comment ajouter une 5ᵉ source (par exemple `DaoRedis`) **sans modifier**
   `MetierImpl` ni `AppConfig` ? Listez les étapes exactes.
5. Quelle est la différence entre `@Primary` et `@Qualifier` ? Donnez un exemple
   d'utilisation de chacun dans ce projet.
6. Quel est le rôle de `@PropertySource("classpath:app.properties")` ? Que se
   passe-t-il si on le retire, et comment Spring résout-il alors
   `@Value("${dao.target}")` ?
7. Pourquoi le pointeur `dynamicDao` récupère-t-il l'instance via
   `applicationContext.getBean(...)` plutôt que `new DaoImpl()` ?
8. `MetierImplTest` ne démarre aucun conteneur Spring. Quel avantage cela
   apporte-t-il sur un projet réel ?

## 8. Ce que vous devez être capable de faire

- [ ] Injecter une dépendance par constructeur et l'instancier manuellement dans un test.
- [ ] Expliquer `NoUniqueBeanDefinitionException` et le résoudre avec `@Primary`.
- [ ] Résoudre un bean par son nom avec `@Qualifier`.
- [ ] Basculer d'une implémentation à une autre via une propriété externe.
- [ ] Écrire un pointeur dynamique (`@Bean @Primary`) piloté par `@Value`.
- [ ] Ajouter une nouvelle source de données en respectant l'OCP.
