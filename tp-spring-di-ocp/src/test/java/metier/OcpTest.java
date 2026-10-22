package metier;

import dao.IDao;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifie que la couche metier respecte le Principe d'Ouverture/Fermeture :
 * elle ne depend que de l'abstraction {@link IDao} et d'aucune implementation
 * concrete. Concretement, aucune source de donnees ne peut etre ajoutee ou
 * retiree sans modifier cette classe.
 */
public class OcpTest {

    @Test
    public void metierImplPossedeUnUniqueConstructeur() {
        Constructor<?>[] constructeurs = MetierImpl.class.getDeclaredConstructors();
        assertEquals(1, constructeurs.length);
        assertEquals(1, constructeurs[0].getParameterCount());
    }

    @Test
    public void leConstructeurAttendLAbstraction() {
        Constructor<?> constructeur = MetierImpl.class.getDeclaredConstructors()[0];
        assertEquals(IDao.class, constructeur.getParameterTypes()[0]);
    }

    @Test
    public void aucunAttributNeReferenceUneImplementationConcrete() {
        for (Field champ : MetierImpl.class.getDeclaredFields()) {
            if (Modifier.isStatic(champ.getModifiers())) {
                continue;
            }
            assertEquals(IDao.class, champ.getType());
            assertTrue("La couche metier ne doit dependre que d'une interface",
                    champ.getType().isInterface());
        }
    }
}
