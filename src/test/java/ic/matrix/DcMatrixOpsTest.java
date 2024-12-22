package ic.matrix;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DcMatrixOpsTest {

    @Test
    void mult() {
        int numRows = 16;
        int numCols = 75;
        int nzCount = numCols / 5;
        int timesToRepeat = 200000;

        ZMatrixDc Z = randomZMatrixDc(numCols);
        DMatrixSparseCSC refZ = cscZ(Z);
        DMatrixSparseCSC refK = randomK(numRows, numCols, nzCount);
        IMatrixCsr K = kCsr(refK);
        DMatrixSparseCSC refKZ = new DMatrixSparseCSC(numRows, numCols);
        DMatrixSparseCSC refKTrans = new DMatrixSparseCSC(numCols, numRows);
        DMatrixSparseCSC refKZK = new DMatrixSparseCSC(numRows, numRows);
        IGrowArray gw = new IGrowArray(numCols);
        CommonOps_DSCC.transpose(refK, refKTrans, gw);
        DGrowArray gx = new DGrowArray(numCols);
        measureTimeMillis(() -> {
            CommonOps_DSCC.mult(refK, refZ, refKZ, gw, gx);
            CommonOps_DSCC.mult(refKZ, refKTrans, refKZK, gw, gx);
        }, timesToRepeat, System.out::println);

        DMatrixRMaj KZK = new DMatrixRMaj(numRows, numRows);
        measureTimeMillis(() -> DcMatrixOps.mult(K, Z, KZK), timesToRepeat, System.out::println);

        double[] expected = toRMaj(refKZK);
        double[] actual = KZK.data;
        assertArrayEquals(expected, actual, 0.5e-6);
    }

    private DMatrixSparseCSC randomK(int numRows, int numCols, int nzCount) {
        DMatrixSparseCSC res = new DMatrixSparseCSC(numRows, numCols, nzCount * numRows);
        Random r = ThreadLocalRandom.current();
        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < nzCount; k++) {
                int j = r.nextInt(0, numCols);
                double v = r.nextDouble(-1, 1);
                res.set(i, j, v > 0 ? 1 : -1);
            }
        }
        return res;
    }

    private ZMatrixDc randomZMatrixDc(int size) {
        ZMatrixDc res = new ZMatrixDc(size);
        Random r = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            res.set(i, r.nextDouble(1, 2));
        }
        return res;
    }

    private DMatrixSparseCSC cscZ(ZMatrixDc Z) {
        DMatrixSparseCSC res = new DMatrixSparseCSC(Z.size(), Z.size(), Z.size());
        for (int i = 0; i < Z.size(); i++) {
            res.set(i, i, Z.get(i));
        }
        return res;
    }

    private IMatrixCsr kCsr(DMatrixSparseCSC K) {
        IMatrixCsr res = new IMatrixCsr(K.numCols);
        for (int i = 0; i < K.numRows; i++) {
            res.addRow();
            for (int j = 0; j < K.numCols; j++) {
                int v = (int) K.get(i, j);
                if (v != 0) {
                    res.append(j, v);
                }
            }
        }
        return res;
    }

    private double[] toRMaj(DMatrixSparseCSC csc) {
        double[] res = new double[csc.numRows * csc.numCols];
        int idx = 0;
        for (int i = 0; i < csc.numRows; i++) {
            for (int j = 0; j < csc.numCols; j++) {
                res[idx++] = csc.get(i, j);
            }
        }
        return res;
    }
}