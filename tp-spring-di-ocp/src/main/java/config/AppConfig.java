package config;

import dao.IDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Configuration de l'application (beans de configuration).
 *
 * <p>{@code @ComponentScan} indique a Spring de scanner les paquets specifies
 * pour trouver les classes annotees ({@code @Component}, {@code @Service}) ;
 * {@code @Configuration} declare cette classe comme source de definitions de
 * beans.</p>
 *
 * <p>Sans ce scan, Spring ne trouverait ni les DAO ({@code @Component}) ni la
 * couche metier ({@code @Service}).</p>
 *
 * <p>{@code @PropertySource("classpath:app.properties")} fait charger par
 * Spring le fichier de proprietes externe : c'est lui qui alimente
 * {@code @Value("${dao.target}")} et donc le choix de la source.</p>
 *
 * <p>{@code @Import(DynamicDaoConfig.class)} active la strategie C (selection
 * par propriete externe). Pour utiliser la strategie B (alias {@code @Bean}),
 * remplacer cette importation par {@code @Import(DaoAliasConfig.class)} :
 * les deux strategies declarent un bean {@code @Primary}, elles ne peuvent
 * donc pas etre actives simultanement.</p>
 */
@Configuration
@ComponentScan(basePackages = {"dao", "metier"})
@PropertySource("classpath:app.properties")
@Import(DynamicDaoConfig.class)
public class AppConfig {

    /**
     * Active la resolution des {@code @Value("${...}")} a partir des
     * proprietes externes ({@code app.properties}).
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Bean de configuration illustrant la selection explicite d'un bean par
     * son nom : {@code @Qualifier("dao2")} designe la source secondaire,
     * quelle que soit la source active ({@code @Value} lit la propriete
     * externe {@code dao.target}).
     */
    @Bean
    public String sourceSecondaire(@Qualifier("dao2") IDao daoSecondaire,
                                   @Value("${dao.target:dao}") String cible) {
        return "bean 'dao2' -> " + daoSecondaire.getClass().getSimpleName()
                + " (dao.target=" + cible + ")";
    }
}
