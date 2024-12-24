package ic.matrix;

import org.ejml.data.DMatrixRMaj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicDoubleArray;

import java.util.Arrays;

/** Операции над матрицами решателя схем МПЗ постоянного тока. */
public class DcMatrixOps {

    /**
     * Транспонировать матрицу <code>src</code>, результат сохранить в <code>dest</code>. Важное свойство алгоритма:
     * индексы ненулевых столбцов в <code>dest</code> окажутся упорядочены по возрастанию.
     * <p>Если <code>dest == null</code>, она будет создана.</p>
     * @return <code>dest</code>.
     */
    @NotNull
    public static IMatrixCsr transpose(
            @NotNull IMatrixCsr src,
            @Nullable IMatrixCsr dest
    ) {
        if (dest == null) {
            dest = new IMatrixCsr(src.numRows());
        } else {
            dest.reset(src.numRows());
        }
        dest.cols.setSize(src.cols.getSize());
        dest.data.setSize(src.data.getSize());
        dest.rows.setSize(src.numCols() + 1);
        int[] destRowsData = dest.rows.getData();
        Arrays.fill(destRowsData, 0, dest.rows.getSize(), 0);
        for (int i = 0; i < src.cols.getSize(); i++) {
            destRowsData[src.cols.get(i) + 1]++;
        }
        int cumSum = 0;
        int tmp;
        for (int i = 1; i <= src.numCols(); i++) {
            tmp = destRowsData[i];
            destRowsData[i] = cumSum;
            cumSum += tmp;
        }
        for (int i = 0; i < src.numRows(); i++) {
            for (int j = src.begin(i); j < src.end(i); j++) {
                int v = src.data.get(j);
                int destRowIdx = src.cols.get(j);
                int destIdx = destRowsData[destRowIdx + 1];
                dest.data.set(destIdx, v);
                dest.cols.set(destIdx, i);
                destRowsData[destRowIdx + 1]++;
            }
        }
        return dest;
    }

    /**
     * K&#215Z&#215K<sup>T</sup>.
     * @param K    Матрица независимых контуров.
     * @param Z    Матрица сопротивлений.
     * @param KT   Транспонированная матрица независимых контуров K<sup>T</sup>. Индексы ненулевых столбцов, относящиеся
     *             к одной строке, должны быть отсортированы по возрастанию.
     * @param dest Матрица для сохранения результата. Если <code>dest == null</code>, она будет создана.
     * @return <code>dest</code>
     */
    @NotNull
    public static DMatrixRMaj KZKT(
            @NotNull IMatrixCsr K,
            @NotNull ZMatrixDc Z,
            @NotNull IMatrixCsr KT,
            @Nullable DMatrixRMaj dest
    ) {
        if (dest == null) {
            dest = new DMatrixRMaj(K.numRows(), K.numRows());
        } else {
            dest.reshape(K.numRows(), K.numRows());
            dest.zero();
        }
        /* Главная диагональ и нижний треугольник. */
        for (int i = 0, a = 0; i < K.numRows(); i++, a += dest.numCols) {
            double d = 0;
            for (int j = K.begin(i); j < K.end(i); j++) {
                int kColIdx = K.cols.get(j);
                double z = Z.get(kColIdx);
                double kz = K.data.get(j) * z;
                d += z; /* Диагональный элемент есть сумма сопротивлений всех ребер, входящих в контур. Его можно вычислить
                без умножений и без обращения к матрице KT. */
                for (int l = KT.begin(kColIdx); l < KT.end(kColIdx); l++) {
                    int destColIdx = KT.cols.get(l);
                    if (destColIdx >= i) { /* В силу симметрии результирующей матрицы (dest), а также потому, что индексы
                     * ненулевых столбцов в KT следуют по возрастанию, внутренний цикл можно не доводить до конца. */
                        break;
                    }
                    int kt = KT.data.get(l);
                    dest.data[a + destColIdx] += kt * kz;
                }
            }
            dest.data[a + i] = d;
        }
        /* Отзеркалить нижний треугольник наверх. */
        int n = dest.numRows;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                int ij = i * n + j;
                int ji = j * n + i;
                dest.data[ji] = dest.data[ij];
            }
        }
        return dest;
    }

    /**
     * Операция K&#215E. Для эффективности порядок операндов изменен, и фактически вычисляется E<sup>T</sup>&#215K<sup>T</sup>.
     * @param E    Вектор ЭДС.
     * @param KT   Транспонированная матрица контуров K<sup>T</sup>.
     * @param dest Вектор для сохранения результата. Если <code>dest == null</code>, он будет создан.
     * @return <code>dest</code>.
     */
    @NotNull
    public static DynamicDoubleArray mult(
            @NotNull VectorDc E,
            @NotNull IMatrixCsr KT,
            @Nullable DynamicDoubleArray dest
    ) {
        if (dest == null) {
            dest = new DynamicDoubleArray(KT.numCols());
            dest.setSize(KT.numCols());
        } else {
            dest.setSize(KT.numCols());
            Arrays.fill(dest.getData(), 0, dest.getSize(), 0);
        }
        double[] kvData = dest.getData();
        for (int i = 0; i < E.nzi.getSize(); i++) {
            int r = E.nzi.get(i);
            double v = E.data.get(r);
            for (int j = KT.begin(r); j < KT.end(r); j++) {
                int colIdx = KT.cols.get(j);
                kvData[colIdx] += KT.data.get(j) * v;
            }
        }
        return dest;
    }

    /**
     * Операция <code>K&#215Z&#215I</code>.
     * Для эффективности порядок операндов изменен, и фактически вычисляется <code>I<sup>T</sup>&#215Z&#215K<sup>T</sup></code>.
     * @param I    Вектор задающих токов.
     * @param Z    Матрица сопротивлений.
     * @param KT   Транспонированная матрица контуров K<sup>T</sup>.
     * @param dest Вектор для сохранения результата. Если <code>dest == null</code>, он будет создан.
     * @return <code>dest</code>.
     */
    @NotNull
    public static DynamicDoubleArray mult(
            @NotNull VectorDc I,
            @NotNull ZMatrixDc Z,
            @NotNull IMatrixCsr KT,
            @Nullable DynamicDoubleArray dest
    ) {
        if (dest == null) {
            dest = new DynamicDoubleArray(KT.numCols());
            dest.setSize(KT.numCols());
        } else {
            dest.setSize(KT.numCols());
            Arrays.fill(dest.getData(), 0, KT.numCols(), 0);
        }
        double[] destData = dest.getData();
        for (int i = 0; i < I.nzi.getSize(); i++) {
            int r = I.nzi.get(i);
            double v = I.data.get(r) * Z.get(r);
            for (int j = KT.begin(r); j < KT.end(r); j++) {
                int colIdx = KT.cols.get(j);
                destData[colIdx] += KT.data.get(j) * v;
            }
        }
        return dest;
    }

    /**
     * Операция <code>KE + KZI</code>.
     * @param dest Вектор для сохранения результата. Если <code>dest == null</code>, он будет создан.
     * @return <code>dest</code>.
     */
    @NotNull
    public static DMatrixRMaj add(
            @NotNull DynamicDoubleArray KE,
            @NotNull DynamicDoubleArray KZI,
            @Nullable DMatrixRMaj dest
    ) {
        if (dest == null) {
            dest = new DMatrixRMaj(KE.getSize(), 1);
        }
        for (int i = 0; i < KE.getSize(); i++) {
            dest.data[i] = KE.get(i) + KZI.get(i);
        }
        return dest;
    }
}
