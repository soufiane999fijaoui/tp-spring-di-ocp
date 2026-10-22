package config;

import dao.IDao;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Reproduit le probleme d'ambiguite du TP : plusieurs beans implementent
 * {@link IDao}, Spring ne sait pas lequel injecter.
 *
 * <p>Ce test sert de reference aux strategies de resolution :</p>
 * <ul>
 *   <li>{@code @Primary} : designer un bean par defaut ;</li>
 *   <li>{@code @Qualifier} : designer un bean par son nom (mais cela fige le
 *       code qui pose la dependance) ;</li>
 *   <li>bean de configuration : alias vers un bean ;</li>
 *   <li>proprietes externes : selection dynamique a la construction du
 *       contexte (voir {@code BasculeSourceTest}).</li>
 * </ul>
 *
 * <p>Note : le parametre du point d'injection ne doit porter le nom d'aucun
 * bean ("source" ci-dessous). Sinon Spring leve l'ambiguite en tominant sur le
 * <em>nom du parametre</em> qui correspond a un nom de bean.</p>
 */
public class AmbiguiteTest {

    /** Consommateur qui provoque l'injection litigieuse. */
    static class Consommateur {
        private final IDao dao;

        Consommateur(IDao dao) {
            this.dao = dao;
        }

        IDao getDao() {
            return dao;
        }
    }

    private static IDao source(double valeur) {
        return () -> valeur;
    }

    /** Quatre beans {@link IDao}, aucun bean par defaut : l'injection echoue. */
    @Configuration
    static class QuatreDaoSansPrimary {

        @Bean
        IDao dao() {
            return source(100d);
        }

        @Bean
        IDao dao2() {
            return source(150d);
        }

        @Bean
        IDao daoFile() {
            return source(180d);
        }

        @Bean
        IDao daoApi() {
            return source(220d);
        }

        @Bean
        Consommateur consommateur(IDao source) {
            return new Consommateur(source);
        }
    }

    /** Meme configuration, mais un bean est {@code @Primary} : plus d'ambiguite. */
    @Configuration
    static class QuatreDaoAvecPrimary {

        @Bean
        @Primary
        IDao dao() {
            return source(100d);
        }

        @Bean
        IDao dao2() {
            return source(150d);
        }

        @Bean
        IDao daoFile() {
            return source(180d);
        }

        @Bean
        IDao daoApi() {
            return source(220d);
        }

        @Bean
        Consommateur consommateur(IDao source) {
            return new Consommateur(source);
        }
    }

    @Test
    public void sansPrimarySpringNeSaitPasQuelBeanInjecter() {
        NoUniqueBeanDefinitionException ambiguite = null;
        AnnotationConfigApplicationContext context = null;
        try {
            context = new AnnotationConfigApplicationContext(QuatreDaoSansPrimary.class);
            context.getBean(Consommateur.class);
            fail("Une NoUniqueBeanDefinitionException etait attendue");
        } catch (NoUniqueBeanDefinitionException e) {
            ambiguite = e;
        } catch (BeanCreationException e) {
            if (!(e.getCause() instanceof NoUniqueBeanDefinitionException)) {
                throw e;
            }
            ambiguite = (NoUniqueBeanDefinitionException) e.getCause();
        } finally {
            if (context != null) {
                context.close();
            }
        }

        assertNotNull("Une NoUniqueBeanDefinitionException etait attendue", ambiguite);
        String message = ambiguite.getMessage();
        assertTrue(message, message.contains("expected single matching bean but found 4"));
        assertTrue(message, message.contains("dao"));
        assertTrue(message, message.contains("dao2"));
        assertTrue(message, message.contains("daoFile"));
        assertTrue(message, message.contains("daoApi"));
    }

    @Test
    public void primaryDesigneLeBeanParDefaut() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(QuatreDaoAvecPrimary.class);
        try {
            Consommateur consommateur = context.getBean(Consommateur.class);
            assertSame(context.getBean("dao", IDao.class), consommateur.getDao());
            assertEquals(100d, consommateur.getDao().getValue(), 0.0001);
        } finally {
            context.close();
        }
    }
}
