package graph;

import graph.data.SchemaGraphTestDataProvider;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.TestEdge;
import org.jgrapht.graph.TestVertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SchemaGraphTest {

    private final SchemaGraphTestDataProvider td = new SchemaGraphTestDataProvider();

    @ParameterizedTest
    @MethodSource("testData")
    void edgesOf(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        for (TestVertex v : graph.getVertices()) {
            Set<TestEdge> expected = refGraph.edgesOf(v);
            Set<TestEdge> actual = graph.edgesOf(v);
            assertEquals(expected, actual);
        }
    }

    @Test
    void addGraph() {
        SchemaGraph<TestVertex, TestEdge> actual = td.g6();
        Multigraph<TestVertex, TestEdge> expected = actual.toJGraphTMultigraph(TestEdge.class);
        Multigraph<TestVertex, TestEdge> mgToAdd = td.g7().toJGraphTMultigraph(TestEdge.class);
        Graphs.addGraph(actual, mgToAdd);
        Graphs.addGraph(expected, mgToAdd);
        assertEquals(expected.vertexSet(), new HashSet<>(actual.getVertices()));
        assertEquals(expected.edgeSet(), new HashSet<>(actual.getEdges()));
        for (TestVertex v : expected.vertexSet()) {
            assertEquals(expected.edgesOf(v), actual.edgesOf(v));
        }
    }

    @ParameterizedTest
    @MethodSource("testData")
    void containsEdgeBetweenTwoVertices(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        int vCnt = graph.getVertices().size();
        for (int i = 0; i < vCnt - 1; ++i) {
            for (int j = i; j < vCnt; j++) {
                TestVertex v1 = graph.getVertices().get(i);
                TestVertex v2 = graph.getVertices().get(j);
                assertEquals(refGraph.containsEdge(v1, v2), graph.containsEdge(v1, v2));
            }
        }
        TestVertex someVertex = new TestVertex(0);
        assertFalse(graph.containsEdge(someVertex, graph.getVertices().get(0)));
        assertFalse(graph.containsEdge(graph.getVertices().get(0), someVertex));
        assertFalse(graph.containsEdge(someVertex, new TestVertex(0)));
    }

    @ParameterizedTest
    @MethodSource("testData")
    void containsEdge(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        for (TestEdge e : graph.edgeSet()) {
            assertEquals(refGraph.containsEdge(e), graph.containsEdge(e));
        }
        assertFalse(graph.containsEdge(new TestEdge()));
    }

    @ParameterizedTest
    @MethodSource("testData")
    void containsVertex(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        for (TestVertex v : graph.vertexSet()) {
            assertEquals(refGraph.containsVertex(v), graph.containsVertex(v));
        }
        assertFalse(graph.containsVertex(new TestVertex(0)));
    }

    @ParameterizedTest
    @MethodSource("testData")
    void edgeSet(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        assertEquals(refGraph.edgeSet(), graph.edgeSet());
    }

    @ParameterizedTest
    @MethodSource("testData")
    void degreeOf(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        for (TestVertex v : graph.vertexSet()) {
            assertEquals(refGraph.degreeOf(v), graph.degreeOf(v));
        }
        assertEquals(-1, graph.degreeOf(new TestVertex(0)));
    }

    @ParameterizedTest
    @MethodSource("testData")
    void getAllEdges(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        int vCnt = graph.getVertices().size();
        for (int i = 0; i < vCnt - 1; ++i) {
            for (int j = i; j < vCnt; j++) {
                TestVertex v1 = graph.getVertices().get(i);
                TestVertex v2 = graph.getVertices().get(j);
                assertEquals(refGraph.getAllEdges(v1, v2), graph.getAllEdges(v1, v2));
            }
        }
        TestVertex someVertex = new TestVertex(0);
        assertTrue(graph.getAllEdges(someVertex, graph.getVertices().get(0)).isEmpty());
        assertTrue(graph.getAllEdges(graph.getVertices().get(0), someVertex).isEmpty());
        assertTrue(graph.getAllEdges(someVertex, new TestVertex(0)).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("testData")
    void getEdge(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        int vCnt = graph.getVertices().size();
        for (int i = 0; i < vCnt - 1; ++i) {
            for (int j = i; j < vCnt; j++) {
                TestVertex v1 = graph.getVertices().get(i);
                TestVertex v2 = graph.getVertices().get(j);
                TestEdge edge = graph.getEdge(v1, v2);
                if (edge == null) {
                    assertNull(refGraph.getEdge(v1, v2));
                } else {
                    assertTrue(refGraph.getAllEdges(v1, v2).contains(edge));
                }
            }
        }
        TestVertex someVertex = new TestVertex(0);
        assertNull(graph.getEdge(someVertex, graph.getVertices().get(0)));
        assertNull(graph.getEdge(graph.getVertices().get(0), someVertex));
        assertNull(graph.getEdge(someVertex, new TestVertex(0)));
    }

    private Stream<SchemaGraph<TestVertex, TestEdge>> testData() {
        return Stream.of(td.g1(), td.g2(), td.g3(), td.g4(), td.g5(), td.g6(), td.g7());
    }
}