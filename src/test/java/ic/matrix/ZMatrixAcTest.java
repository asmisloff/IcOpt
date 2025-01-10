package ic.matrix;

import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static ic.matrix.util.IcMatrixTestHelper.randomDenseZMatrix;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ZMatrixAcTest {

    private final int numberOfBlockEdges = 30;
    private final int totalNumberOfEdges = 50;

    @Test
    void reset() {
        ZMatrixAc Z = null;
        for (int nzCntPerRow = 1; nzCntPerRow <= wiringEdgesQty(); nzCntPerRow++) {
            ZMatrixRMaj refZ = randomDenseZMatrix(numberOfBlockEdges, totalNumberOfEdges, nzCntPerRow);
            Z = zMatrixAc(refZ, Z);
            assertArrayEquals(refZ.data, rMaj(Z), 0.5e-6);
        }
    }

    private double[] rMaj(ZMatrixAc Z) {
        int sz = Z.size();
        double[] res = new double[2 * sz * sz];
        Complex_F64 buf = new Complex_F64();
        int resIdx = 0;
        for (int i = 0; i < sz; i++) {
            for (int j = 0; j < sz; j++) {
                Z.get(i, j, buf);
                res[resIdx++] = buf.real;
                res[resIdx++] = buf.imaginary;
            }
        }
        return res;
    }

    @NotNull
    private ZMatrixAc zMatrixAc(@NotNull ZMatrixRMaj Z, @Nullable ZMatrixAc dest) {
        if (dest == null) {
            dest = new ZMatrixAc(numberOfBlockEdges, wiringEdgesQty());
        } else {
            dest.reset(numberOfBlockEdges, wiringEdgesQty());
        }
        for (int i = 0; i < totalNumberOfEdges; i++) {
            for (int j = 0; j < totalNumberOfEdges; j++) {
                double re = Z.getReal(i, j);
                double im = Z.getImag(i, j);
                if (re != 0 || im != 0) {
                    dest.insert(i, j, re, im);
                }
            }
        }
        return dest;
    }

    private int wiringEdgesQty() {
        return totalNumberOfEdges - numberOfBlockEdges;
    }
}