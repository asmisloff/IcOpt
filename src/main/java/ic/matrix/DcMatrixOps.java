package ic.matrix;

import org.ejml.data.DMatrixRMaj;

import java.util.Arrays;

public class DcMatrixOps {

    static void mult(KMatrixCsr K, ZMatrixDc Z, DMatrixRMaj A) {
        Arrays.fill(A.data, 0, A.data.length, 0);
        for (int i = 0; i < K.numRows(); i++) {
            int begin = K.csrBegin(i);
            int end = K.csrEnd(i);
            for (int c = begin; c < end; ++c) {
                int colIdx = K.cols.get(c);
                double z = Z.get(colIdx) * K.get(i, colIdx);
                for (int j = i, ai = i * K.numRows() + i,
                     ki = i * K.numCols() + colIdx; j < K.numRows();
                     j++, ai++, ki += K.numCols()
                ) {
                    A.data[ai] += K.denseData.get(ki) * z;
                }
            }
            for (int j = i + 1; j < K.numRows(); j++) {
                A.set(j, i, A.get(i, j));
            }
        }
    }
}
