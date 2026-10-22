package dao;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests des differentes implementations de {@link IDao}.
 *
 * <p>Elles sont toutes interchangeables : chacune respecte le meme contrat et
 * fournit sa propre valeur. C'est ce qui permet au conteneur Spring de
 * substituer l'une a l'autre sans toucher a la couche metier.</p>
 */
public class DaoImplTest {

    @Test
    public void daoImplLitLaBaseProd() {
        assertEquals(100.0, new DaoImpl().getValue(), 0.0001);
    }

    @Test
    public void daoImpl2LitLaBaseDev() {
        assertEquals(150.0, new DaoImpl2().getValue(), 0.0001);
    }

    @Test
    public void daoFileLitLeFichierLocal() {
        assertEquals(180.0, new DaoFile().getValue(), 0.0001);
    }

    @Test
    public void daoApiAppelleLApiRest() {
        assertEquals(220.0, new DaoApi().getValue(), 0.0001);
    }
}
