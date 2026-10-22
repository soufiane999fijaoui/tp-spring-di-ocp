package config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Properties;

/**
 * Utilitaire de test : demarre un conteneur Spring avec une source de donnees
 * imposee, sans modifier une seule ligne de code de l'application.
 *
 * <p>La surcharge se fait via une propriete placede en tete des sources de
 * proprietes ({@code addFirst}) : elle l'emporte donc sur
 * {@code app.properties} ({@code @PropertySource}) et sur la valeur par
 * defaut.</p>
 */
public final class ConteneurDeTest {

    private ConteneurDeTest() {
    }

    /**
     * @param cible nom du bean DAO cible : dao | dao2 | daoFile | daoApi | daoProd.
     * @return un conteneur demarre, a fermer par l'appelant.
     */
    public static AnnotationConfigApplicationContext demarrer(String cible) {
        return demarrer(cible, new String[0]);
    }

    /**
     * @param cible  nom du bean DAO cible.
     * @param profils profils a activer AVANT le demarrage du conteneur
     *                (les conditions {@code @Profile} sont evaluees au refresh).
     * @return un conteneur demarre, a fermer par l'appelant.
     */
    public static AnnotationConfigApplicationContext demarrer(String cible, String... profils) {
        StandardEnvironment environment = new StandardEnvironment();

        Properties surcharge = new Properties();
        surcharge.setProperty("dao.target", cible);
        environment.getPropertySources().addFirst(new PropertiesPropertySource("test", surcharge));

        if (profils.length > 0) {
            environment.setActiveProfiles(profils);
        }

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setEnvironment(environment);
        context.register(AppConfig.class);
        context.refresh();
        return context;
    }
}
