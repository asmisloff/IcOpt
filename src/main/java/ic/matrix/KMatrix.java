package ic.matrix;

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
}