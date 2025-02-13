package ic.matrix;

import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

/** Разреженная матрица целых чисел в формате CSR. */
public class IMatrixCsr {

    public final DynamicIntArray data;
    public final DynamicIntArray rows;
    public final DynamicIntArray cols;
    private int numCols;

    /**
     * @param numCols Количество столбцов.
     */
    public IMatrixCsr(int numCols) {
        this.numCols = numCols;
        int numRows = 12;
        rows = new DynamicIntArray(numRows);
        data = new DynamicIntArray(numCols / 4 * numRows);
        cols = new DynamicIntArray(numCols / 4 * numRows);
        rows.append(0);
    }

    /** Количество строк. */
    public int numRows() {
        return rows.getSize() - 1;
    }

    /** Количество столбцов. */
    public int numCols() {
        return numCols;
    }

    /** Количество ненулевых элементов. */
    public int nzCnt() {
        return cols.getSize();
    }

    /** Индекс в массивах <code>cols</code> и <code>data</code>, соответствующий началу <code>rowIdx</code>-й строки. */
    public int begin(int rowIdx) {
        return rows.get(rowIdx);
    }

    /** Индекс в массивах <code>cols</code> и <code>data</code>, соответствующий началу <code>(rowIdx + 1)</code>-й строки. */
    public int end(int rowIdx) {
        return rows.get(rowIdx + 1);
    }

    /** Сбросить состояние к "пустому": 0 строк, <code>numCols</code> столбцов. */
    public void reset(int numCols) {
        this.numCols = numCols;
        data.setSize(0);
        rows.setSize(1);
        rows.set(0, 0);
        cols.setSize(0);
    }

    /** Завершить формирование текущей строки и начать переключиться в режим формирования следующей строки. */
    public void addRow() {
        rows.append(last(rows));
    }

    /**
     * Добавить ненулевой элемент в <code>colIdx</code>-й столбец текущей строки.
     * Контроль за отсутствием дубликатов и за тем, в самом ли деле <code>value = 0</code>, - ответственность вызывающего
     * кода. Здесь нет проверок.
     */
    public void append(int colIdx, int value) {
        incLast(rows, 1);
        cols.append(colIdx);
        data.append(value);
    }

    /**
     * Возвращает значение элемента с координатами <code>(i, j)</code>.
     * <p>Только для тестов и отладки (<code>O(n)</code>).</p>
     */
    public int get(int i, int j) {
        for (int k = begin(i); k < end(i); k++) {
            if (cols.get(k) == j) {
                return data.get(k);
            }
        }
        return 0;
    }

    /** Напечатать плотное представление в System.out. */
    public void print() {
        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numCols(); j++) {
                System.out.printf("%3d ", get(i, j));
            }
            System.out.println();
        }
    }

    /** Последний элемент массива. */
    public int last(DynamicIntArray arr) {
        return arr.get(arr.getSize() - 1);
    }

    /** Увеличить последний элемент массива на <code>amount</code>. */
    public void incLast(DynamicIntArray arr, int amount) {
        int[] data = arr.getData();
        data[arr.getSize() - 1] += amount;
    }
}
