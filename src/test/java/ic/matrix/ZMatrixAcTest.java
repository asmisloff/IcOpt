package ic.matrix;

import org.ejml.data.CMatrixRMaj;
import org.ejml.data.Complex_F32;
import org.ejml.dense.row.CommonOps_CDRM;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.IcMatrixTestHelper.measureTimeMillis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ZMatrixAcTest {

    @Test
    void add() {
        int size = 200;
        ZMatrixAc m = new ZMatrixAc(size, 3);
        m.add(0, 0, 1f, 1f);
        m.add(1, 1, 2f, 2f);
        m.add(1, 120, 120f, 120f);
        assertEquals("0: (1.0, 1.0);", m.rowToString(0));
        assertEquals("1: (2.0, 2.0); 120: (120.0, 120.0);", m.rowToString(1));
    }

    @Test
    void mul() {
        int size = 100;
        int w = 2;
        int nCont = 20;
        int nEdg = 5;
        CMatrixRMaj refZMatrix = randomSymmetricalMatrix(size, w);
        int maxRowW = maxRowWidth(refZMatrix);
        ZMatrixAc m = new ZMatrixAc(size, maxRowW);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                float re = refZMatrix.getReal(i, j);
                float im = refZMatrix.getImag(i, j);
                if (re != 0f || im != 0f) {
                    m.add(i, j, re, im);
                }
            }
        }
        assertArrayEquals(refZMatrix.data, m.toCMatrixRMaj().data, 0.5e-3f);

        KMatrix k = new KMatrix(nCont, size);
        CMatrixRMaj refKMatrix = new CMatrixRMaj(nCont, size);
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        for (int i = 0; i < nCont; ++i) {
            for (int j = 0; j < nEdg; j++) {
                int idx = tlr.nextInt(0, size);
                if (refZMatrix.getReal(i, idx) == 0f && refZMatrix.getImag(i, idx) == 0f) {
                    refKMatrix.set(i, idx, 1, 0f);
                    k.set(i, idx, 1);
                }
            }
        }
        assertArrayEquals(refKMatrix.data, k.toCMatrixRMaj().data, 0.5e-3f);

        CMatrixRMaj expected = new CMatrixRMaj(nCont, size);
        double ejmlTime = measureTimeMillis(() -> CommonOps_CDRM.mult(refKMatrix, refZMatrix, expected), 20_000);
        CMatrixRMaj actual = new CMatrixRMaj(nCont, size);
        double customTime = measureTimeMillis(() -> m.mul(k, actual), 20_000);
        assertArrayEquals(expected.data, actual.data, 0.5e-3f);

        System.out.printf("maxRowW = %d; avgRowW = %.2f\n", maxRowW, avgRowWidth(refZMatrix));
        System.out.printf("ejml: %.4f; custom: %.4f", ejmlTime, customTime);
    }

    private CMatrixRMaj randomSymmetricalMatrix(int size, int qnz) {
        CMatrixRMaj m = new CMatrixRMaj(size, size);
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        for (int i = 0; i < size - 1; i++) {
            m.set(i, i, 2f, 2f);
            for (int j = 0; j < qnz; j++) {
                int k = tlr.nextInt(i + 1, size);
                m.set(i, k, 1f, 1f);
                m.set(k, i, 1f, 1f);
            }
        }
        m.set(size - 1, size - 1, 2f, 2f);
        return m;
    }

    private int maxRowWidth(CMatrixRMaj m) {
        Complex_F32 buf = new Complex_F32();
        int res = 0;
        for (int i = 0; i < m.numRows; i++) {
            int cnt = 0;
            for (int j = 0; j < m.numCols; j++) {
                m.get(i, j, buf);
                if (buf.real != 0f || buf.imaginary != 0f) {
                    ++cnt;
                }
            }
            if (cnt > res) {
                res = cnt;
            }
        }
        return res;
    }

    private double avgRowWidth(CMatrixRMaj m) {
        int cnt = 0;
        for (int i = 0; i < m.numRows; i++) {
            for (int j = 0; j < m.numCols; j++) {
                if (m.getReal(i, j) != 0) {
                    ++cnt;
                }
            }
        }
        return ((double) cnt) / m.numRows;
    }
}