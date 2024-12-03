package ic.matrix;

import ic.matrix.util.TimedValue;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KMatrixTest {

    //@formatter:off
    byte[][] data = {
            {  0,  1,  0,  1,  0,  0, -1,  0,  0 },
            {  1,  0,  0, -1,  0,  1,  0,  0,  0 },
            {  1,  0,  0, -1,  0,  1,  0,  1,  0 },
            { -1,  0,  1,  0,  0,  1,  1,  0, -1 },
            { -1,  0,  1,  0,  1,  0, -1,  0,  0 },
            {  0, -1, -1,  0, -1, -1,  0,  1,  1 },
            {  0,  1,  0,  1,  0,  0, -1,  1, -1 }
    };
    //@formatter:on

    private final CMatrixRMaj m;
    private final KMatrix k;
    private final CMatrixRMaj kRef;

    KMatrixTest() {
        m = RandomMatrices_CDRM.rectangle(10 * data[0].length, data[0].length, ThreadLocalRandom.current());
        k = new KMatrix(data);
        kRef = new CMatrixRMaj(data.length, data[0].length);
        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < data[0].length; c++) {
                kRef.set(r, c, data[r][c], 0);
            }
        }
    }

    @Test
    void constructor() {
        short[][] expected = {
                { 3, 1, 3, 6, 0, 0, 0, 0, 0, 0 },
                { 3, 0, 3, 5, 0, 0, 0, 0, 0, 0 },
                { 4, 0, 3, 5, 7, 0, 0, 0, 0, 0 },
                { 5, 0, 2, 5, 6, 8, 0, 0, 0, 0 },
                { 4, 0, 2, 4, 6, 0, 0, 0, 0, 0 },
                { 6, 1, 2, 4, 5, 7, 8, 0, 0, 0 },
                { 5, 1, 3, 6, 7, 8, 0, 0, 0, 0 }
        };
        for (int i = 0; i < expected.length; ++i) {
            assertArrayEquals(expected[i], k.nzi[i]);
        }
    }

    @Test
    void set() {
        k.set(0, 4, 1);
        k.set(2, 3, -1);
        assertEquals(1, k.data[0][4]);
        assertEquals(-1, k.data[2][3]);
    }

    @Test
    void rMulT() {
        int timesToRepeat = 100_000;
        CMatrixRMaj expected = new CMatrixRMaj(m.numRows, k.numRows());
        TimedValue<CMatrixRMaj> actual = measureTimeMillis(() -> k.mulTransR(m), timesToRepeat);
        double denseTime = measureTimeMillis(() -> CommonOps_CDRM.multTransB(m, kRef, expected), timesToRepeat);
        assertArrayEquals(expected.data, actual.value().data);
        System.out.printf("sparse: %f ms, dense: %f ms\n", actual.t(), denseTime);
    }
}