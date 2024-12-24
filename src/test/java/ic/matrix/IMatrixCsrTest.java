package ic.matrix;

import org.junit.jupiter.api.Test;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IMatrixCsrTest {

    private final int numCols = 9;
    private final IMatrixCsr K = new IMatrixCsr(numCols);

    //@formatter:off
    private final int[][] data = {
            {  0,  1,  0,  1,  0,  0, -1,  0,  0 },
            {  1,  0,  0, -1,  0,  1,  0,  0,  0 },
            {  1,  0,  0, -1,  0,  1,  0,  1,  0 },
            { -1,  0,  1,  0,  0,  1,  1,  0, -1 },
            { -1,  0,  1,  0,  1,  0, -1,  0,  0 },
            {  0, -1, -1,  0, -1, -1,  0,  1,  1 },
            {  0,  1,  0,  1,  0,  0, -1,  1, -1 }
    };
    //@formatter:on

    @Test
    void initialState() {
        assertEquals(0, K.data.getSize());
        assertEquals(1, K.rows.getSize());
        assertEquals(0, K.rows.get(0));
        assertEquals(0, K.cols.getSize());
        assertEquals(0, K.numRows());
    }

    @Test
    void addRow() {
        K.addRow();
        assertEquals(0, K.data.getSize());
        assertEquals(2, K.rows.getSize());
        assertEquals(0, K.rows.get(0));
        assertEquals(0, K.rows.get(1));
        assertEquals(0, K.cols.getSize());
    }

    @Test
    void append() {
        K.addRow();
        K.append(1, 1);
        K.append(3, -1);
        assertEquals(1, K.get(0, 1));
        assertEquals(-1, K.get(0, 3));
        assertEquals(2, K.rows.get(K.rows.getSize() - 1));
    }

    @Test
    void fill() {
        IMatrixCsr K = new IMatrixCsr(numCols);
        for (int r = 0; r < 10; r++) {
            fillKMatrix(K);
            assertEquals(data.length, K.numRows());
            assertEquals(data[0].length, K.numCols());
            for (int i = 0; i < data.length; i++) {
                assertArrayEquals(data[i], denseRow(K, i));
            }
            K.reset(numCols);
        }
    }

    @Test
    void transpose() {
        IMatrixCsr A = new IMatrixCsr(numCols);
        IMatrixCsr AT = new IMatrixCsr(A.numRows());
        fillKMatrix(A);
        System.out.println(measureTimeMillis(() -> DcMatrixOps.transpose(A, AT), 200_000));
        AT.print();
        int[] expected = new int[data.length];
        for (int i = 0; i < AT.numRows(); i++) {
            for (int j = 0; j < data.length; j++) {
                expected[j] = data[j][i];
            }
            assertArrayEquals(expected, denseRow(AT, i));
        }
    }

    private void fillKMatrix(IMatrixCsr K) {
        for (int[] datum : data) {
            K.addRow();
            for (int j = 0; j < datum.length; j++) {
                int elt = datum[j];
                if (elt != 0) {
                    K.append(j, elt);
                }
            }
        }
    }

    private int[] denseRow(IMatrixCsr K, int rowIdx) {
        int[] res = new int[K.numCols()];
        for (int k = K.begin(rowIdx); k < K.end(rowIdx); k++) {
            int colIdx = K.cols.get(k);
            res[colIdx] = K.data.get(k);
        }
        return res;
    }
}