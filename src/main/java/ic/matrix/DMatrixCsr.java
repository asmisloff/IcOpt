package ic.matrix;

import ru.vniizht.asuterkortes.counter.latticemodel.DynamicDoubleArray;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

public class DMatrixCsr {

    public final DynamicDoubleArray data;
    public final DynamicIntArray rows;
    public final DynamicIntArray cols;
    private int numCols;

    public DMatrixCsr(int numCols) {
        this.numCols = numCols;
        int numRows = 12;
        data = new DynamicDoubleArray(numCols * numRows);
        rows = new DynamicIntArray(numRows);
        cols = new DynamicIntArray(numCols / 4 * numRows);
        rows.append(0);
    }

    public void reset(int numCols) {
        this.numCols = numCols;
        data.setSize(0);
        rows.setSize(1);
        rows.set(0, 0);
        cols.setSize(0);
    }

    void addRow() {
        rows.append(last(rows));
    }

    public int numRows() {
        return rows.getSize() - 1;
    }

    public int numCols() {
        return numCols;
    }

    public int nzCnt() {
        return cols.getSize();
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
        data.append(value);
    }

    public double get(int i, int j) {
        for (int k = csrBegin(i); k < csrEnd(i); k++) {
            if (cols.get(k) == j) {
                return data.get(k);
            }
        }
        return 0;
    }

    public void print() {
        for (int i = 0, cnt = 0; i < data.getSize(); i++, ++cnt) {
            if (cnt == numCols) {
                cnt = 0;
                System.out.println();
            }
            System.out.printf("%.2f  ", data.get(i));
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
