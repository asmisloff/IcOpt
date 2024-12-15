package ic.matrix;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class KMatrixCsrTest {

    private final int numCols = 9;
    private final KMatrixCsr K = new KMatrixCsr(numCols);

    //@formatter:off
    private final double[][] data = {
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
        assertEquals(0, K.denseData.getSize());
        assertEquals(1, K.rows.getSize());
        assertEquals(0, K.rows.get(0));
        assertEquals(0, K.cols.getSize());
        assertEquals(0, K.numRows());
    }

    @Test
    void addRow() {
        K.addRow();
        assertEquals(K.numCols(), K.denseData.getSize());
        assertEquals(2, K.rows.getSize());
        assertEquals(0, K.rows.get(0));
        assertEquals(0, K.rows.get(1));
        assertEquals(0, K.cols.getSize());
    }

    @Test
    void append() {
        K.addRow();
        K.append(1, 1.0);
        K.append(3, -1.0);
        assertEquals(1.0, K.get(0, 1));
        assertEquals(-1.0, K.get(0, 3));
        assertEquals(2, K.rows.get(K.rows.getSize() - 1));
    }

    @Test
    void fill() {
        KMatrixCsr K = new KMatrixCsr(numCols);
        for (int r = 0; r < 10; r++) {
            doFillKMatrix(K);
            assertEquals(data.length, K.numRows());
            assertEquals(data[0].length, K.numCols());
            assertArrayEquals(flatten(data), Arrays.copyOfRange(K.denseData.getData(), 0, K.numRows() * K.numCols()));
            for (int i = 0; i < data.length; i++) {
                assertArrayEquals(data[i], denseRow(K, i));
            }
            K.reset(numCols);
        }
    }

    private void doFillKMatrix(KMatrixCsr K) {
        for (double[] datum : data) {
            K.addRow();
            for (int j = 0; j < datum.length; j++) {
                double elt = datum[j];
                if (elt != 0) {
                    K.append(j, elt);
                }
            }
        }
    }

    private double[] flatten(double[][] array2d) {
        int nRows = array2d.length;
        int nCols = array2d[0].length;
        double[] res = new double[nRows * nCols];
        int i = 0;
        for (double[] row : array2d) {
            for (double v : row) {
                res[i++] = v;
            }
        }
        return res;
    }

    private double[] denseRow(KMatrixCsr K, int rowIdx) {
        double[] res = new double[K.numCols()];
        int anchor = K.anchor(rowIdx);
        for (int c = K.csrBegin(rowIdx); c < K.csrEnd(rowIdx); c++) {
            int colIdx = K.cols.get(c);
            res[colIdx] = K.denseData.get(anchor + colIdx);
        }
        return res;
    }
}