package dao;

/**
 * Recuperation d'une valeur numerique depuis la source de donnees.
 *
 * @return la valeur recuperee
 */
public interface IDao {
    /**
     * Recupere une valeur numerique depuis la source de donnees.
     *
     * @return la valeur recuperee
     */
    double getValue();
}
