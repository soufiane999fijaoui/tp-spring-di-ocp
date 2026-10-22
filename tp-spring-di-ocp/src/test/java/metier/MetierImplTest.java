package metier;

import dao.IDao;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test unitaire de la couche metier : aucun conteneur Spring n'est demarre.
 * Possible grace a l'injection par constructeur : la dependance est fournie
 * manuellement, comme le ferait Spring.
 */
public class MetierImplTest {

    /** Faux DAO : remplace la base, le fichier ou l'API. */
    private static class DaoFictif implements IDao {
        @Override
        public double getValue() {
            return 100.0;
        }
    }

    @Test
    public void calculDoubleLaValeurDeLaSource() {
        MetierImpl metier = new MetierImpl(new DaoFictif());
        assertEquals(200.0, metier.calcul(), 0.0001);
    }
}
