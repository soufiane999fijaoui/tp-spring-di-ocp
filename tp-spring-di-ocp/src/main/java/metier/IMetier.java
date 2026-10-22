package metier;

/**
 * Contrat de la couche metier.
 */
public interface IMetier {

    /**
     * Calcule la valeur metier a partir de la source de donnees active.
     *
     * @return la valeur calculee
     */
    double calcul();
}
