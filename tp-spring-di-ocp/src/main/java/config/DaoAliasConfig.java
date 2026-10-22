package config;

import dao.DaoApi;
import dao.IDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Strategie B : alias via un bean de configuration ({@code @Bean}).
 *
 * <p>Ce bean de configuration pointe vers l'implementation souhaitee. Pour
 * changer de cible, il suffit de changer UNE ligne ici
 * ({@code return new DaoApi();} -&gt; {@code return new DaoFile();}), sans
 * modifier les classes DAO ni la classe metier : c'est plus flexible que
 * {@code @Primary} pose directement sur une implementation.</p>
 *
 * <p><strong>Alternative a {@link DynamicDaoConfig}</strong> : les deux classes
 * declarent un bean {@code @Primary}, elles ne peuvent donc pas etre actives
 * en meme temps. Pour utiliser cette strategie, il suffit d'importer
 * {@code DaoAliasConfig} a la place de {@code DynamicDaoConfig} dans
 * {@code AppConfig}.</p>
 */
@Configuration
public class DaoAliasConfig {

    @Bean
    @Primary
    public IDao daoAlias() {
        // Pointe vers l'implementation API
        return new DaoApi();
    }
}
