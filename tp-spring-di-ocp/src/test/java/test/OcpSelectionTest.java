package test;

import config.AppConfig;
import metier.IMetier;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertEquals;

/**
 * Test de non-regression : le principe OCP est respecte.
 *
 * <p>Le code metier ({@code MetierImpl}) n'est jamais modifie ; c'est la
 * seule configuration ({@code dao.target}) qui determine la source utilisee.
 * Ce test echoue des que la configuration ne pointe plus vers
 * {@code DaoImpl}.</p>
 */
public class OcpSelectionTest {

    @Test
    public void testDaoImplParDefaut() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);
        try {
            IMetier metier = context.getBean(IMetier.class);

            // Si dao.target=dao, le resultat doit etre 200.0
            assertEquals(200.0, metier.calcul(), 0.001);
        } finally {
            context.close();
        }
    }
}
