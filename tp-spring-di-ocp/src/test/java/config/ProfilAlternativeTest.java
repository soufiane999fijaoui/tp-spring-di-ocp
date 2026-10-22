package config;

import dao.DaoImplProd;
import dao.IDao;
import metier.IMetier;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Alternative : le mecanisme natif des profils Spring.
 *
 * <p>Un bean annote {@code @Profile("prod")} n'est enregistre que si le profil
 * {@code prod} est actif au moment du demarrage du conteneur.</p>
 */
public class ProfilAlternativeTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void arreterConteneur() {
        if (context != null) {
            context.close();
        }
    }

    /**
     * Profil "prod" actif : le bean {@code daoProd} existe et c'est lui qui est
     * injecte dans la couche metier.
     */
    @Test
    public void leProfilProdActiveLaSourceProd() {
        context = ConteneurDeTest.demarrer("daoProd", "prod");
        assertTrue(context.getBean(IDao.class) instanceof DaoImplProd);
        assertEquals(200.0, context.getBean(IMetier.class).calcul(), 0.0001);
    }

    /**
     * Sans le profil, le bean n'existe pas du tout dans le conteneur.
     */
    @Test
    public void sansProfilLeBeanNExistePas() {
        try {
            ConteneurDeTest.demarrer("daoProd");
            fail("Une NoSuchBeanDefinitionException etait attendue");
        } catch (RuntimeException e) {
            Throwable racine = e;
            while (racine.getCause() != null) {
                racine = racine.getCause();
            }
            assertTrue(racine instanceof NoSuchBeanDefinitionException);
        }
    }
}
