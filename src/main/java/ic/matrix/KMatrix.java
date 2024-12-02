package ic.matrix;

import org.ejml.data.CMatrixRMaj;

public class KMatrix {

    final byte[][] data;
    final short[][] nzi;

    public KMatrix(int nRows, int nCols) {
        data = new byte[nRows][nCols];
        nzi = new short[nRows][nCols + 1];
    }

    public void set(int rowIdx, int colIdx, int value) {
        if (value != 0) {
            data[rowIdx][colIdx] = (byte) value;
            int idx = ++(nzi[rowIdx][0]);
            nzi[rowIdx][idx] = (short) colIdx;
        }
    }

    public CMatrixRMaj rMulT(CMatrixRMaj m) {
        CMatrixRMaj res = new CMatrixRMaj(data.length, data.length);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < m.numRows; j++) {
                float re = 0f;
                float im = 0f;
                int a = j * m.numCols;
                for (int k = 1; k < nzi[j][0]; k++) {
                    int idx = a + nzi[j][k] * 2;
                    re += m.data[idx];
                    im += m.data[idx + 1];
                }
                res.set(i, j, re, im);
            }
        }
        return res;
    }

    public CMatrixRMaj toCMatrixRMaj() {
        CMatrixRMaj m = new CMatrixRMaj(data.length, data[0].length);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                m.set(i, j, data[i][j], 0);
            }
        }
        return m;
    }
}