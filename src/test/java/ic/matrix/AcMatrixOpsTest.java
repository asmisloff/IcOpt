package ic.matrix;

import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class AcMatrixOpsTest {

    @Test
    public void KxZ() {
        int size = 100;
        double nonZeroEltFraction = 0.05;
        int numberOfCycles = 20;
        int numberOfEdgesInCycle = 10;
        int strideEstimation = (int) (size * nonZeroEltFraction);

        CMatrixRMaj denseZ = randomSymmetricalDenseMatrix(size, nonZeroEltFraction);
        ZMatrixAc sparseZ = new ZMatrixAc(size, strideEstimation);
        copyElts(sparseZ, denseZ);
        CMatrixRMaj denseK = randomDenseKMatrix(numberOfCycles, size, numberOfEdgesInCycle);
        KMatrix sparseK = new KMatrix(numberOfCycles, size);
        copyElts(sparseK, denseK);

        CMatrixRMaj expected = new CMatrixRMaj(numberOfCycles, size);
        int timesToRepeat = 20_000;
        double denseTime = measureTimeMillis(() -> CommonOps_CDRM.mult(denseK, denseZ, expected), timesToRepeat);
        CMatrixRMaj actual = new CMatrixRMaj(numberOfCycles, size);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mul(sparseK, sparseZ, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data, 0.5e-3f);
        logTime(denseTime, sparseTime, timesToRepeat);
    }

    @Test
    public void KZxKTrans() {
        int timesToRepeat = 20_000;
        int numRows = 30;
        int numCols = 100;
        int numberOfEdgesInCycle = 10;
        CMatrixRMaj M = RandomMatrices_CDRM.rectangle(numRows, numCols, ThreadLocalRandom.current());
        CMatrixRMaj denseK = randomDenseKMatrix(numRows, numCols, numberOfEdgesInCycle);
        KMatrix sparseK = new KMatrix(numRows, numCols);
        copyElts(sparseK, denseK);
        CMatrixRMaj expected = new CMatrixRMaj(numRows, numRows);
        CMatrixRMaj actual = new CMatrixRMaj(numRows, numRows);
        double denseTime = measureTimeMillis(() -> CommonOps_CDRM.multTransB(M, denseK, expected), timesToRepeat);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mulTransK(M, sparseK, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data);
        logTime(denseTime, sparseTime, timesToRepeat);
    }

    @Test
    public void KxE() {
        int timesToRepeat = 20_000;
        int numberOfCycles = 25;
        int numberOfEdges = 100;
        int numberOfEdgesInCycle = 10;
        int nonZeroEltQty = 3;
        CMatrixRMaj denseK = randomDenseKMatrix(numberOfCycles, numberOfEdges, numberOfEdgesInCycle);
        KMatrix sparseK = new KMatrix(numberOfCycles, numberOfEdges);
        copyElts(sparseK, denseK);
        CMatrixRMaj denseE = randomDenseVector(numberOfEdges, nonZeroEltQty);
        VectorAc sparseE = new VectorAc(numberOfEdges);
        copyElts(sparseE, denseE);
        CMatrixRMaj expected = new CMatrixRMaj(numberOfCycles, 1);
        CMatrixRMaj actual = new CMatrixRMaj(numberOfCycles, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_CDRM.mult(denseK, denseE, expected), timesToRepeat);
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
        CMatrixRMaj M = RandomMatrices_CDRM.rectangle(numRows, numCols, ThreadLocalRandom.current());
        CMatrixRMaj denseV = randomDenseVector(numCols, nonZeroEltQty);
        VectorAc sparseV = new VectorAc(numCols);
        copyElts(sparseV, denseV);
        CMatrixRMaj expected = new CMatrixRMaj(numRows, 1);
        CMatrixRMaj actual = new CMatrixRMaj(numRows, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_CDRM.mult(M, denseV, expected), timesToRepeat);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mul(M, sparseV, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data, 0.5e-6f);
        logTime(denseTime, sparseTime, timesToRepeat);
    }

    @Test
    public void KTrans_x_Icc() {
        int timesToRepeat = 200_000;
        int numberOfCycles = 25;
        int numberOfEdges = 100;
        int numberOfEdgesInCycle = 10;
        CMatrixRMaj denseK = randomDenseKMatrix(numberOfCycles, numberOfEdges, numberOfEdgesInCycle);
        KMatrix sparseK = new KMatrix(numberOfCycles, numberOfEdges);
        copyElts(sparseK, denseK);
        CMatrixRMaj Icc = RandomMatrices_CDRM.rectangle(numberOfCycles, 1, ThreadLocalRandom.current());
        CMatrixRMaj expected = new CMatrixRMaj(numberOfEdges, 1);
        CMatrixRMaj actual = new CMatrixRMaj(numberOfEdges, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_CDRM.multTransA(denseK, Icc, expected), timesToRepeat);
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
        CMatrixRMaj denseZ = randomSymmetricalDenseMatrix(size, nonZeroEltFraction);
        ZMatrixAc sparseZ = new ZMatrixAc(size, strideEstimation);
        copyElts(sparseZ, denseZ);
        CMatrixRMaj J = randomDenseVector(size, size);
        CMatrixRMaj expected = new CMatrixRMaj(size, 1);
        CMatrixRMaj actual = new CMatrixRMaj(size, 1);
        double denseTime = measureTimeMillis(() -> CommonOps_CDRM.mult(denseZ, J, expected), timesToRepeat);
        double sparseTime = measureTimeMillis(() -> AcMatrixOps.mul(sparseZ, J, actual), timesToRepeat);
        assertArrayEquals(expected.data, actual.data, 0.5e-6f);
        logTime(denseTime, sparseTime, timesToRepeat);
    }

    private void copyElts(ZMatrixAc dest, CMatrixRMaj src) {
        for (int i = 0; i < src.numRows; i++) {
            for (int j = 0; j < src.numCols; j++) {
                float re = src.getReal(i, j);
                float im = src.getImag(i, j);
                dest.add(i, j, re, im);
            }
        }
    }

    private void copyElts(KMatrix dest, CMatrixRMaj src) {
        for (int i = 0; i < src.numRows; i++) {
            for (int j = 0; j < src.numCols; j++) {
                float re = src.getReal(i, j);
                dest.set(i, j, (int) re);
            }
        }
    }

    private void copyElts(VectorAc dest, CMatrixRMaj src) {
        for (int i = 0; i < src.numRows; i++) {
            float re = src.getReal(i, 0);
            float im = src.getImag(i, 0);
            if (re != 0 || im != 0) {
                dest.insert(i, re, im);
            }
        }
    }

    private CMatrixRMaj randomSymmetricalDenseMatrix(int size, double nonZeroEltFraction) {
        CMatrixRMaj m = new CMatrixRMaj(size, size);
        int n = (int) (nonZeroEltFraction * (size * size - size)) / 2;
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            float re = tlr.nextFloat();
            float im = tlr.nextFloat();
            m.set(i, i, re, im);
        }
        for (int i = 0; i < n; i++) {
            float re = tlr.nextFloat(-1, 1);
            float im = tlr.nextFloat(-1, 1);
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

    private CMatrixRMaj randomDenseKMatrix(int numRows, int numCols, int nonZeroEltsInRow) {
        Random r = ThreadLocalRandom.current();
        CMatrixRMaj m = new CMatrixRMaj(numRows, numCols);
        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < nonZeroEltsInRow; k++) {
                int j = r.nextInt(0, numCols);
                int v = r.nextBoolean() ? 1 : -1;
                m.set(i, j, v, 0);
            }
        }
        return m;
    }

    private CMatrixRMaj randomDenseVector(int numRows, int numNonZeroes) {
        CMatrixRMaj res = new CMatrixRMaj(numRows, 1);
        Random r = ThreadLocalRandom.current();
        int origin = -1;
        int bound = 1;
        if (numNonZeroes < numRows) { // разреженный
            for (int i = 0; i < numNonZeroes; i++) {
                int idx = r.nextInt(0, numRows);
                float re = r.nextFloat(origin, bound);
                float im = r.nextFloat(origin, bound);
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