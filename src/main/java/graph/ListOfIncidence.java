package graph;

import ru.vniizht.asuterkortes.counter.latticemodel.DynamicArray;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

import java.util.Set;

/**
 * Список инцидентности графа.
 * @param <E> тип ребра.
 */
class ListOfIncidence<E extends ICircuitEdge> {

    private final DynamicIntArray begins;
    private final DynamicIntArray ends;
    private final DynamicArray<E> edges;
    private static final int MEM_RES = 2;
    private final DynamicArray<E> znEdges;


    public ListOfIncidence(int stride, int initialCapacity) {
        begins = new DynamicIntArray(initialCapacity);
        ends = new DynamicIntArray(initialCapacity);
        edges = new DynamicArray<>(initialCapacity * stride);
        znEdges = new DynamicArray<>(64);
        begins.setSize(1);
        ends.setSize(1);
        edges.setSize(1);
        znEdges.setSize(0);
    }

    public ListOfIncidence() {
        this(10, 40);
    }

    /**
     * Выделить память для хранения данных о новой вершине.
     * @param degree степень новой вершины.
     * @implNote память будет выделена с запасом на случай присоединения в будущем к этой вершине ребер тяговой сети.
     */
    public void appendVertex(int degree) {
        degree += MEM_RES;
        begins.append(edges.getSize());
        ends.append(edges.getSize());
        edges.setSize(edges.getSize() + degree);
    }

    /**
     * Добавить ребро.
     * @param srcIdx индекс начальной вершины.
     * @param tgtIdx индекс конечной вершины.
     * @param e      ребро.
     */
    public void addEdge(int srcIdx, int tgtIdx, E e) {
        addEdge(srcIdx, e);
        addEdge(tgtIdx, e);
    }

    /**
     * @param vIdx индекс вершины.
     * @return начало диапазона индексов для перебора ребер, инцидентных данной вершине.
     */
    public int begin(int vIdx) {
        return (vIdx == 0) ? zniEncode(0) : begins.get(vIdx);
    }

    /**
     * @param vIdx индекс вершины.
     * @return конец диапазона индексов (т.е., индекс, на единицу больший последнего в диапазоне) для перебора ребер,
     * инцидентных данной вершине.
     */
    public int end(int vIdx) {
        return (vIdx == 0) ? zniEncode(znEdges.getSize()) : ends.get(vIdx);
    }

    /**
     * Ребро по внутреннему индексу.
     * @apiNote внутренние индексы возвращают методы <code>begin</code> и <code>end</code>.
     */
    public E get(int internalIndex) {
        return internalIndex >= 0
                ? edges.get(internalIndex)
                : znEdges.get(zniDecode(internalIndex));
    }

    /** Множество ребер, инцидентных узлу с индексом <code>vIdx</code>. */
    public Set<E> edgesOf(int vIdx) {
        return vIdx > 0
                ? new ListSubset<>(edges, begin(vIdx), end(vIdx))
                : new ListSubset<>(znEdges, 0, znEdges.getSize());
    }

    /** Обнулить состояние перед повторным использованием. */
    void clear() {
        begins.setSize(1);
        ends.setSize(1);
        edges.setSize(1);
        znEdges.setSize(0);
    }

    private void addEdge(int vIdx, E e) {
        if (vIdx > 0) {
            edges.set(ends.getData()[vIdx]++, e);
        } else {
            znEdges.append(e);
        }
    }

    private int zniEncode(int idx) {
        return idx - znEdges.getSize() - 1;
    }

    private int zniDecode(int idx) {
        return idx + znEdges.getSize() + 1;
    }
}