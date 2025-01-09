package ic.matrix;

import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static ic.matrix.util.IcMatrixTestHelper.measureTimeMs;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AcMatrixOpsTest {

    private final int timesToRepeat = 20_000;
    private final int numRows = 30;
    private final int numCols = 100;

    private final ZMatrixRMaj refZ = randomSymmetricalDenseMatrix(numCols, 0.12);
    private final ZMatrixCsr Z = zMatrixCsr(refZ);
    private final ZMatrixRMaj refK = randomDenseKMatrix(numRows, numCols, 10);
    private final IMatrixCsr K = iMatrixCsr(refK);

    private final ZMatrixRMaj refKZ = new ZMatrixRMaj(refK);
    private ZMatrixRMaj KZ = new ZMatrixRMaj(refK);
    private final ZMatrixRMaj refKZKT = new ZMatrixRMaj(refK.numRows, refK.numRows);
    private ZMatrixRMaj KZKT = new ZMatrixRMaj(refKZKT);

//    @BeforeAll
//    void prepareTestData() {
//        Random r = ThreadLocalRandom.current();
//    }

    @Test
    public void KxZ() {
        measureTimeMs("ref", timesToRepeat, () -> CommonOps_ZDRM.mult(refK, refZ, refKZ));
        measureTimeMs("act", timesToRepeat, () -> AcMatrixOps.KZ(K, Z, KZ));
        assertArrayEquals(refKZ.data, KZ.data, 0.5e-6);
    }

    @Test
    public void KZxKT() {
        CommonOps_ZDRM.mult(refK, refZ, refKZ);
        measureTimeMs("ref", timesToRepeat, () -> CommonOps_ZDRM.multTransB(refKZ, refK, refKZKT));
        KZ = AcMatrixOps.KZ(K, Z, KZ);
        measureTimeMs("act", timesToRepeat, () -> AcMatrixOps.KZKT(KZ, K, KZKT));
        assertArrayEquals(refKZKT.data, KZKT.data, 0.5e-4f);
    }

    @Test
    public void KZKT() {
        measureTimeMs("ref", timesToRepeat, () -> {
            CommonOps_ZDRM.mult(refK, refZ, refKZ);
            CommonOps_ZDRM.multTransB(refKZ, refK, refKZKT);
        });
        measureTimeMs("act", timesToRepeat, () -> {
            AcMatrixOps.KZ(K, Z, KZ);
            KZKT = AcMatrixOps.KZKT(KZ, K, KZKT);
        });
        assertArrayEquals(refKZKT.data, KZKT.data, 0.5e-6);
    }

    @Test
    public void KxE() {
        int timesToRepeat = 20_000;
        int numberOfCycles = 25;
        int numberOfEdges = 100;
        int numberOfEdgesInCycle = 10;
        int nonZeroEltQty = 3;
        ZMatrixRMaj denseK = randomDenseKMatrix(numberOfCycles, numberOfEdges, numberOfEdgesInCycle);
        KMatrix sparseK = new KMatrix(numberOfCycles, numberOfEdges);
        copyElts(sparseK, denseK);
        ZMatrixRMaj denseE = randomDenseVector(numberOfEdges, nonZeroEltQty);
        VectorAc sparseE = new VectorAc(numberOfEdges);
        copyElts(sparseE, denseE);
        ZMatrixRMaj expected = new ZMatrixRMaj(numberOfCycles, 1);
        ZMatrixRMaj actual = new ZMatrixRMaj(numberOfCycles, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_ZDRM.mult(denseK, denseE, expected), timesToRepeat);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mul(sparseK, sparseE, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data, 0.5e-6f);
        logTime(denseTime, sparseTime, timesToRepeat);
    }

    @Test
    public void KZxI() {
        int timesToRepeat = 200_000;
        int numRows = 25;
        int numCols = 100;
        int nonZeroEltQty = 10;
        ZMatrixRMaj M = RandomMatrices_ZDRM.rectangle(numRows, numCols, ThreadLocalRandom.current());
        ZMatrixRMaj denseV = randomDenseVector(numCols, nonZeroEltQty);
        VectorAc sparseV = new VectorAc(numCols);
        copyElts(sparseV, denseV);
        ZMatrixRMaj expected = new ZMatrixRMaj(numRows, 1);
        ZMatrixRMaj actual = new ZMatrixRMaj(numRows, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_ZDRM.mult(M, denseV, expected), timesToRepeat);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mul(M, sparseV, actual), timesToRepeat);
        double sparseTime2 = measureTimeMillis(() -> AcMatrixOps.mul2(M, sparseV, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data, 0.5e-6f);
        logTime(denseTime, sparseTime, timesToRepeat);
        System.out.printf("t2 = %.6f (%.2f); %.2f", sparseTime2, sparseTime2 * timesToRepeat, sparseTime / sparseTime2);
    }

    @Test
    public void KTrans_x_Icc() {
        int timesToRepeat = 200_000;
        int numberOfCycles = 25;
        int numberOfEdges = 100;
        int numberOfEdgesInCycle = 10;
        ZMatrixRMaj denseK = randomDenseKMatrix(numberOfCycles, numberOfEdges, numberOfEdgesInCycle);
        KMatrix sparseK = new KMatrix(numberOfCycles, numberOfEdges);
        copyElts(sparseK, denseK);
        ZMatrixRMaj Icc = RandomMatrices_ZDRM.rectangle(numberOfCycles, 1, ThreadLocalRandom.current());
        ZMatrixRMaj expected = new ZMatrixRMaj(numberOfEdges, 1);
        ZMatrixRMaj actual = new ZMatrixRMaj(numberOfEdges, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_ZDRM.multTransA(denseK, Icc, expected), timesToRepeat);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mulTransK(sparseK, Icc, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data, 0.5e-6f);
        logTime(denseTime, sparseTime, timesToRepeat);
    }

    @Test
    public void ZxJ() {
        int timesToRepeat = 200_000;
        int size = 100;
        double nonZeroEltFraction = 0.05;
        int strideEstimation = (int) (size * nonZeroEltFraction * 1.2);
        ZMatrixRMaj denseZ = randomSymmetricalDenseMatrix(size, nonZeroEltFraction);
        ZMatrixAc sparseZ = new ZMatrixAc(size, strideEstimation);
        copyElts(sparseZ, denseZ);
        ZMatrixRMaj J = randomDenseVector(size, size);
        ZMatrixRMaj expected = new ZMatrixRMaj(size, 1);
        ZMatrixRMaj actual = new ZMatrixRMaj(size, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_ZDRM.mult(denseZ, J, expected), timesToRepeat);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mul(sparseZ, J, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data, 0.5e-6f);
        logTime(denseTime, sparseTime, timesToRepeat);
    }

    private IMatrixCsr iMatrixCsr(ZMatrixRMaj M) {
        IMatrixCsr res = new IMatrixCsr(M.numCols);
        for (int i = 0; i < M.numRows; i++) {
            res.addRow();
            for (int j = 0; j < M.numCols; j++) {
                double re = M.getReal(i, j);
                if (re != 0) {
                    res.append(j, (int) re);
                }
            }
        }
        return res;
    }

    private ZMatrixCsr zMatrixCsr(ZMatrixRMaj M) {
        ZMatrixCsr res = new ZMatrixCsr(M.numCols);
        Complex_F64 z = new Complex_F64();
        for (int i = 0; i < M.numRows; i++) {
            res.addRow();
            for (int j = 0; j < M.numCols; j++) {
                M.get(i, j, z);
                if (z.real != 0 && z.imaginary != 0) {
                    res.append(j, z.real, z.imaginary);
                }
            }
        }
        return res;
    }

    private double[] rMaj(ZMatrixCsr Z) {
        double[] res = new double[Z.numRows() * Z.numCols() * 2];
        Complex_F64 buf = new Complex_F64();
        int idx = 0;
        for (int i = 0; i < Z.numRows(); i++) {
            for (int j = 0; j < Z.numCols(); j++) {
                Z.get(i, j, buf);
                res[idx++] = buf.real;
                res[idx++] = buf.imaginary;
            }
        }
        return res;
    }

    private void copyElts(ZMatrixAc dest, ZMatrixRMaj src) {
        for (int i = 0; i < src.numRows; i++) {
            for (int j = 0; j < src.numCols; j++) {
                double re = src.getReal(i, j);
                double im = src.getImag(i, j);
                dest.add(i, j, re, im);
            }
        }
    }

    private void copyElts(KMatrix dest, ZMatrixRMaj src) {
        for (int i = 0; i < src.numRows; i++) {
            for (int j = 0; j < src.numCols; j++) {
                double re = src.getReal(i, j);
                dest.set(i, j, (int) re);
            }
        }
    }

    private void copyElts(IMatrixCsr dest, ZMatrixRMaj src) {
        for (int i = 0; i < src.numRows; i++) {
            dest.addRow();
            for (int j = 0; j < src.numCols; j++) {
                int re = (int) src.getReal(i, j);
                if (re != 0) {
                    dest.append(j, re);
                }
            }
        }
    }

    private void copyElts(VectorAc dest, ZMatrixRMaj src) {
        for (int i = 0; i < src.numRows; i++) {
            double re = src.getReal(i, 0);
            double im = src.getImag(i, 0);
            if (re != 0 || im != 0) {
                dest.insert(i, re, im);
            }
        }
    }

    private ZMatrixRMaj randomSymmetricalDenseMatrix(int size, double nonZeroEltFraction) {
        ZMatrixRMaj m = new ZMatrixRMaj(size, size);
        int n = (int) (nonZeroEltFraction * (size * size - size)) / 2;
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            double re = tlr.nextFloat();
            double im = tlr.nextFloat();
            m.set(i, i, re, im);
        }
        for (int i = 0; i < n; i++) {
            double re = tlr.nextFloat(-1, 1);
            double im = tlr.nextDouble(-1, 1);
            int j = 0, k = 0;
            while (m.getReal(j, k) != 0 || m.getImag(j, k) != 0) {
                j = tlr.nextInt(0, size);
                k = tlr.nextInt(0, size);
            }
            m.set(j, k, re, im);
            m.set(k, j, re, im);
        }
        return m;
    }

    private ZMatrixRMaj randomDenseKMatrix(int numRows, int numCols, int nonZeroEltsInRow) {
        Random r = ThreadLocalRandom.current();
        ZMatrixRMaj m = new ZMatrixRMaj(numRows, numCols);
        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < nonZeroEltsInRow; k++) {
                int j = r.nextInt(0, numCols);
                int v = r.nextBoolean() ? 1 : -1;
                m.set(i, j, v, 0);
            }
        }
        return m;
    }

    private ZMatrixRMaj randomDenseVector(int numRows, int numNonZeroes) {
        ZMatrixRMaj res = new ZMatrixRMaj(numRows, 1);
        Random r = ThreadLocalRandom.current();
        int origin = -1;
        int bound = 1;
        if (numNonZeroes < numRows) { // разреженный
            for (int i = 0; i < numNonZeroes; i++) {
                int idx = r.nextInt(0, numRows);
                double re = r.nextFloat(origin, bound);
                double im = r.nextFloat(origin, bound);
                res.set(idx, 0, re, im);
            }
        } else { // плотный
            for (int i = 0; i < numRows; i++) {
                res.set(i, 0, r.nextFloat(origin, bound), r.nextFloat(origin, bound));
            }
        }
        return res;
    }

    private void logTime(double denseTime, double sparseTime, int timesToRepeat) {
        System.out.printf("Итого   -- плотн.: %.2f мс; разр.: %.2f мс\n", denseTime * timesToRepeat, sparseTime * timesToRepeat);
        System.out.printf("На цикл -- плотн.: %.6f мс; разр.: %.6f мс\n", denseTime, sparseTime);
    }
}