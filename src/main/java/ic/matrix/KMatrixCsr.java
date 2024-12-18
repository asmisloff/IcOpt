package ic.matrix;

import ru.vniizht.asuterkortes.counter.latticemodel.DynamicDoubleArray;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

import java.util.Arrays;

public class KMatrixCsr {

    public final DynamicDoubleArray denseData;
    public final DynamicIntArray rows;
    public final DynamicIntArray cols;
    private int numCols;

    public KMatrixCsr(int numCols) {
        this.numCols = numCols;
        int numRows = 12;
        denseData = new DynamicDoubleArray(numCols * numRows);
        rows = new DynamicIntArray(numRows);
        rows.append(0);
        cols = new DynamicIntArray(numCols / 4 * numRows);
    }

    public void reset(int numCols) {
        this.numCols = numCols;
        Arrays.fill(denseData.getData(), 0);
        denseData.setSize(0);
        Arrays.fill(rows.getData(), 0);
        rows.setSize(1);
        Arrays.fill(cols.getData(), 0);
        cols.setSize(0);
    }

    void addRow() {
        int prevDenseSize = denseData.getSize();
        int newDenseSize = prevDenseSize + numCols;
        denseData.setSize(newDenseSize);
        rows.append(last(rows));
    }

    public int numRows() {
        return rows.getSize() - 1;
    }

    public int lastRowIdx() {
        return rows.getSize() - 2;
    }

    public int numCols() {
        return numCols;
    }

    public double get(int rowIdx, int colIdx) {
        return denseData.get(rowIdx * numCols + colIdx);
    }

    public int anchor(int rowIdx) {
        return rowIdx * numCols;
    }

    public int csrBegin(int rowIdx) {
        return rows.get(rowIdx);
    }

    public int csrEnd(int rowIdx) {
        return rows.get(rowIdx + 1);
    }

    public void append(int colIdx, double value) {
        incLast(rows);
        cols.append(colIdx);
        denseData.set(lastRowIdx() * numCols + colIdx, value);
    }

    public void print() {
        for (int i = 0, cnt = 0; i < denseData.getSize(); i++, ++cnt) {
            if (cnt == numCols) {
                cnt = 0;
                System.out.println();
            }
            System.out.printf("%.0f  ", denseData.get(i));
        }
        System.out.println();
    }

    private int last(DynamicIntArray arr) {
        return arr.get(arr.getSize() - 1);
    }

    private void incLast(DynamicIntArray arr) {
        int[] data = arr.getData();
        ++data[arr.getSize() - 1];
    }
}
