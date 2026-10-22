package dao;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Variante "profils" de la source "base de donnees de production".
 *
 * <p>Contrairement aux autres DAO, celui-ci n'est enregistre dans le
 * conteneur que si le profil {@code prod} est actif : c'est le mecanisme
 * natif de Spring pour activer/desactiver des beans selon un contexte
 * (dev, prod, test).</p>
 *
 * <p>Le profil s'active AVANT le demarrage du conteneur :</p>
 *
 * <pre>
 *   context.getEnvironment().setActiveProfiles("prod");
 *   context.register(AppConfig.class);
 *   context.refresh();
 * </pre>
 *
 * <p>ou, sans toucher au code, via la propriete systeme
 * {@code -Dspring.profiles.active=prod} (lue nativement par Spring).</p>
 */
@Component("daoProd")
@Profile("prod")
public class DaoImplProd implements IDao {

    @Override
    public double getValue() {
        System.out.println("[DAO] Lecture depuis la base de donnees (prod) - profil prod");
        return 100.0;
    }
}
