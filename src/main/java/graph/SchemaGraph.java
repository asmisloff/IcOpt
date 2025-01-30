package graph;

import kotlin.NotImplementedError;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.Multigraph;

import java.util.*;
import java.util.function.Supplier;

public class SchemaGraph<V extends ICircuitNode, E extends ICircuitEdge> implements Graph<V, E> {

    private final List<V> vertices;
    private final List<E> edges;
    private final ListOfIncidence<E> loi;
    private final int defaultDegree;

    public SchemaGraph() {
        this(10);
    }

    public SchemaGraph(int defaultDegree) {
        this.defaultDegree = defaultDegree;
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        loi = new ListOfIncidence<>();
    }

    /**
     * Добавить вершину.
     * @param v      вершина.
     * @param degree степень вершины.
     */
    public boolean addVertex(V v, int degree) {
        if (!containsVertex(v)) {
            int idx = vertices.size();
            v.setIndex(idx);
            vertices.add(v);
            loi.appendVertex(degree);
            return true;
        }
        return false;
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

    @Override
    public boolean containsEdge(V sourceVertex, V targetVertex) {
        if (!sourceVertex.equals(targetVertex) && containsVertex(sourceVertex) && containsVertex(targetVertex)) {
            for (int i = loi.begin(sourceVertex.getIndex()); i < loi.end(sourceVertex.getIndex()); ++i) {
                E e = loi.get(i);
                if (e.getSourceNode().equals(targetVertex) || e.getTargetNode().equals(targetVertex)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsEdge(E e) {
        int i = e.getIndex();
        if (i >= 0 && i < edges.size()) {
            return edges.get(i).equals(e);
        }
        return false;
    }

    @Override
    public boolean containsVertex(V v) {
        int i = v.getIndex();
        if (i >= 0 && i < vertices.size()) {
            return vertices.get(i).equals(v);
        }
        return false;
    }

    @Override
    public Set<E> edgeSet() {
        return new SchemaGraphView<>(edges, 0, edges.size());
    }

    @Override
    public int degreeOf(V vertex) {
        if (!containsVertex(vertex)) {
            return -1;
        }
        return loi.end(vertex.getIndex()) - loi.begin(vertex.getIndex());
    }

    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
        Set<E> res = new HashSet<>();
        if (!sourceVertex.equals(targetVertex) && containsVertex(sourceVertex) && containsVertex(targetVertex)) {
            for (int i = loi.begin(sourceVertex.getIndex()); i < loi.end(sourceVertex.getIndex()); i++) {
                E e = loi.get(i);
                if (e.getSourceNode().equals(targetVertex) || e.getTargetNode().equals(targetVertex)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    @Override
    public E getEdge(V sourceVertex, V targetVertex) {
        if (!sourceVertex.equals(targetVertex) && containsVertex(sourceVertex) && containsVertex(targetVertex)) {
            for (int i = loi.begin(sourceVertex.getIndex()); i < loi.end(sourceVertex.getIndex()); i++) {
                E e = loi.get(i);
                if (e.getSourceNode().equals(targetVertex) || e.getTargetNode().equals(targetVertex)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public boolean addEdge(V src, V tgt, E e) {
        if (!containsEdge(e)) {
            e.setIndex(edges.size());
            edges.add(e);
            e.setSourceNode(src);
            e.setTargetNode(tgt);
            loi.addEdge(src.getIndex(), tgt.getIndex(), e);
            return true;
        }
        return false;
    }

    @Override
    public boolean addVertex(V v) {
        return addVertex(v, defaultDegree);
    }

    @Override
    public Set<E> edgesOf(V v) {
        return loi.edgesOf(v.getIndex());
    }

    @Override
    public Set<V> vertexSet() {
        return new SchemaGraphView<>(vertices, 0, vertices.size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getEdgeSource(E e) {
        return (V) e.getSourceNode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getEdgeTarget(E e) {
        return (V) e.getTargetNode();
    }

    @Override
    public GraphType getType() {
        return SchemaGraphType.instance;
    }

    @Override
    public int inDegreeOf(V vertex) {
        return degreeOf(vertex);
    }

    @Override
    public Set<E> incomingEdgesOf(V vertex) {
        return edgesOf(vertex);
    }

    @Override
    public int outDegreeOf(V vertex) {
        return degreeOf(vertex);
    }

    @Override
    public Set<E> outgoingEdgesOf(V vertex) {
        return edgesOf(vertex);
    }

    //<editor-fold desc="Неподдерживаемые и ненужные операции">
    @Deprecated
    @Override
    public V addVertex() {
        return null;
    }

    @Deprecated
    @Override
    public Supplier<V> getVertexSupplier() {
        return null;
    }

    @Deprecated
    @Override
    public Supplier<E> getEdgeSupplier() {
        return null;
    }

    @Deprecated
    @Override
    public E addEdge(V sourceVertex, V targetVertex) {
        return null;
    }

    @Deprecated
    @Override
    public double getEdgeWeight(E e) {
        return 0;
    }

    @Deprecated
    @Override
    public void setEdgeWeight(E e, double weight) {
    }

    @Deprecated
    @Override
    public boolean removeAllEdges(Collection<? extends E> edges) {
        throw modificationDenied();
    }

    @Deprecated
    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
        throw modificationDenied();
    }

    @Deprecated
    @Override
    public E removeEdge(V sourceVertex, V targetVertex) {
        throw modificationDenied();
    }

    @Deprecated
    @Override
    public boolean removeEdge(E e) {
        throw modificationDenied();
    }

    @Deprecated
    @Override
    public boolean removeVertex(V v) {
        throw modificationDenied();
    }

    @Deprecated
    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices) {
        throw modificationDenied();
    }

    private Error modificationDenied() {
        return new NotImplementedError("Изменяющие операции не поддерживаются");
    }
    //</editor-fold>
}

class SchemaGraphType implements GraphType {

    public static final SchemaGraphType instance = new SchemaGraphType();

    private SchemaGraphType() { }

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public boolean isUndirected() {
        return true;
    }

    @Override
    public boolean isMixed() {
        return false;
    }

    @Override
    public boolean isAllowingMultipleEdges() {
        return true;
    }

    @Override
    public boolean isAllowingSelfLoops() {
        return false;
    }

    @Override
    public boolean isAllowingCycles() {
        return true;
    }

    @Override
    public boolean isWeighted() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public boolean isPseudograph() {
        return false;
    }

    @Override
    public boolean isMultigraph() {
        return true;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public GraphType asDirected() {
        return null;
    }

    @Override
    public GraphType asUndirected() {
        return null;
    }

    @Override
    public GraphType asMixed() {
        return null;
    }

    @Override
    public GraphType asUnweighted() {
        return null;
    }

    @Override
    public GraphType asWeighted() {
        return null;
    }

    @Override
    public GraphType asModifiable() {
        return null;
    }

    @Override
    public GraphType asUnmodifiable() {
        return null;
    }
}
