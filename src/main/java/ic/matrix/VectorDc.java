package ic.matrix;

import ru.vniizht.asuterkortes.counter.latticemodel.DynamicDoubleArray;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

import java.util.Arrays;

/** Вектор действительных чисел для решателя схем МПЗ на постоянном токе. */
public class VectorDc {

    /** Элементы вектора в "плотной" форме. */
    public final DynamicDoubleArray data;

    /** Индексы ненулевых элементов. */
    public final DynamicIntArray nzi;

    public VectorDc(int size) {
        data = new DynamicDoubleArray(size);
        data.setSize(size);
        nzi = new DynamicIntArray(16);
    }

    /**
     * Вставить ненулевой элемент <code>value</code> на позицию <code>idx</code>.
     * Повторная вставка приведет к неправильной работе - ответственность на вызывающей стороне, здесь нет проверок.
     */
    public void insert(int idx, double value) {
        data.set(idx, value);
        nzi.append(idx);
    }

    /** Повторная инициализация. */
    public void reset(int size) {
        data.setSize(size);
        Arrays.fill(data.getData(), 0, size, 0);
        nzi.setSize(0);
    }
}
