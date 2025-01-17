package graph;

import java.util.ArrayList;
import java.util.List;

public class SchemaGraph<V extends ICircuitNode, E extends ICircuitEdge> {

    private final List<V> vertices = new ArrayList<>();
    private final List<E> edges = new ArrayList<>();
    private final ListOfIncidence<E> loi = new ListOfIncidence<>();

    public void addVertex(V v) {
        int idx = vertices.size();
        v.setIndex(idx);
        vertices.add(v);
        loi.ensureCapacity(idx);
    }

    public void addEdge(V src, V tgt, E e) {
        e.setIndex(edges.size());
        edges.add(e);
        e.setSourceNode(src);
        e.setTargetNode(tgt);
        loi.addEdge(src, tgt, e);
    }

    public void clear() {
        vertices.clear();
        edges.clear();
        loi.clear();
    }

    /** Список всех вершин. */
    public List<V> getVertices() {
        return vertices;
    }

    /** Список всех ребер. */
    public List<E> getEdges() {
        return edges;
    }

    /** Список инцидентности. */
    public ListOfIncidence<E> getLoi() {
        return loi;
    }
}
