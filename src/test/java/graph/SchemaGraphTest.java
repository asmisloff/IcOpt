package graph;

import graph.data.SchemaGraphTestDataProvider;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.TestEdge;
import org.jgrapht.graph.TestVertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SchemaGraphTest {

    private final SchemaGraphTestDataProvider td = new SchemaGraphTestDataProvider();

    @ParameterizedTest
    @MethodSource("testData")
    void edgesOf(SchemaGraph<TestVertex, TestEdge> graph) {
        Multigraph<TestVertex, TestEdge> refGraph = graph.toJGraphTMultigraph(TestEdge.class);
        for (TestVertex v : graph.getVertices()) {
            Set<TestEdge> expected = refGraph.edgesOf(v);
            Set<TestEdge> actual = setOf(graph.edgesOf(v));
            assertEquals(expected, actual);
        }
    }

    @Test
    void addGraph() {
        SchemaGraph<TestVertex, TestEdge> actual = td.g6();
        Multigraph<TestVertex, TestEdge> expected = actual.toJGraphTMultigraph(TestEdge.class);
        Multigraph<TestVertex, TestEdge> mgToAdd = td.g7().toJGraphTMultigraph(TestEdge.class);
        actual.addGraph(mgToAdd);
        Graphs.addGraph(expected, mgToAdd);
        assertEquals(expected.vertexSet(), new HashSet<>(actual.getVertices()));
        assertEquals(expected.edgeSet(), new HashSet<>(actual.getEdges()));
        for (TestVertex v : expected.vertexSet()) {
            assertEquals(expected.edgesOf(v), setOf(actual.edgesOf(v)));
        }
    }

    private Stream<SchemaGraph<TestVertex, TestEdge>> testData() {
        return Stream.of(td.g1(), td.g2(), td.g3(), td.g4(), td.g5(), td.g6(), td.g7());
    }

    @NotNull
    private static <T> Set<T> setOf(Iterator<T> iter) {
        Set<T> actual = new HashSet<>();
        while (iter.hasNext()) {
            actual.add(iter.next());
        }
        return actual;
    }
}