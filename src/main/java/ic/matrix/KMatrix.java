package ic.matrix;

public class KMatrix {

    final double[][] data;
    final int[][] nzi;

    public KMatrix(int nRows, int nCols) {
        data = new double[nRows][nCols];
        nzi = new int[nRows][nCols + 1];
    }

    public KMatrix(double[][] data) {
        this.data = data;
        nzi = new int[numRows()][numCols() + 1];
        for (int r = 0; r < numRows(); r++) {
            int[] nziRow = nzi[r];
            double[] dataRow = data[r];
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