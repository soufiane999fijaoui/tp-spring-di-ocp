package dao;

import org.springframework.stereotype.Component;

/**
 * Source "base de donnees de production".
 *
 * <p>Le nom du bean ({@code dao}) est important : c'est ce nom qui sera
 * reference pour la selection par propriete externe
 * ({@code dao.target=dao}) et pour une selection explicite
 * ({@code @Qualifier("dao")}).</p>
 */
@Component("dao")
public class DaoImpl implements IDao {

    @Override
    public double getValue() {
        System.out.println("[DAO] Lecture depuis la base de donnees (prod)");
        return 100.0;
    }
}
