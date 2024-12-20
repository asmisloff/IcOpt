package graph;

import java.util.Arrays;

public class IncidenceList<E extends ICircuitEdge> {

    private final int stride;
    private int[] degrees;
    private ICircuitEdge[] incidenceList;

    public IncidenceList(int stride, int initialCapacity) {
        this.stride = stride;
        degrees = new int[initialCapacity];
        incidenceList = new ICircuitEdge[initialCapacity * stride];
    }

    public IncidenceList() {
        this(10, 40);
    }

    public int begin(int vertexIdx) {
        return vertexIdx * stride;
    }

    public int degree(int vertexIdx) {
        return degrees[vertexIdx];
    }

    @SuppressWarnings("unchecked")
    public E get(int idx) {
        return (E) incidenceList[idx];
    }

    void addEdge(ICircuitNode src, ICircuitNode tgt, E e) {
        addEdge(src, e);
        addEdge(tgt, e);
    }

    void clear() {
        Arrays.fill(degrees, 0);
    }

    private void addEdge(ICircuitNode v, E e) {
        int i = v.getIndex();
        int d = degree(i);
        incidenceList[begin(i) + d] = e;
        degrees[i] = ++d;
    }

    void ensureCapacity(int vertexIdx) {
        if (degrees.length < vertexIdx) {
            int[] _degrees = new int[degrees.length * 3 / 2];
            System.arraycopy(degrees, 0, _degrees, 0, degrees.length);
            degrees = _degrees;
            ICircuitEdge[] _incidenceList = new ICircuitEdge[_degrees.length * stride];
            System.arraycopy(incidenceList, 0, _incidenceList, 0, incidenceList.length);
            incidenceList = _incidenceList;
        }
    }
}
