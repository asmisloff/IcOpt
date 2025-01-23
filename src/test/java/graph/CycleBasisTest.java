package graph;

import graph.data.SchemaGraphTestDataProvider;
import ic.matrix.IMatrixCsr;
import org.jgrapht.alg.cycle.AbstractFundamentalCycleBasis;
import org.jgrapht.alg.cycle.QueueBFSFundamentalCycleBasis;
import org.jgrapht.alg.cycle.StackBFSFundamentalCycleBasis;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.TestEdge;
import org.jgrapht.graph.TestVertex;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CycleBasisTest {

    private static final SchemaGraphTestDataProvider td = new SchemaGraphTestDataProvider();
    private final CycleBasis.Traversing traversing = CycleBasis.Traversing.QUEUE_BASED;
    private final CycleBasis basis = new CycleBasis(td.g1());
    private IMatrixCsr K = null;
    private static final int timesToRepeat = 200_000;

    @ParameterizedTest
    @MethodSource("getCyclesTestData")
    void getCycles(SchemaGraph<TestVertex, TestEdge> graph) {
        basis.setGraph(graph);
        Multigraph<TestVertex, TestEdge> jGraphTMultigraph = graph.toJGraphTMultigraph(TestEdge.class);
        measureTimeMs("ref", timesToRepeat, () -> computeRefCycles(jGraphTMultigraph, traversing));
        measureTimeMs("act", timesToRepeat, () -> {
            basis.setGraph(graph);
            K = basis.getCycles(K, traversing);
        });
        int[][] cycles = to2dArray(basis.getCycles(K, traversing));
        printCycles(cycles);
        assertCyclesEqual(computeRefCycles(jGraphTMultigraph, traversing), cycles);
    }

    private Stream<SchemaGraph<TestVertex, TestEdge>> getCyclesTestData() {
        return Stream.of(td.g1(), td.g2(), td.g3(), td.g4(), td.g5(), td.g6(), td.g7());
    }

    /**
     * Вычислить базисные циклы средствами JGraphT.
     * @return список массивов с преобразованными индексами ребер, образующих циклы.
     * К индексу прибавляется единица, и в случае, если направление обхода не совпадает с направлением ребра,
     * назначается знак минус. Прибавление единицы решает проблему знака для нулевого индекса.
     */
    private List<int[]> computeRefCycles(
            Multigraph<TestVertex, TestEdge> jGraphTMultigraph,
            CycleBasis.Traversing traversing
    ) {
        AbstractFundamentalCycleBasis<TestVertex, TestEdge> basis = traversing == CycleBasis.Traversing.QUEUE_BASED
                ? new QueueBFSFundamentalCycleBasis<>(jGraphTMultigraph)
                : new StackBFSFundamentalCycleBasis<>(jGraphTMultigraph);
        Set<List<TestEdge>> cycles = basis.getCycleBasis().getCycles();
        List<int[]> res = new ArrayList<>();
        for (List<TestEdge> c : cycles) {
            int[] map = new int[c.size()];
            TestEdge e1 = c.get(0);
            TestEdge e2 = c.get(1);
            ICircuitNode src1 = e1.getSourceNode();
            ICircuitNode src2 = e2.getSourceNode();
            ICircuitNode tgt1 = e1.getTargetNode();
            ICircuitNode tgt2 = e2.getTargetNode();
            ICircuitNode begin = (tgt1 == src2 || tgt1 == tgt2) ? src1 : tgt1;
            for (int i = 0; i < c.size(); i++) {
                ICircuitEdge e = c.get(i);
                ICircuitNode src = e.getSourceNode();
                ICircuitNode tgt = e.getTargetNode();
                if (begin == src) {
                    begin = tgt;
                    map[i] = e.getIndex() + 1;
                } else {
                    begin = src;
                    map[i] = -e.getIndex() - 1;
                }
            }
            res.add(map);
        }
        return res;
    }

    /**
     * @param K матрица независимых контуров.
     * @return массивы с преобразованными индексами ребер, образующих циклы. К индексу прибавляется единица, и в случае,
     * если направление обхода не совпадает с направлением ребра, назначается знак минус. Прибавление единицы решает
     * проблему знака для нулевого индекса.
     */
    private int[][] to2dArray(IMatrixCsr K) {
        int[][] res = new int[K.numRows()][];
        for (int i = 0; i < K.numRows(); i++) {
            int begin = K.begin(i);
            int end = K.end(i);
            res[i] = new int[end - begin];
            for (int j = begin, idx = 0; j < end; j++, idx++) {
                int k = (K.cols.get(j) + 1) * K.data.get(j);
                res[i][idx] = k;
            }
        }
        return res;
    }

    /**
     * Восстановить индекс после преобразования в методе <code>to2dArray</code> или <code>computeRefCycles</code>: прибавить либо вычесть единицу в
     * зависимости от знака.
     */
    private int restoreIndex(int index) {
        return index > 0 ? index - 1 : index + 1;
    }

    private void assertCyclesEqual(List<int[]> expected, int[][] actual) {
        assertEquals(expected.size(), actual.length);
        Set<Set<Integer>> _expected = expected.stream()
                .map(a -> Arrays.stream(a).boxed().collect(Collectors.toSet()))
                .collect(Collectors.toSet());
        for (int i = 0; i < actual.length; i++) {
            Set<Integer> indices = Arrays.stream(actual[i]).boxed().collect(Collectors.toSet());
            /* Циклы могут отличаться только направлениями обхода. Тогда все знаки будут противоположны. */
            Set<Integer> negatedIndices = indices.stream().map(x -> -x).collect(Collectors.toSet());
            System.out.printf("%d: %s\n", i, indices.stream().map(this::restoreIndex).toList());
            assertTrue(_expected.contains(indices) || _expected.contains(negatedIndices));
        }
    }

    private void printCycles(int[][] cycles) {
        int totalEdgesQty = 0;
        for (int[] cycle : cycles) {
            for (int value : cycle) {
                System.out.printf("%d, ", restoreIndex(value));
            }
            System.out.println();
            totalEdgesQty += cycle.length;
        }
        System.out.printf("Общее количество ребер в циклах: %d\n", totalEdgesQty);
    }
}