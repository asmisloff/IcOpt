package graph;

import java.util.ArrayList;
import java.util.List;

public class SchemaGraph<V extends ICircuitNode, E extends ICircuitEdge> {

    private final List<V> vertices = new ArrayList<>();
    private final List<E> edges = new ArrayList<>();
    public final IncidenceList<E> incidenceList = new IncidenceList<>();

    public void addVertex(V v) {
        int idx = vertices.size();
        v.setIndex(idx);
        vertices.add(v);
        incidenceList.ensureCapacity(idx);
    }

    public void addEdge(V src, V tgt, E e) {
        edges.add(e);
        e.setSourceNode(src);
        e.setTargetNode(tgt);
        incidenceList.addEdge(src, tgt, e);
    }

    public void clear() {
        vertices.clear();
        edges.clear();
        incidenceList.clear();
    }

    public List<V> getVertices() {
        return vertices;
    }
}
