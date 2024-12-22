package ic.matrix;

import org.ejml.data.DMatrixRMaj;

import java.util.Arrays;

public class DcMatrixOps {

    static void KZ(IMatrixCsr K, ZMatrixDc Z, DMatrixCsr dest) {
        dest.rows.setSize(K.rows.getSize());
        dest.cols.setSize(K.cols.getSize());
        dest.data.setSize(K.data.getSize());
        System.arraycopy(K.rows.getData(), 0, dest.rows.getData(), 0, K.rows.getSize());
        System.arraycopy(K.cols.getData(), 0, dest.cols.getData(), 0, K.cols.getSize());
        for (int i = 0; i < K.cols.getSize(); i++) {
            double v = K.data.get(i) * Z.get(K.cols.get(i));
            dest.data.set(i, v);
        }
    }

    static void mult(IMatrixCsr K, ZMatrixDc Z, DMatrixRMaj A) {
        Arrays.fill(A.data, 0, A.data.length, 0);
        for (int i = 0; i < K.numRows(); i++) {
            for (int k = K.csrBegin(i); k < K.csrEnd(i); ++k) {
                int colIdx = K.cols.get(k);
                double z = Z.get(colIdx) * K.data.get(k);
                for (int j = i, ai = i * K.numRows() + i,
                     ki = i * K.numCols() + colIdx; j < K.numRows();
                     j++, ai++, ki += K.numCols()
                ) {
                    A.data[ai] += K.data.get(ki) * z;
                }
            }
            for (int j = i + 1; j < K.numRows(); j++) {
                A.set(j, i, A.get(i, j));
            }
        }
    }
}
