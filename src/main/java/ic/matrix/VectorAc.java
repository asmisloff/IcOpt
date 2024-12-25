package ic.matrix;

import ru.vniizht.asuterkortes.counter.latticemodel.DynamicComplexArray;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

import java.util.Arrays;

public class VectorAc {

    /** Элементы вектора в "плотной" форме. */
    public final DynamicComplexArray data;

    /** Индексы ненулевых элементов. */
    public final DynamicIntArray nzi;

    public VectorAc(int size) {
        data = new DynamicComplexArray(size);
        data.setSize(size);
        nzi = new DynamicIntArray(16);
    }

    public void insert(int idx, double re, double im) {
        data.set(idx, re, im);
        nzi.append(idx);
    }

    /** Повторная инициализация. */
    public void reset(int size) {
        data.setSize(size);
        Arrays.fill(data.getDataRe(), 0, size, 0);
        Arrays.fill(data.getDataIm(), 0, size, 0);
        nzi.setSize(0);
    }
}
