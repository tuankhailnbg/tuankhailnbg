package nro.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */

public abstract class AbsManager<E> implements IManager<E> {

    protected List<E> list = new ArrayList<>();

    @Override
    public void add(E e) {
        list.add(e);
    }

    @Override
    public void remove(E e) {
        list.remove(e);
    }

    @Override
    public abstract E findByID(int id);

    public List<E> getList() {
        return list;
    }
}
