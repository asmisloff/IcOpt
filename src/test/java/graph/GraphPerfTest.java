package graph;

import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.TestEdge;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static java.lang.System.nanoTime;

public class GraphPerfTest {

    private Multigraph<Vertex, Edge> jGraph = new Multigraph<>(Edge.class);
    private final SchemaGraph<Vertex, Edge> schemaGraph = new SchemaGraph<>();

    @Test
    public void filling() {
        int n = 200;
        int timesToRepeat = 300000;

        measureTimeMillis(() -> {
            fillJGraphTMultigraph(n);
            jGraph = new Multigraph<>(Edge.class);
        }, timesToRepeat, System.out::println);

        measureTimeMillis(() -> {
            fillSchemaGraph(n);
            schemaGraph.clear();
        }, timesToRepeat, System.out::println);
    }

    @Test
    public void getEdgesOfVertices() {
        int n = 100;
        int timesToRepeat = 800_000;

        traverseJGraphTGraph(n, timesToRepeat);
        traverseSchemaGraph(n, timesToRepeat);
    }

    private void traverseJGraphTGraph(int n, int timesToRepeat) {
        fillJGraphTMultigraph(n);
        int cnt = 0;
        long t0 = nanoTime();
        for (int r = 0; r < timesToRepeat; r++) {
            for (Vertex v : jGraph.vertexSet()) {
                for (Edge ignored : jGraph.edgesOf(v)) {
                    ++cnt;
                }
            }
        }
        System.out.printf("cnt = %d, dt = %.2f\n", cnt, msFrom(t0));
    }

    private void traverseSchemaGraph(int n, int timesToRepeat) {
        fillSchemaGraph(n);
        int cnt = 0;
        long t0 = nanoTime();
        ListOfIncidence<Edge> loi = schemaGraph.getLoi();
        for (int r = 0; r < timesToRepeat; r++) {
            for (Vertex v : schemaGraph.getVertices()) {
                int begin = loi.begin(v.index);
                int end = loi.end(v.index);
                for (int i = begin; i < end; i++) {
                    Edge e = loi.get(i);
                    if (e.getSourceNode() != v && e.getTargetNode() != v) {
                        throw new IllegalStateException();
                    }
                    ++cnt;
                }
            }
        }
        System.out.printf("cnt = %d, dt = %.2f\n", cnt, msFrom(t0));
    }

    private static double msFrom(long t0) {
        return 1e-6 * (nanoTime() - t0);
    }

    private void fillJGraphTMultigraph(int n) {
        List<Vertex> vertices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Vertex v = new Vertex(i);
            jGraph.addVertex(v);
            vertices.add(v);
        }
        for (int i = 0; i < vertices.size() - 1; ++i) {
            Edge e = new Edge();
            jGraph.addEdge(vertices.get(i), vertices.get(i + 1), e);
        }
    }

    private void fillSchemaGraph(int n) {
        for (int i = 0; i < n; i++) {
            Vertex v = new Vertex(i);
            schemaGraph.addVertex(v, 2);
        }
        List<Vertex> vertices = schemaGraph.getVertices();
        for (int i = 0; i < vertices.size() - 1; ++i) {
            Edge e = new Edge();
            schemaGraph.addEdge(vertices.get(i), vertices.get(i + 1), e);
        }
    }

    private static class Vertex implements ICircuitNode {

        private int index;

        private Vertex(int index) { this.index = index; }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public void setIndex(int index) {
            this.index = index;
        }
    }

    private static class Edge extends TestEdge { }
}