package ic.matrix;

import org.ejml.data.CMatrixRMaj;

public class KMatrix {

    final byte[][] data;
    final short[][] nzi;

    public KMatrix(int nRows, int nCols) {
        data = new byte[nRows][nCols];
        nzi = new short[nRows][nCols + 1];
    }

    public KMatrix(byte[][] data) {
        this.data = data;
        nzi = new short[numRows()][numCols() + 1];
        for (int r = 0; r < numRows(); r++) {
            short[] nziRow = nzi[r];
            byte[] dataRow = data[r];
            for (short c = 0; c < numCols(); c++) {
                if (dataRow[c] != 0) {
                    nziRow[++nziRow[0]] = c;
                }
            }
        }
    }

    public int numRows() {
        return data.length;
    }

    public int numCols() {
        return data[0].length;
    }

    public void set(int rowIdx, int colIdx, int value) {
        if (value != 0) {
            data[rowIdx][colIdx] = (byte) value;
            int idx = ++(nzi[rowIdx][0]);
            nzi[rowIdx][idx] = (short) colIdx;
        }
    }

    public CMatrixRMaj mulTransR(CMatrixRMaj m) {
        CMatrixRMaj res = new CMatrixRMaj(m.numRows, this.numRows());
        mulTransR(m, res);
        return res;
    }

    public void mulTransR(CMatrixRMaj m, CMatrixRMaj res) {
        int stride = m.numCols * 2;
        for (int r = 0, idx = 0, anchor = 0;
             r < m.numRows;
             ++r, anchor += stride
        ) {
            for (int c = 0; c < this.numRows(); c++) {
                float re = 0f;
                float im = 0f;
                byte[] kRow = data[c];
                short[] nzis = nzi[c];
                short nonZeroEltQty = nzis[0];
                for (int k = 1; k <= nonZeroEltQty; k++) {
                    int colIdx = nzis[k];
                    int mDataIdx = anchor + colIdx * 2;
                    if (kRow[colIdx] == 1) {
                        re += m.data[mDataIdx];
                        im += m.data[mDataIdx + 1];
                    } else { // -1
                        re -= m.data[mDataIdx];
                        im -= m.data[mDataIdx + 1];
                    }
                }
                res.data[idx++] = re;
                res.data[idx++] = im;
            }
        }
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