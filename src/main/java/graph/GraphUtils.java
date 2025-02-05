package graph;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.List;

public class GraphUtils {

    /** Удалить все вершины и ребра. */
    public static <V extends ICircuitNode, E extends ICircuitEdge>
    void clear(Graph<V, E> graph) {
        if (graph instanceof SchemaGraph) {
            ((SchemaGraph<V, E>) graph).clear();
        } else {
            List<V> vertices = graph.vertexSet().stream().toList();
            graph.removeAllVertices(vertices);
        }
    }

    /** Добавить все вершины и ребра из <code>src</code> в <code>dest</code>. */
    @SuppressWarnings("unchecked")
    public static <V extends ICircuitNode, E extends ICircuitEdge>
    void addGraph(Graph<V, E> dest, Graph<V, E> src) {
        if (dest instanceof SchemaGraph<V, E> schemaGraph) {
            for (V v : src.vertexSet()) {
                schemaGraph.addVertex(v, src.degreeOf(v));
            }
            for (E e : src.edgeSet()) {
                schemaGraph.addEdge((V) e.getSourceNode(), (V) e.getTargetNode(), e);
            }
        } else {
            Graphs.addGraph(dest, src);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V extends ICircuitNode, E extends ICircuitEdge, G extends Graph<V, E>>
    CycleBasis<V, E, G> cycleBasis(Graph<V, E> graph) {
        if (graph instanceof SchemaGraph) {
            return (CycleBasis<V, E, G>) new SchemaGraphCycleBasis<V, E>((SchemaGraph<?, ?>) graph);
        }
        return (CycleBasis<V, E, G>) new JGraphTCycleBasis<>(graph);
    }
}
