package dao;

import org.springframework.stereotype.Component;

/**
 * Source "API REST externe".
 *
 * <p>Implementation simulee : elle simule un appel a un service REST distant.
 * Le contrat {@link IDao} est le meme que pour les autres sources, donc cette
 * classe est interchangeable avec {@link DaoImpl} ou {@link DaoFile} sans
 * impacter le code metier.</p>
 */
@Component("daoApi")
public class DaoApi implements IDao {

    @Override
    public double getValue() {
        System.out.println("[DAO] Appel a l'API REST externe");
        return 220.0;
    }
}
