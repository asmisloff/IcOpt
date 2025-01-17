package graph;

import graph.data.CycleBasisTestDataProvider;
import ic.matrix.IMatrixCsr;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

    private final CycleBasisTestDataProvider td = new CycleBasisTestDataProvider();

    @ParameterizedTest
    @MethodSource("getCyclesTestData")
    void getCycles(CycleBasisTestDataProvider.DataSet ds) {
        int timesToRepeat = 200_000;
        CycleBasis.Traversing traversing = CycleBasis.Traversing.QUEUE_BASED;
        CycleBasis basis = new CycleBasis(ds.graph, traversing);
        IMatrixCsr K = new IMatrixCsr(ds.graph.getEdges().size());
        measureTimeMs("ref", timesToRepeat, () -> ds.computeRefCycles(traversing));
        measureTimeMs("act", timesToRepeat, () -> {
            basis.setGraph(ds.graph);
            basis.getCycles(K);
        });
        int[][] cycles = to2dArray(basis.getCycles(K));
        printCycles(cycles);
        assertCyclesEqual(ds.computeRefCycles(traversing), cycles);
    }

    private Stream<CycleBasisTestDataProvider.DataSet> getCyclesTestData() {
        return Stream.of(td.case1(), td.case2(), td.case3(), td.case4(), td.case5(), td.case6(), td.case7());
    }

    private int[][] to2dArray(IMatrixCsr K) {
        int[][] res = new int[K.numRows()][];
        for (int i = 0; i < K.numRows(); i++) {
            int begin = K.begin(i);
            int end = K.end(i);
            res[i] = new int[end - begin];
            for (int j = begin, idx = 0; j < end; j++, idx++) {
                int k = K.cols.get(j) + 1;
                if (K.data.get(j) < 0) {
                    k = -k;
                }
                res[i][idx] = k;
            }
        }
        return res;
    }

    private void assertCyclesEqual(List<int[]> expected, int[][] actual) {
        assertEquals(expected.size(), actual.length);
        Set<Set<Integer>> _expected = expected.stream()
                .map(a -> Arrays.stream(a).boxed().collect(Collectors.toSet()))
                .collect(Collectors.toSet());
        for (int i = 0; i < actual.length; i++) {
            Set<Integer> indices = Arrays.stream(actual[i]).boxed().collect(Collectors.toSet());
            /* Циклы могут отличаться направлениями обхода. Их нужно считать одинаковыми. */
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

    private int restoreIndex(int index) {
        return index > 0 ? index - 1 : index + 1;
    }
}