package ic.matrix;

import ic.matrix.util.TimedValue;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicDoubleArray;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@SuppressWarnings({ "FieldCanBeLocal", "SameParameterValue" })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DcMatrixOpsTest {

    private final int timesToRepeat = 200_000;
    private final int numRows = 40;
    private final int numCols = 150;
    private final int nzCount = numCols / 5;

    private final ZMatrixDc Z = randomZMatrixDc(numCols);
    private final DMatrixSparseCSC refZ = zCsc(Z);

    private final DMatrixSparseCSC refK = randomK(numRows, numCols, nzCount);
    private final IMatrixCsr K = kCsr(refK);

    private final VectorDc E = new VectorDc(numCols);
    private final VectorDc I = new VectorDc(numCols);
    private final DMatrixSparseCSC refE = new DMatrixSparseCSC(numCols, 1);
    private final DMatrixSparseCSC refI = new DMatrixSparseCSC(numCols, 1);

    private final IGrowArray gw = new IGrowArray(numCols);
    private final DGrowArray gx = new DGrowArray(numCols);

    @BeforeAll
    void prepareTestData() {
        Random r = ThreadLocalRandom.current();
        fillVectors(E, refE, 3, r);
        fillVectors(I, refI, 10, r);
    }

    private void fillVectors(VectorDc vec, DMatrixSparseCSC refVec, int nzCnt, Random r) {
        for (int i = 0; i < nzCnt; i++) {
            int idx = r.nextInt(0, numCols);
            double value = r.nextDouble();
            refVec.set(idx, 0, value);
        }
        for (int i = 0; i < numCols; i++) {
            vec.insert(i, refVec.get(i, 0));
        }
    }

    @Test
    void KTrans() {
        IMatrixCsr KT = new IMatrixCsr(K.numRows());
        DMatrixSparseCSC refKT = new DMatrixSparseCSC(K.numCols(), K.numRows(), K.nzCnt());
        measureTimeMillis(() -> CommonOps_DSCC.transpose(refK, refKT, gw), timesToRepeat, System.out::println);
        measureTimeMillis(() -> DcMatrixOps.transpose(K, KT), timesToRepeat, System.out::println);
        assertArrayEquals(rMaj(refKT), rMaj(KT), 0.5e-6);
    }

    @Test
    void KZKT() {
        DMatrixSparseCSC refA = new DMatrixSparseCSC(K.numRows(), K.numRows());
        DMatrixSparseCSC refKZ = new DMatrixSparseCSC(numRows, numCols);
        DMatrixSparseCSC refKT = new DMatrixSparseCSC(K.numCols(), K.numRows(), K.nzCnt());
        CommonOps_DSCC.transpose(refK, refKT, gw);
        measureTimeMillis(() -> {
            CommonOps_DSCC.mult(refK, refZ, refKZ, gw, gx);
            CommonOps_DSCC.mult(refKZ, refKT, refA, gw, gx);
        }, timesToRepeat, System.out::println);

        final DMatrixRMaj[] A = { null };
        IMatrixCsr KT = DcMatrixOps.transpose(K, null);
        measureTimeMillis(() -> A[0] = DcMatrixOps.KZKT(K, Z, KT, A[0]), timesToRepeat, System.out::println);
        assertArrayEquals(rMaj(refA), A[0].data, 0.5e-6);
    }

    @Test
    void KE() {
        DMatrixSparseCSC refKE = new DMatrixSparseCSC(K.numRows(), 1);
        DynamicDoubleArray KE = new DynamicDoubleArray(K.numRows());
        IMatrixCsr KT = DcMatrixOps.transpose(K, null);
        TimedValue<DMatrixSparseCSC> ref = measureTimeMillis(() -> CommonOps_DSCC.mult(refK, refE, refKE, gw, gx), timesToRepeat);
        TimedValue<DynamicDoubleArray> act = measureTimeMillis(() -> DcMatrixOps.mult(E, KT, KE), timesToRepeat);
        assertArrayEquals(rMaj(refKE), KE.getData(), 0.5e-6);
        System.out.printf("tRef = %.6f; tAct = %.6f\n", ref.t(), act.t());
    }

    @Test
    void KZI() {
        DMatrixSparseCSC refKZI = new DMatrixSparseCSC(K.numRows(), 1);
        DynamicDoubleArray KZI = new DynamicDoubleArray(K.numRows());
        IMatrixCsr KT = DcMatrixOps.transpose(K, null);

        DMatrixSparseCSC refKZ = new DMatrixSparseCSC(numRows, numCols);
        CommonOps_DSCC.mult(refK, refZ, refKZ, gw, gx);
        TimedValue<DMatrixSparseCSC> ref = measureTimeMillis(() -> CommonOps_DSCC.mult(refKZ, refI, refKZI, gw, gx), timesToRepeat);
        TimedValue<DynamicDoubleArray> act = measureTimeMillis(() -> DcMatrixOps.mult(I, Z, KT, KZI), timesToRepeat);
        assertArrayEquals(rMaj(refKZI), KZI.getData(), 0.5e-6);
        System.out.printf("tRef = %.6f; tAct = %.6f\n", ref.t(), act.t());
    }

    @Test
    void sumOfVectors() {
        DMatrixSparseCSC refKZ = CommonOps_DSCC.mult(refK, refZ, null, gw, gx);
        DMatrixSparseCSC refKE = CommonOps_DSCC.mult(refK, refE, null, gw, gx);
        DMatrixSparseCSC refKZI = CommonOps_DSCC.mult(refKZ, refI, null, gw, gx);
        DMatrixSparseCSC refSum = CommonOps_DSCC.add(1, refKE, 1, refKZI, null, gw, gx);

        IMatrixCsr KT = new IMatrixCsr(K.numCols());
        DcMatrixOps.transpose(K, KT);
        DynamicDoubleArray KE = DcMatrixOps.mult(E, KT, null);
        DynamicDoubleArray KZI = DcMatrixOps.mult(I, Z, KT, null);
        DMatrixRMaj sum = DcMatrixOps.add(KE, KZI, null);

        assertArrayEquals(rMaj(refSum), sum.data, 0.5e-6);
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

    private DMatrixSparseCSC zCsc(ZMatrixDc Z) {
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

    private double[] rMaj(DMatrixSparseCSC csc) {
        double[] res = new double[csc.numRows * csc.numCols];
        int idx = 0;
        for (int i = 0; i < csc.numRows; i++) {
            for (int j = 0; j < csc.numCols; j++) {
                res[idx++] = csc.get(i, j);
            }
        }
        return res;
    }

    private double[] rMaj(IMatrixCsr csr) {
        double[] res = new double[csr.numRows() * csr.numCols()];
        int idx = 0;
        for (int i = 0; i < csr.numRows(); i++) {
            for (int j = 0; j < csr.numCols(); j++) {
                res[idx++] = csr.get(i, j);
            }
        }
        return res;
    }
}