package config;

import dao.DaoApi;
import dao.DaoFile;
import dao.DaoImpl;
import dao.DaoImpl2;
import dao.IDao;
import metier.IMetier;
import metier.MetierImpl;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test central du TP : le meme code et les memes tests, quatre sources de
 * donnees differentes. {@code MetierImpl} n'est jamais modifie.
 *
 * <p>Strategie C : seule la propriete externe {@code dao.target} change.</p>
 */
public class BasculeSourceTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void arreterConteneur() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void cibleDaoInjecteLeDaoProd() {
        demarrer("dao");
        assertTrue(context.getBean(IDao.class) instanceof DaoImpl);
        assertEquals(200.0, context.getBean(IMetier.class).calcul(), 0.0001);
    }

    @Test
    public void cibleDao2InjecteLaSourceSecondaire() {
        demarrer("dao2");
        assertTrue(context.getBean(IDao.class) instanceof DaoImpl2);
        assertEquals(300.0, context.getBean(IMetier.class).calcul(), 0.0001);
    }

    @Test
    public void cibleDaoFileInjecteLeDaoFichier() {
        demarrer("daoFile");
        assertTrue(context.getBean(IDao.class) instanceof DaoFile);
        assertEquals(360.0, context.getBean(IMetier.class).calcul(), 0.0001);
    }

    @Test
    public void cibleDaoApiInjecteLeDaoApi() {
        demarrer("daoApi");
        assertTrue(context.getBean(IDao.class) instanceof DaoApi);
        assertEquals(440.0, context.getBean(IMetier.class).calcul(), 0.0001);
    }

    /**
     * La couche metier est identique dans les quatre modes : seule l'implementation
     * injectee change. C'est la demonstration du Principe d'Ouverture/Fermeture.
     */
    @Test
    public void laCoucheMetierNeChangeJamais() {
        Set<Class<?>> implementations = new LinkedHashSet<>();
        for (String cible : Arrays.asList("dao", "dao2", "daoFile", "daoApi")) {
            demarrer(cible);
            IMetier metier = context.getBean(IMetier.class);
            assertEquals(MetierImpl.class, metier.getClass());
            implementations.add(context.getBean(IDao.class).getClass());
            arreterConteneur();
            context = null;
        }
        assertEquals(4, implementations.size());
    }

    /**
     * Le bean {@code dynamicDao} est le seul {@code @Primary} : il est donc le
     * bean injecte par defaut, et il pointe sur l'implementation choisie par
     * la propriete externe.
     */
    @Test
    public void lePointeurDynamiqueEstLeBeanParDefaut() {
        demarrer("daoApi");
        String[] noms = context.getBeanNamesForType(IDao.class);
        assertEquals(5, noms.length);
        assertNotNull(context.getBean("dao"));
        assertNotNull(context.getBean("dao2"));
        assertNotNull(context.getBean("daoFile"));
        assertNotNull(context.getBean("daoApi"));
        assertNotNull(context.getBean("dynamicDao"));
        // Le pointeur renvoie l'instance geree par le conteneur (une seule par source).
        assertTrue(context.getBean(IDao.class) == context.getBean("daoApi"));
    }

    /**
     * Une cible inconnue est rejetee explicitement, avec un message lisible.
     * (Spring enveloppe l'erreur de creation du bean : on va chercher la
     * cause racine pour verifier le message du {@code switch}).
     */
    @Test
    public void uneCibleInconnueEstRejetee() {
        try {
            ConteneurDeTest.demarrer("daoInconnu");
            fail("Une IllegalArgumentException etait attendue");
        } catch (RuntimeException e) {
            Throwable racine = e;
            while (racine.getCause() != null) {
                racine = racine.getCause();
            }
            assertTrue(racine instanceof IllegalArgumentException);
            assertTrue(racine.getMessage(), racine.getMessage().contains("Cible DAO inconnue"));
        }
    }

    private void demarrer(String cible) {
        arreterConteneur();
        context = ConteneurDeTest.demarrer(cible);
    }
}
