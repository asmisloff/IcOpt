package ic.matrix;

import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicComplexArray;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMs;
import static ic.matrix.util.IcMatrixTestHelper.randomDenseZMatrix;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AcMatrixOpsTest {

    private final int timesToRepeat = 20_000;
    private final int numberOfCycles = 30;
    private final int numberOfEdges = 100;
    private final int numberOfBlockEdges = numberOfEdges / 2;

    private final ZMatrixRMaj refZ = randomDenseZMatrix(numberOfBlockEdges, numberOfEdges, 12);
    private final ZMatrixAc Z = zMatrixAc(refZ);

    private final ZMatrixRMaj refK = randomDenseKMatrix(10);
    private final IMatrixCsr K = iMatrixCsr(refK);

    private final ZMatrixRMaj refE = randomDenseVector(3);
    private final VectorAc E = vectorAc(refE);

    private final ZMatrixRMaj refI = randomDenseVector(10);
    private final VectorAc I = vectorAc(refI);

    private final ZMatrixRMaj refKZ = new ZMatrixRMaj(refK);
    private ZMatrixRMaj KZ = new ZMatrixRMaj(refK);

    private final ZMatrixRMaj refKZKT = new ZMatrixRMaj(refK.numRows, refK.numRows);
    private ZMatrixRMaj KZKT = new ZMatrixRMaj(refKZKT);

    private final IMatrixCsr KT = DcMatrixOps.transpose(K, null);

    private final ZMatrixRMaj refKE = new ZMatrixRMaj(numberOfCycles, 1);
    private final DynamicComplexArray KE = AcMatrixOps.mult(E, KT, null);

    private final ZMatrixRMaj refKZI = new ZMatrixRMaj(numberOfCycles, 1);
    private final DynamicComplexArray KZI = AcMatrixOps.mult(I, Z, KT, null);

    private final ZMatrixRMaj Icc = RandomMatrices_ZDRM.rectangle(numberOfCycles, 1, ThreadLocalRandom.current());

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
        int timesToRepeat = 200_000;
        measureTimeMs("ref", timesToRepeat, () -> CommonOps_ZDRM.mult(refK, refE, refKE));
        measureTimeMs("act", timesToRepeat, () -> AcMatrixOps.mult(E, KT, KE));
        assertArrayEquals(refKE.data, rMaj(KE), 0.5e-6f);
    }

    @Test
    public void KZxI() {
        int timesToRepeat = 200_000;
        CommonOps_ZDRM.mult(refK, refZ, refKZ);
        measureTimeMs("ref", timesToRepeat, () -> CommonOps_ZDRM.mult(refKZ, refI, refKZI));
        measureTimeMs("act", timesToRepeat, () -> AcMatrixOps.mult(I, Z, KT, KZI));
        assertArrayEquals(refKZI.data, rMaj(KZI), 0.5e-6f);
    }

    @Test
    public void KE_minus_KZI() {
        int timesToRepeat = 2_000_000;
        CommonOps_ZDRM.mult(refK, refE, refKE);
        CommonOps_ZDRM.mult(refK, refZ, refKZ);
        CommonOps_ZDRM.mult(refKZ, refI, refKZI);
        ZMatrixRMaj refB = new ZMatrixRMaj(refKE);
        ZMatrixRMaj B = AcMatrixOps.sub(KE, KZI, null);
        assertArrayEquals(refKE.data, rMaj(KE), 0.5e-6);
        assertArrayEquals(refKZI.data, rMaj(KZI), 0.5e-6);
        measureTimeMs("ref", timesToRepeat, () -> CommonOps_ZDRM.subtract(refKE, refKZI, refB));
        measureTimeMs("act", timesToRepeat, () -> AcMatrixOps.sub(KE, KZI, B));
        assertArrayEquals(refB.data, B.data, 0.5e-6);
    }

    @Test
    public void J() {
        int timesToRepeat = 200_000;
        ZMatrixRMaj expected = new ZMatrixRMaj(numberOfEdges, 1);
        ZMatrixRMaj actual = new ZMatrixRMaj(numberOfEdges, 1);
        measureTimeMs("ref", timesToRepeat, () -> CommonOps_ZDRM.multTransA(refK, Icc, expected));
        measureTimeMs("act", timesToRepeat, () -> AcMatrixOps.mult(KT, Icc, actual));
        assertArrayEquals(expected.data, actual.data, 0.5e-6f);
    }

    @Test
    public void ZxJ() {
        int timesToRepeat = 200_000;
        ZMatrixRMaj J = AcMatrixOps.mult(KT, Icc, null);
        ZMatrixRMaj refDu = new ZMatrixRMaj(numberOfEdges, 1);
        ZMatrixRMaj dU = AcMatrixOps.mult(Z, J, null);
        measureTimeMs("ref", timesToRepeat, () -> CommonOps_ZDRM.mult(refZ, J, refDu));
        measureTimeMs("act", timesToRepeat, () -> AcMatrixOps.mult(Z, J, dU));
        assertArrayEquals(refDu.data, dU.data, 0.5e-6f);
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

    private ZMatrixAc zMatrixAc(ZMatrixRMaj M) {
        ZMatrixAc res = new ZMatrixAc(numberOfBlockEdges, numberOfEdges - numberOfBlockEdges);
        Complex_F64 z = new Complex_F64();
        for (int i = 0; i < M.numRows; i++) {
            for (int j = 0; j < M.numCols; j++) {
                M.get(i, j, z);
                if (z.real != 0 && z.imaginary != 0) {
                    res.insert(i, j, z.real, z.imaginary);
                }
            }
        }
        return res;
    }

    private VectorAc vectorAc(ZMatrixRMaj V) {
        VectorAc res = new VectorAc(V.numRows);
        for (int i = 0; i < V.numRows; i++) {
            double re = V.getReal(i, 0);
            double im = V.getImag(i, 0);
            if (re != 0 || im != 0) {
                res.insert(i, re, im);
            }
        }
        return res;
    }

    private ZMatrixRMaj randomDenseKMatrix(@SuppressWarnings("SameParameterValue") int nzCntPerRow) {
        Random r = ThreadLocalRandom.current();
        ZMatrixRMaj m = new ZMatrixRMaj(numberOfCycles, numberOfEdges);
        for (int i = 0; i < numberOfCycles; i++) {
            for (int k = 0; k < nzCntPerRow; k++) {
                int j = r.nextInt(0, numberOfEdges);
                int v = r.nextBoolean() ? 1 : -1;
                m.set(i, j, v, 0);
            }
        }
        return m;
    }

    private ZMatrixRMaj randomDenseVector(int nzCnt) {
        ZMatrixRMaj res = new ZMatrixRMaj(numberOfEdges, 1);
        Random r = ThreadLocalRandom.current();
        int origin = -1;
        int bound = 1;
        for (int i = 0; i < nzCnt; i++) {
            int idx = r.nextInt(0, numberOfBlockEdges);
            double re = r.nextFloat(origin, bound);
            double im = r.nextFloat(origin, bound);
            res.set(idx, 0, re, im);
        }
        return res;
    }

    private double[] rMaj(DynamicComplexArray a) {
        double[] res = new double[a.getSize() * 2];
        int idx = 0;
        for (int i = 0; i < a.getSize(); i++) {
            res[idx++] = a.getRe(i);
            res[idx++] = a.getIm(i);
        }
        return res;
    }
}