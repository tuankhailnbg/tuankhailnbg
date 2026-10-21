package nro.manager;

/**
 * @author outcast c-cute hột me 😳
 */

public interface IManager <E> {

    void add(E e);

    void remove(E e);

    E findByID(int id);
}
