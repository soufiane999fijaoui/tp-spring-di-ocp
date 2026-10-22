package dao;

import org.springframework.stereotype.Component;

/**
 * Source "fichier local".
 *
 * <p>Implementation simulee : elle lit une valeur depuis un fichier. Le
 * contrat {@link IDao} est le meme que pour les autres sources, donc cette
 * classe est interchangeable avec {@link DaoImpl} ou {@link DaoApi} sans
 * impacter le code metier.</p>
 */
@Component("daoFile")
public class DaoFile implements IDao {

    @Override
    public double getValue() {
        System.out.println("[DAO] Lecture depuis un fichier local");
        return 180.0;
    }
}
