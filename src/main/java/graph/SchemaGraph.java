package graph;

import org.jgrapht.graph.Multigraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SchemaGraph<V extends ICircuitNode, E extends ICircuitEdge> {

    private final List<V> vertices = new ArrayList<>();
    private final List<E> edges = new ArrayList<>();
    private final ListOfIncidence<E> loi = new ListOfIncidence<>();

    /**
     * Добавить вершину.
     * @param v      вершина.
     * @param degree степень вершины.
     */
    public void addVertex(V v, int degree) {
        int idx = vertices.size();
        v.setIndex(idx);
        vertices.add(v);
        loi.appendVertex(degree);
    }

    /**
     * Добавить ребро.
     * @param src начальная вершина.
     * @param tgt конечная вершина.
     * @param e   ребро.
     */
    public void addEdge(V src, V tgt, E e) {
        e.setIndex(edges.size());
        edges.add(e);
        e.setSourceNode(src);
        e.setTargetNode(tgt);
        loi.addEdge(src.getIndex(), tgt.getIndex(), e);
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

    /**
     * @param v вершина.
     * @return итератор по ребрам, инцидентным <code>v</code>.
     */
    public Iterator<E> edgesOf(V v) {
        return loi.edgeIterator(v.getIndex());
    }

    /** Встроить <code>mg</code> в данный граф. */
    @SuppressWarnings("unchecked")
    public void addGraph(Multigraph<V, E> mg) {
        for (V v : mg.vertexSet()) {
            addVertex(v, mg.degreeOf(v));
        }
        for (E e : mg.edgeSet()) {
            addEdge((V) e.getSourceNode(), (V) e.getTargetNode(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Multigraph<V, E> toJGraphTMultigraph(Class<E> clazz) {
        Multigraph<V, E> mg = new Multigraph<>(clazz);
        for (V v : vertices) {
            mg.addVertex(v);
        }
        for (E e : edges) {
            V src = (V) e.getSourceNode();
            ICircuitNode tgt = e.getTargetNode();
            mg.addEdge(src, (V) tgt, e);
        }
        return mg;
    }
}
