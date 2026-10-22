import config.AppConfig;
import dao.IDao;
import metier.IMetier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Properties;

/**
 * Point d'entree de l'application.
 *
 * <p>La source de donnees est choisie par une PROPRIETE EXTERNE
 * ({@code dao.target} dans {@code app.properties}), jamais par une
 * modification du code Java : {@code MetierImpl} reste identique quelle que
 * soit la source active.</p>
 *
 * <p>Le fichier {@code app.properties} est charge par Spring lui-meme grace a
 * {@code @PropertySource("classpath:app.properties")} sur {@code AppConfig}.
 * On peut surcharger la cible sans toucher au fichier :</p>
 *
 * <pre>
 *   java -jar tp.jar daoApi        (argument de la ligne de commande)
 *   java -Ddao.target=daoApi ...    (propriete systeme)
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        // 1. Environnement Spring + surcharge eventuelle de la cible.
        //    La priorite est : ligne de commande > propriete systeme > app.properties.
        StandardEnvironment environment = new StandardEnvironment();
        if (args.length > 0) {
            Properties surcharge = new Properties();
            surcharge.setProperty("dao.target", args[0]);
            environment.getPropertySources()
                    .addFirst(new PropertiesPropertySource("ligne de commande", surcharge));
        }

        // 2. Cree le conteneur Spring (AppConfig charge app.properties)
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setEnvironment(environment);
        context.register(AppConfig.class);
        context.refresh();

        try {
            // 3. Recupere le bean metier (et le bean DAO qui lui a ete injecte)
            IMetier metier = context.getBean(IMetier.class);
            IDao daoActif = context.getBean(IDao.class);
            String cible = environment.getProperty("dao.target", "dao");

            System.out.println("============================================================");
            System.out.println("  Source de donnees active (propriete dao.target) : " + cible);
            String[] profils = environment.getActiveProfiles();
            System.out.println("  Profils actifs : "
                    + (profils.length == 0 ? "(aucun)" : String.join(", ", profils)));
            System.out.println("============================================================");
            System.out.println("Implementations IDao presentes dans le conteneur :");
            for (String nom : context.getBeanNamesForType(IDao.class)) {
                IDao dao = context.getBean(nom, IDao.class);
                System.out.println("   - " + nom + "  ->  " + dao.getClass().getSimpleName());
            }
            System.out.println();

            // 4. Execute le calcul
            double resultat = metier.calcul();
            System.out.println("Bean injecte dans la couche metier (pointeur dynamique) : "
                    + daoActif.getClass().getSimpleName());
            System.out.println("Resultat final : " + resultat);
            System.out.println();
            System.out.println("Selection explicite par nom (@Qualifier) : "
                    + context.getBean("sourceSecondaire", String.class));
        } finally {
            // 5. Ferme le conteneur
            context.close();
        }
    }
}
