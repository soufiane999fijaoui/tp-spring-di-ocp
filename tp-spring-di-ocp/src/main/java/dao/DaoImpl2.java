package dao;

import org.springframework.stereotype.Component;

/**
 * Deuxieme implementation de {@link IDao} : base de donnees de developpement.
 *
 * <p>Ce bean est enregistre en permanence (comme les autres sources) et il
 * n'est pas marque {@code @Primary} : il n'est donc pas le bean injecte par
 * defaut. Il est utilise lorsque la propriete externe vaut
 * {@code dao.target=dao2}.</p>
 */
@Component("dao2")
public class DaoImpl2 implements IDao {

    @Override
    public double getValue() {
        System.out.println("[DAO] Lecture depuis la base de donnees (dev)");
        return 150.0;
    }
}
