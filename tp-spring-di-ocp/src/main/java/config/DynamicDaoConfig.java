package config;

import dao.IDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Strategie C : selection par proprietes externes.
 *
 * <p>Le bean {@code dynamicDao} est un <em>pointeur</em> vers l'implementation
 * designee par la propriete externe {@code dao.target} (fichier
 * {@code app.properties}). Pour changer de source de donnees, il suffit de
 * changer la valeur de cette propriete :</p>
 *
 * <pre>
 *   dao.target=dao       -&gt; base de donnees de production
 *   dao.target=dao2      -&gt; base de donnees de developpement
 *   dao.target=daoFile   -&gt; fichier local
 *   dao.target=daoApi    -&gt; API REST externe
 *   dao.target=daoProd   -&gt; base de production, bean present seulement si
 *                           le profil "prod" est actif (voir DaoImplProd)
 * </pre>
 *
 * <p>Aucune classe DAO ni aucune classe metier n'est modifiee, et aucune
 * recompilation n'est necessaire : c'est la resolution dynamique de Spring
 * qui fait le reste.</p>
 *
 * <p>{@link Primary} est pose sur ce seul bean : il est donc le bean injecte
 * par defaut dans {@code MetierImpl}, quelle que soit la cible.</p>
 */
@Configuration
public class DynamicDaoConfig {

    private final ApplicationContext applicationContext;
    private final String daoTarget;

    public DynamicDaoConfig(ApplicationContext applicationContext,
                            @Value("${dao.target:dao}") String daoTarget) {
        this.applicationContext = applicationContext;
        this.daoTarget = daoTarget;
    }

    /**
     * Pointeur dynamique vers la source designee par {@code dao.target}.
     *
     * <p>On recupere l'instance <em>gestionnee par le conteneur</em> (et non
     * une nouvelle instance) pour qu'il n'existe qu'un seul objet par source :
     * {@code getBean("dao2")} et le bean injecte dans le metier sont
     * strictement le meme objet.</p>
     */
    @Bean
    @Primary
    public IDao dynamicDao() {
        switch (daoTarget) {
            case "dao":
                return applicationContext.getBean("dao", IDao.class);
            case "dao2":
                return applicationContext.getBean("dao2", IDao.class);
            case "daoFile":
                return applicationContext.getBean("daoFile", IDao.class);
            case "daoApi":
                return applicationContext.getBean("daoApi", IDao.class);
            case "daoProd":
                return applicationContext.getBean("daoProd", IDao.class);
            default:
                throw new IllegalArgumentException("Cible DAO inconnue: " + daoTarget);
        }
    }
}
