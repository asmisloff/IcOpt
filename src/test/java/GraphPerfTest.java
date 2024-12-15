import org.jgrapht.graph.Multigraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;

public class GraphPerfTest {

    private Multigraph<Vertex, Edge> g = new Multigraph<>(Edge.class);

    @Test
    public void filling() {
        int n = 200;
        long t0 = System.nanoTime();
        measureTimeMillis(() -> fillJGraphTMultigraph(n), 300000, System.out::println);
        System.out.println(1e-6 * (System.nanoTime() - t0));
    }

    private void fillJGraphTMultigraph(int n) {
        List<Vertex> vertices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Vertex v = new Vertex();
            g.addVertex(v);
            vertices.add(v);
        }
        for (int i = 0; i < vertices.size() - 1; ++i) {
            Edge e = new Edge();
            g.addEdge(vertices.get(i), vertices.get(i + 1), e);
        }
        g = new Multigraph<>(Edge.class);
    }

    private static class Vertex {
    }

    private static class Edge {
    }
}

