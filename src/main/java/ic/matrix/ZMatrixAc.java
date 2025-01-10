package ic.matrix;

import org.ejml.data.Complex_F64;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicComplexArray;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

public class ZMatrixAc {

    public final DynamicComplexArray data;
    public final DynamicIntArray cols;
    private final DynamicIntArray begins;
    private final DynamicIntArray ends;
    private int blockEdgesQty;
    private int wiringEdgesQty;

    /**
     * @param blockEdgesQty  Количество ребер графа схемы, не связанных взаимоиндукцией (внутренние ребра блоков).
     * @param wiringEdgesQty Количество ребер графа схемы, потенциально связанных взаимоиндукцией (ребра тяговых сетей).
     */
    public ZMatrixAc(int blockEdgesQty, int wiringEdgesQty) {
        this.blockEdgesQty = blockEdgesQty;
        this.wiringEdgesQty = wiringEdgesQty;
        int totalEdgeQty = blockEdgesQty + wiringEdgesQty;
        begins = new DynamicIntArray(totalEdgeQty);
        ends = new DynamicIntArray(totalEdgeQty);
        int dataSize = blockEdgesQty + wiringEdgesQty * wiringEdgesQty;
        data = new DynamicComplexArray(dataSize);
        cols = new DynamicIntArray(dataSize);
        initArrays();
    }

    /**
     * Количество строк (столбцов) матрицы.
     */
    public int size() {
        return blockEdgesQty + wiringEdgesQty;
    }

    /**
     * Индекс в массивах <code>cols</code> и <code>data</code>, соответствующий началу <code>rowIdx</code>-й строки.
     */
    public int begin(int rowIdx) {
        return begins.get(rowIdx);
    }

    /**
     * Индекс в массивах <code>cols</code> и <code>data</code>, соответствующий началу <code>(rowIdx + 1)</code>-й строки.
     */
    public int end(int rowIdx) {
        return ends.get(rowIdx);
    }

    /**
     * Повторно инициализировать внутренние массивы.
     * @param blockEdgesQty  Количество ребер графа схемы, не связанных взаимоиндукцией (внутренние ребра блоков).
     * @param wiringEdgesQty Количество ребер графа схемы, потенциально связанных взаимоиндукцией (ребра тяговых сетей).
     */
    public void reset(int blockEdgesQty, int wiringEdgesQty) {
        this.blockEdgesQty = blockEdgesQty;
        this.wiringEdgesQty = wiringEdgesQty;
        initArrays();
    }

    /**
     * Добавить ненулевой элемент в <code>colIdx</code>-й столбец текущей строки.
     * <p>
     * Вызывающий код должен гарантировать следующее.
     *  <ul>
     *      <li>Элемент с парой индексов (i, j) добавляется лишь единожды.</li>
     *      <li>Ребра с номерами <code>[0, blockEdgesQty)</code> не участвуют в индуктивном взаимодействии, и соответствующие
     *      им сопротивления расположены исключительно на главной диагонали.</li>
     *  </ul>
     * </p>
     */
    public void insert(int rowIdx, int colIdx, double re, double im) {
        int dataIdx = ends.getData()[rowIdx]++;
        cols.set(dataIdx, colIdx);
        data.set(dataIdx, re, im);
    }

    /**
     * Возвращает значение элемента с координатами <code>(i, j)</code>.
     * <p>Только для тестов и отладки (<code>O(n)</code>).</p>
     */
    public void get(int i, int j, Complex_F64 dest) {
        for (int k = begin(i); k < end(i); k++) {
            if (cols.get(k) == j) {
                data.get(k, dest);
                return;
            }
        }
        dest.setTo(0, 0);
    }

    /**
     * Напечатать плотное представление в System.out.
     */
    public void print() {
        Complex_F64 z = new Complex_F64();
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                get(i, j, z);
                System.out.printf("(%5.2f; %5.2f) ", z.real, z.imaginary);
            }
            System.out.println();
        }
    }

    public int getBlockEdgesQty() {
        return blockEdgesQty;
    }

    private void initArrays() {
        int totalEdgeQty = size();
        begins.setSize(totalEdgeQty);
        for (int i = 0; i < blockEdgesQty; i++) {
            begins.set(i, i);
        }
        begins.set(blockEdgesQty, blockEdgesQty);
        for (int i = blockEdgesQty + 1; i < totalEdgeQty; i++) {
            int rowStartIdx = begins.get(i - 1) + wiringEdgesQty;
            begins.set(i, rowStartIdx);
        }
        ends.setSize(totalEdgeQty);
        System.arraycopy(begins.getData(), 0, ends.getData(), 0, totalEdgeQty);
        int dataSize = blockEdgesQty + wiringEdgesQty * wiringEdgesQty;
        data.setSize(dataSize);
        cols.setSize(dataSize);
    }
}
