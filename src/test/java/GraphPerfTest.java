import graph.ICircuitEdge;
import graph.ICircuitNode;
import graph.IncidenceList;
import graph.SchemaGraph;
import org.jgrapht.graph.Multigraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static java.lang.System.nanoTime;

public class GraphPerfTest {

    private Multigraph<Vertex, Edge> g = new Multigraph<>(Edge.class);
    private final SchemaGraph<Vertex, Edge> sg = new SchemaGraph<>();

    @Test
    public void filling() {
        int n = 200;
        int timesToRepeat = 300000;

        measureTimeMillis(() -> {
            fillJGraphTMultigraph(n);
            g = new Multigraph<>(Edge.class);
        }, timesToRepeat, System.out::println);

        measureTimeMillis(() -> {
            fillSchemaGraph(n);
            sg.clear();
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
            for (Vertex v : g.vertexSet()) {
                for (Edge ignored : g.edgesOf(v)) {
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
        IncidenceList<Edge> incidenceList = sg.incidenceList;
        for (int r = 0; r < timesToRepeat; r++) {
            for (Vertex v : sg.getVertices()) {
                int begin = incidenceList.begin(v.index);
                int end = begin + incidenceList.degree(v.index);
                for (int i = begin; i < end; i++) {
                    Edge e = incidenceList.get(i);
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
            g.addVertex(v);
            vertices.add(v);
        }
        for (int i = 0; i < vertices.size() - 1; ++i) {
            Edge e = new Edge();
            g.addEdge(vertices.get(i), vertices.get(i + 1), e);
        }
    }

    private void fillSchemaGraph(int n) {
        for (int i = 0; i < n; i++) {
            Vertex v = new Vertex(i);
            sg.addVertex(v);
        }
        List<Vertex> vertices = sg.getVertices();
        for (int i = 0; i < vertices.size() - 1; ++i) {
            Edge e = new Edge();
            sg.addEdge(vertices.get(i), vertices.get(i + 1), e);
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

    private static class Edge implements ICircuitEdge {

        private ICircuitNode sourceNode;
        private ICircuitNode targetNode;

        @Override
        public ICircuitNode getSourceNode() {
            return sourceNode;
        }

        @Override
        public ICircuitNode getTargetNode() {
            return targetNode;
        }

        public void setSourceNode(ICircuitNode sourceNode) {
            this.sourceNode = sourceNode;
        }

        public void setTargetNode(ICircuitNode targetNode) {
            this.targetNode = targetNode;
        }
    }
}

