package config;

import dao.IDao;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Properties;

import static org.junit.Assert.assertSame;

/**
 * Alias Spring : exposer un bean existant sous un second nom.
 *
 * <p>Un alias expose un bean existant sous un second nom, sans le dupliquer :
 * les deux noms designent la meme instance. Cela permet de nommer un bean de
 * facon explicite ("sourceParDefaut") tout en gardant le nom technique
 * d'origine ("dao").</p>
 */
public class AliasTest {

    @Test
    public void unAliasExposeLeBeanSousUnSecondNom() {
        AnnotationConfigApplicationContext context = contexte("dao");
        try {
            // "sourceParDefaut" est un alias du bean "dao" : meme instance.
            assertSame(context.getBean("dao"), context.getBean("sourceParDefaut"));
            // Le pointeur dynamique (@Primary) pointe vers la cible "dao" : meme instance.
            assertSame(context.getBean("dao"), context.getBean(IDao.class));
        } finally {
            context.close();
        }
    }

    /**
     * Construit un conteneur ou la cible est "dao", avec un alias enregistre
     * avant le demarrage.
     */
    private AnnotationConfigApplicationContext contexte(String cible) {
        StandardEnvironment environment = new StandardEnvironment();

        Properties surcharge = new Properties();
        surcharge.setProperty("dao.target", cible);
        environment.getPropertySources().addFirst(new PropertiesPropertySource("test", surcharge));

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setEnvironment(environment);
        context.register(AppConfig.class);
        ((BeanDefinitionRegistry) context).registerAlias("dao", "sourceParDefaut");
        context.refresh();
        return context;
    }
}
