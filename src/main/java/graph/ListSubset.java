package graph;

import kotlin.NotImplementedError;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Представление части списка с интерфейсом множества. */
class ListSubset<T> implements Set<T> {

    private final List<T> list;
    private final int begin;
    private final int end;

    public ListSubset(List<T> list, int begin, int end) {
        this.list = list;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int size() {
        return end - begin;
    }

    @Override
    public boolean isEmpty() {
        return end == begin;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof ICircuitNode) {
            return list.get(((ICircuitNode) o).getIndex()).equals(o);
        } else if (o instanceof ICircuitEdge) {
            return list.get(((ICircuitEdge) o).getIndex()).equals(o);
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return list.subList(begin, end).iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        Object[] res = new Object[size()];
        for (int i = begin, j = 0; i < end; i++, j++) {
            res[j] = list.get(i);
        }
        return res;
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        Object[] dest = a.length >= size() ? a : new Object[size()];
        for (int i = begin, j = 0; i < end; i++, j++) {
            dest[j] = list.get(i);
        }
        //noinspection unchecked
        return (T1[]) dest;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean add(T t) {
        throw new NotImplementedError("Не поддерживается");
    }

    @Override
    public boolean remove(Object o) {
        throw new NotImplementedError("Не поддерживается");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new NotImplementedError("Не поддерживается");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new NotImplementedError("Не поддерживается");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new NotImplementedError("Не поддерживается");
    }

    @Override
    public void clear() {
        throw new NotImplementedError("Не поддерживается");
    }
}
