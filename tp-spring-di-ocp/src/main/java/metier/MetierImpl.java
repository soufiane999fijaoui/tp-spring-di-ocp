package metier;

import dao.IDao;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * La classe metier : le consommateur de la dependance.
 *
 * <p>Elle depend de {@link IDao} via l'injection de constructeur. Cette classe
 * ne doit JAMAIS etre modifiee pour changer de source de donnees : c'est le
 * conteneur Spring qui decide, au demarrage, quelle implementation concrete
 * injecter (voir {@code config.DynamicDaoConfig} et la propriete externe
 * {@code dao.target}).</p>
 *
 * <p>L'injection de constructeur est preferee car elle garantit que la
 * dependance est toujours presente et rend la classe immuable.</p>
 */
@Service
public class MetierImpl implements IMetier {

    private final IDao dao;

    public MetierImpl(IDao dao) {
        this.dao = dao;
    }

    /**
     * Trace de demarrage : confirme l'implementation reellement injectee.
     * Appele par Spring juste apres l'injection de la dependance.
     */
    @PostConstruct
    public void init() {
        System.out.println("[TRACE] Implementation DAO injectee : " + dao.getClass().getSimpleName());
    }

    @Override
    public double calcul() {
        double valeur = dao.getValue();
        // Logique metier simple : double la valeur
        return valeur * 2.0;
    }
}
