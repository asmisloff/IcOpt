package ic.matrix;

import org.ejml.data.ZMatrixRMaj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicComplexArray;

import java.util.Arrays;

public class AcMatrixOps {

    /**
     * Операция K&#215Z.
     * @param K    Матрица независимых контуров.
     * @param Z    Матрица сопротивлений.
     * @param dest Матрица для сохранения результата. Если <code>dest == null</code>, она будет создана.
     * @return <code>dest</code>
     */
    @NotNull
    public static ZMatrixRMaj KZ(
            @NotNull IMatrixCsr K,
            @NotNull ZMatrixAc Z,
            @Nullable ZMatrixRMaj dest
    ) {
        if (dest == null) {
            dest = new ZMatrixRMaj(K.numRows(), K.numCols());
        } else {
            int numberOfElements = K.numRows() * K.numCols();
            if (dest.numRows * dest.numCols >= numberOfElements) {
                Arrays.fill(dest.data, 0, numberOfElements * 2, 0);
            }
            dest.reshape(K.numRows(), K.numCols());
        }
        for (int i = 0, a = 0; i < K.numRows(); i++, a += K.numCols() * 2) {
            for (int j = K.begin(i); j < K.end(i); ++j) {
                int col = K.cols.get(j);
                int kElt = K.data.get(j);
                if (col < Z.getBlockEdgesQty()) {
                    int outIdx = a + col * 2;
                    dest.data[outIdx++] = kElt * Z.data.getRe(col);
                    dest.data[outIdx] = kElt * Z.data.getIm(col);
                } else {
                    for (int k = Z.begin(col); k < Z.end(col); k++) {
                        int outIdx = a + Z.cols.get(k) * 2;
                        dest.data[outIdx++] += kElt * Z.data.getRe(k);
                        dest.data[outIdx] += kElt * Z.data.getIm(k);
                    }
                }
            }
        }
        return dest;
    }


    /**
     * Операция KZ&#215K<sup>T</sup>.
     * @param KZ   Предварительно вычисленная матрица <code>KZ</code>.
     * @param K    Матрица независимых контуров.
     * @param dest Матрица для сохранения результата. Если <code>dest == null</code>, она будет создана.
     * @return <code>dest</code>
     */
    @NotNull
    public static ZMatrixRMaj KZKT(
            @NotNull ZMatrixRMaj KZ,
            @NotNull IMatrixCsr K,
            @Nullable ZMatrixRMaj dest
    ) {
        if (dest == null) {
            dest = new ZMatrixRMaj(KZ.numRows, KZ.numRows);
        } else {
            dest.reshape(KZ.numRows, KZ.numRows);
        }
        int kzStride = KZ.numCols * 2;
        int kzRowAnchor = 0;
        int destIdx = 0;
        /* Главная диагональ и верхний треугольник. */
        for (int i = 0; i < KZ.numRows; ++i) {
            destIdx += 2 * i;
            for (int j = i; j < K.numRows(); j++) {
                double re = 0f;
                double im = 0f;
                for (int k = K.begin(j); k < K.end(j); k++) {
                    int colIdx = K.cols.get(k);
                    int kzDataIdx = kzRowAnchor + colIdx * 2;
                    double kv = K.data.get(k);
                    re += KZ.data[kzDataIdx] * kv;
                    im += KZ.data[kzDataIdx + 1] * kv;
                }
                dest.data[destIdx++] = re;
                dest.data[destIdx++] = im;
            }
            kzRowAnchor += kzStride;
        }
        /* Отзеркалить верхний треугольник вниз. */
        for (int i = 0; i < K.numRows(); i++) {
            for (int j = i + 1; j < K.numRows(); j++) {
                int ij = 2 * (i * K.numRows() + j);
                int ji = 2 * (j * K.numRows() + i);
                dest.data[ji] = dest.data[ij];
                dest.data[ji + 1] = dest.data[ij + 1];
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
    public static DynamicComplexArray mult(
            @NotNull VectorAc E,
            @NotNull IMatrixCsr KT,
            @Nullable DynamicComplexArray dest
    ) {
        if (dest == null) {
            dest = new DynamicComplexArray(KT.numCols());
            dest.setSize(KT.numCols());
        } else {
            dest.setSize(KT.numCols());
            Arrays.fill(dest.getDataRe(), 0, dest.getSize(), 0);
            Arrays.fill(dest.getDataIm(), 0, dest.getSize(), 0);
        }
        double[] destRes = dest.getDataRe();
        double[] destIms = dest.getDataIm();
        for (int i = 0; i < E.nzi.getSize(); i++) {
            int r = E.nzi.get(i);
            double re = E.data.getRe(r);
            double im = E.data.getIm(r);
            for (int j = KT.begin(r); j < KT.end(r); j++) {
                int colIdx = KT.cols.get(j);
                int k = KT.data.get(j);
                destRes[colIdx] += k * re;
                destIms[colIdx] += k * im;
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
    public static DynamicComplexArray mult(
            @NotNull VectorAc I,
            @NotNull ZMatrixAc Z,
            @NotNull IMatrixCsr KT,
            @Nullable DynamicComplexArray dest
    ) {
        if (dest == null) {
            dest = new DynamicComplexArray(KT.numCols());
            dest.setSize(KT.numCols());
        } else {
            dest.setSize(KT.numCols());
            Arrays.fill(dest.getDataRe(), 0, KT.numCols(), 0);
            Arrays.fill(dest.getDataIm(), 0, KT.numCols(), 0);
        }
        double[] destRes = dest.getDataRe();
        double[] destIms = dest.getDataIm();
        for (int i = 0; i < I.nzi.getSize(); i++) {
            int r = I.nzi.get(i);
            double iRe = I.data.getRe(r);
            double iIm = I.data.getIm(r);
            double zRe = Z.data.getRe(r);
            double zIm = Z.data.getIm(r);
            double re = iRe * zRe - iIm * zIm;
            double im = iRe * zIm + iIm * zRe;
            for (int j = KT.begin(r); j < KT.end(r); j++) {
                int colIdx = KT.cols.get(j);
                int k = KT.data.get(j);
                destRes[colIdx] += k * re;
                destIms[colIdx] += k * im;
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
    public static ZMatrixRMaj sub(
            @NotNull DynamicComplexArray KE,
            @NotNull DynamicComplexArray KZI,
            @Nullable ZMatrixRMaj dest
    ) {
        if (dest == null) {
            dest = new ZMatrixRMaj(KE.getSize(), 1);
        } else {
            dest.reshape(KE.getSize(), 1);
        }
        for (int i = 0, destIdx = 0; i < KE.getSize(); ++i) {
            dest.data[destIdx++] = KE.getRe(i) - KZI.getRe(i);
            dest.data[destIdx++] = KE.getIm(i) - KZI.getIm(i);
        }
        return dest;
    }

    /**
     * Операция <code>K<sup>T</sup>&#215I<sub>cc</sub></code>
     * @param KT   Транспонированная матрица независимых контуров.
     * @param Icc  Вектор контурных токов.
     * @param dest Вектор для сохранения результата. Если <code>dest == null</code>, он будет создан.
     * @return <code>dest</code>
     */
    @NotNull
    public static ZMatrixRMaj mult(
            @NotNull IMatrixCsr KT,
            @NotNull ZMatrixRMaj Icc,
            @Nullable ZMatrixRMaj dest
    ) {
        if (dest == null) {
            dest = new ZMatrixRMaj(KT.numRows(), 1);
        } else {
            dest.reshape(KT.numRows(), 1);
        }
        for (int i = 0; i < KT.numRows(); i++) {
            double re = 0;
            double im = 0;
            for (int j = KT.begin(i); j < KT.end(i); j++) {
                int k = KT.data.get(j);
                int iccIdx = KT.cols.get(j) * 2;
                re += k * Icc.data[iccIdx];
                im += k * Icc.data[iccIdx + 1];
            }
            dest.set(i, 0, re, im);
        }
        return dest;
    }

    /**
     * Операция <code>Z&#215J</code>
     * @param Z  Матрица сопротивлений.
     * @param J  Вектор токов в ребрах.
     * @param dU Результирующий вектор падений напряжений на ребрах.
     * @return <code>dU</code>. Если <code>dU == null</code>, он будет создан.
     */
    @NotNull
    public static ZMatrixRMaj mult(
            @NotNull ZMatrixAc Z,
            @NotNull ZMatrixRMaj J,
            @Nullable ZMatrixRMaj dU
    ) {
        if (dU == null) {
            dU = new ZMatrixRMaj(Z.size(), 1);
        } else {
            dU.reshape(Z.size(), 1);
        }
        int duIdx = 0;
        for (int i = 0, j = 0; i < Z.getBlockEdgesQty(); i++) {
            double zRe = Z.data.getRe(i);
            double zIm = Z.data.getIm(i);
            double jRe = J.data[j++];
            double jIm = J.data[j++];
            double re = jRe * zRe - jIm * zIm;
            double im = jRe * zIm + jIm * zRe;
            dU.data[duIdx++] = re;
            dU.data[duIdx++] = im;
        }
        for (int i = Z.getBlockEdgesQty(); i < Z.size(); i++) {
            double re = 0;
            double im = 0;
            for (int j = Z.begin(i); j < Z.end(i); j++) {
                double zRe = Z.data.getRe(j);
                double zIm = Z.data.getIm(j);
                int jIdx = Z.cols.get(j) * 2;
                double jRe = J.data[jIdx];
                double jIm = J.data[jIdx + 1];
                re += (jRe * zRe - jIm * zIm);
                im += (jRe * zIm + jIm * zRe);
            }
            dU.data[duIdx++] = re;
            dU.data[duIdx++] = im;
        }
        return dU;
    }
}
