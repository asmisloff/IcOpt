package ic.matrix;

import org.ejml.data.ZMatrixRMaj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicComplexArray;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

import java.util.Arrays;

import static java.lang.System.nanoTime;

public class AcMatrixOps {

    public static long t1 = 0;
    public static long t2 = 0;

    public static ZMatrixRMaj KZKT(
            @NotNull IMatrixCsr K,
            @NotNull ZMatrixCsr Z,
            @NotNull IMatrixCsr KT,
            @NotNull DynamicComplexArray tmpv,
            @NotNull DynamicIntArray tmpc,
            @NotNull DynamicIntArray tmpi,
            @Nullable ZMatrixRMaj dest
    ) {
        if (dest == null) {
            dest = new ZMatrixRMaj(K.numRows(), K.numRows());
        } else {
            dest.reshape(K.numRows(), K.numRows());
            dest.zero();
        }
        tmpv.setSize(Z.numCols());
        tmpc.setSize(Z.numCols());
        tmpi.setSize(Z.numCols());
        Arrays.fill(tmpi.getData(), 0, tmpi.getSize(), -1);
        int nzCnt;
        for (int i = 0, a = 0; i < K.numRows(); i++, a += 2 * K.numRows()) {
            nzCnt = 0;
            long start = nanoTime();
            for (int j = K.begin(i); j < K.end(i); j++) {
                int colIdx = K.cols.get(j);
                int k = K.data.get(j);
                for (int l = Z.begin(colIdx); l < Z.end(colIdx); l++) {
                    double re = Z.data.getRe(l) * k;
                    double im = Z.data.getIm(l) * k;
                    int idx = Z.cols.get(l);
                    if (tmpi.get(idx) < i) {
                        tmpi.set(idx, i);
                        tmpc.set(nzCnt++, idx);
                        tmpv.set(idx, re, im);
                    } else {
                        tmpv.getDataRe()[idx] += re;
                        tmpv.getDataIm()[idx] += im;
                    }
                }
            }
            long start2 = nanoTime();
            t1 += start2 - start;
            for (int j = 0; j < nzCnt; j++) {
                int idx = tmpc.get(j);
                double re = tmpv.getRe(idx);
                double im = tmpv.getIm(idx);
                for (int m = KT.begin(idx); m < KT.end(idx); m++) {
                    int c = KT.cols.get(m);
                    if (c > i) {
                        break;
                    }
                    int k = KT.data.get(m);
                    int l = a + 2 * c;
                    dest.data[l] += re * k;
                    dest.data[l + 1] += im * k;
                }
            }
            t2 += nanoTime() - start2;
        }
        for (int i = 0; i < K.numRows(); i++) {
            for (int j = 0; j < i; j++) {
                int ij = 2 * (i * K.numRows() + j);
                int ji = 2 * (j * K.numRows() + i);
                dest.data[ji] = dest.data[ij];
                dest.data[ji + 1] = dest.data[ij + 1];
            }
        }
        return dest;
    }

    public static ZMatrixCsr transpose(@NotNull ZMatrixCsr src, @Nullable ZMatrixCsr dest) {
        if (dest == null) {
            dest = new ZMatrixCsr(src.numRows());
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
                double re = src.data.getRe(j);
                double im = src.data.getIm(j);
                int destRowIdx = src.cols.get(j);
                int destIdx = destRowsData[destRowIdx + 1];
                dest.data.set(destIdx, re, im);
                dest.cols.set(destIdx, i);
                destRowsData[destRowIdx + 1]++;
            }
        }
        return dest;
    }

    public static void mul(KMatrix K, ZMatrixAc Z, ZMatrixRMaj dest) {
        for (int k = 0; k < K.data.length; k++) {
            double[] kRow = K.data[k];
            for (int z = 0; z < Z.size(); z++) {
                double real = 0f;
                double imag = 0f;
                for (int idx = Z.begins[z]; idx < Z.ends[z]; idx++) {
                    int col = Z.cols[idx];
                    double re = Z.res[idx];
                    double im = Z.ims[idx];
                    double kv = kRow[col];
                    real += re * kv;
                    imag += im * kv;
                }
                dest.set(k, z, real, imag);
            }
        }
    }

    public static void KZ(IMatrixCsr K, ZMatrixCsr Z, ZMatrixRMaj dest) {
        int numberOfElements = K.numRows() * K.numCols();
        if (dest.numRows * dest.numCols >= numberOfElements) {
            Arrays.fill(dest.data, 0, numberOfElements * 2, 0);
        }
        dest.reshape(K.numRows(), K.numCols());
        for (int i = 0, a = 0; i < K.numRows(); i++, a += K.numCols() * 2) {
            for (int j = K.begin(i); j < K.end(i); ++j) {
                int col = K.cols.get(j);
                int kElt = K.data.get(j);
                for (int k = Z.begin(col); k < Z.end(col); k++) {
                    int outIdx = a + Z.cols.get(k) * 2;
                    dest.data[outIdx++] += kElt * Z.data.getRe(k);
                    dest.data[outIdx] += kElt * Z.data.getIm(k);
                }
            }
        }
    }

    /**
     * Операция K&#215Z.
     *
     * @param K    Матрица независимых контуров.
     * @param Z    Мастрица сопротивлений.
     * @param tmpi Массив для промежуточных данных.
     * @param tmpz Массив для промежуточных данных.
     * @param dest Матрица для сохранения результата. Если <code>dest == null</code>, она будет создана.
     * @return <code>dest</code>.
     */
    public static ZMatrixCsr KZ(
            @NotNull IMatrixCsr K,
            @NotNull ZMatrixCsr Z,
            @NotNull DynamicIntArray tmpi,
            @NotNull DynamicComplexArray tmpz,
            @Nullable ZMatrixCsr dest
    ) {
        if (dest == null) {
            dest = new ZMatrixCsr(K.numCols());
        } else {
            dest.reset(K.numCols());
        }
        tmpi.setSize(K.numCols());
        tmpz.setSize(K.numCols());
        Arrays.fill(tmpi.getData(), 0, tmpi.getSize(), -1);
        dest.cols.ensureCapacity(K.numRows() * K.numCols());
        dest.data.ensureCapacity(K.numRows() * K.numCols());
        dest.rows.setSize(K.numRows() + 1);
        int nzCnt = 0;
        for (int i = 0; i < K.numRows(); i++) {
            int r = i + 1;
            for (int j = K.begin(i); j < K.end(i); ++j) {
                int zRowIdx = K.cols.get(j);
                int kElt = K.data.get(j);
                for (int k = Z.begin(zRowIdx); k < Z.end(zRowIdx); k++) {
                    int zColIdx = Z.cols.get(k);
                    double re = Z.data.getRe(k) * kElt;
                    double im = Z.data.getIm(k) * kElt;
                    if (tmpi.get(zColIdx) < i) {
                        tmpi.set(zColIdx, i);
                        dest.cols.set(nzCnt++, zColIdx);
                        tmpz.set(zColIdx, re, im);
                    } else {
                        tmpz.getDataRe()[zColIdx] += re;
                        tmpz.getDataIm()[zColIdx] += im;
                    }
                }
            }
            dest.rows.set(r, nzCnt);
            for (int j = dest.begin(i); j < dest.end(i); j++) {
                int colIdx = dest.cols.get(j);
                dest.data.set(j, tmpz.getRe(colIdx), tmpz.getIm(colIdx));
            }
        }
        dest.cols.setSize(nzCnt);
        dest.data.setSize(nzCnt);
        return dest;
    }

    public static ZMatrixRMaj KZKT(@NotNull ZMatrixCsr KZ, @NotNull IMatrixCsr KT, @Nullable ZMatrixRMaj dest) {
        if (dest == null) {
            dest = new ZMatrixRMaj(KZ.numRows(), KZ.numRows());
        } else {
            dest.reshape(KZ.numRows(), KZ.numRows());
            dest.zero();
        }
        for (int i = 0, a = 0; i < KZ.numRows(); i++, a += 2 * KZ.numRows()) {
            for (int j = KZ.begin(i); j < KZ.end(i); j++) {
                double re = KZ.data.getRe(j);
                double im = KZ.data.getIm(j);
                int ktRowIdx = KZ.cols.get(j);
                for (int ki = KT.begin(ktRowIdx); ki < KT.end(ktRowIdx); ki++) {
                    int c = KT.cols.get(ki);
                    if (c > i) {
                        break;
                    }
                    int k = KT.data.get(ki);
                    int idx = a + 2 * c;
                    dest.data[idx] += re * k;
                    dest.data[idx + 1] += im * k;
                }
            }
        }
        for (int i = 0; i < KZ.numRows(); i++) {
            for (int j = 0; j < i; j++) {
                int ij = 2 * (i * KZ.numRows() + j);
                int ji = 2 * (j * KZ.numRows() + i);
                dest.data[ji] = dest.data[ij];
                dest.data[ji + 1] = dest.data[ij + 1];
            }
        }
        return dest;
    }

    public static void mul(ZMatrixRMaj M, ZMatrixAc Z, ZMatrixRMaj dest) {
        int idx = 0;
        for (int i = 0, anchor = 0; i < M.numRows; i++, anchor += 2 * M.numCols) {
            for (int j = 0; j < Z.size(); j++) {
                double re = 0;
                double im = 0;
                for (int k = Z.begins[j]; k < Z.ends[j]; k++) {
                    int col = Z.cols[k];
                    double zRe = Z.res[k];
                    double zIm = Z.ims[k];
                    int mIdx = anchor + 2 * col;
                    double mRe = M.data[mIdx];
                    double mIm = M.data[mIdx + 1];
                    re += (mRe * zRe - mIm * zIm);
                    im += (mRe * zIm + zRe * mIm);
                }
                dest.data[idx++] = re;
                dest.data[idx++] = im;
            }
        }
    }

    public static void mulTransK(ZMatrixRMaj M, KMatrix K, ZMatrixRMaj dest) {
        int stride = M.numCols * 2;
        for (int r = 0, anchor = 0, idx = 0;
             r < M.numRows;
             ++r, anchor += stride, idx += r * 2
        ) {
            for (int c = r; c < K.numRows(); c++) {
                double re = 0f;
                double im = 0f;
                double[] kRow = K.data[c];
                int[] nzis = K.nzi[c];
                int nonZeroEltQty = nzis[0];
                for (int k = 1; k <= nonZeroEltQty; k++) {
                    int colIdx = nzis[k];
                    int mDataIdx = anchor + colIdx * 2;
                    double kv = kRow[colIdx];
                    re += M.data[mDataIdx] * kv;
                    im += M.data[mDataIdx + 1] * kv;
                }
                dest.data[idx++] = re;
                dest.data[idx++] = im;
            }
        }
        for (int i = 0; i < K.numRows(); i++) {
            for (int j = i + 1; j < K.numRows(); j++) {
                int ij = 2 * (i * K.numRows() + j);
                int ji = 2 * (j * K.numRows() + i);
                dest.data[ji] = dest.data[ij];
                dest.data[ji + 1] = dest.data[ij + 1];
            }
        }
    }

    public static void mul(KMatrix K, VectorAc V, ZMatrixRMaj dest) {
        for (int i = 0, idx = 0; i < K.numRows(); i++) {
            double[] kRow = K.data[i];
            double re = 0;
            double im = 0;
            for (int j = 0; j < V.nzi.getSize(); ++j) {
                int vIdx = V.nzi.get(j);
                double k = kRow[vIdx];
                re += V.data.getRe(vIdx) * k;
                im += V.data.getIm(vIdx) * k;
            }
            dest.data[idx++] = re;
            dest.data[idx++] = im;
        }
    }

    public static void mul(ZMatrixRMaj M, VectorAc V, ZMatrixRMaj dest) {
        for (int idx = 0, anchor = 0;
             anchor < M.data.length;
             anchor += V.data.getSize() * 2
        ) {
            double re = 0;
            double im = 0;
            for (int j = 0; j < V.nzi.getSize(); ++j) {
                int c = V.nzi.get(j);
                int i = anchor + 2 * c;
                double mRe = M.data[i];
                double mIm = M.data[i + 1];
                double vRe = V.data.getRe(c);
                double vIm = V.data.getIm(c);
                re += (mRe * vRe - mIm * vIm);
                im += (mRe * vIm + vRe * mIm);
            }
            dest.data[idx++] = re;
            dest.data[idx++] = im;
        }
    }

    public static void mul2(ZMatrixRMaj M, VectorAc V, ZMatrixRMaj dest) {
        Arrays.fill(dest.data, 0);
        int maxR = M.numRows * 2;
        int stride = M.numCols * 2;
        for (int j = 0; j <= V.nzi.getSize(); ++j) {
            int c = V.nzi.get(j);
            for (int i = 2 * c, r = 0; r < maxR; i += stride, r += 2) {
                double mRe = M.data[i];
                double mIm = M.data[i + 1];
                if (mRe != 0 || mIm != 0) {
                    double vRe = V.data.getRe(c);
                    double vIm = V.data.getIm(c);
                    dest.data[r] += (mRe * vRe - mIm * vIm);
                    dest.data[r + 1] += (mRe * vIm + vRe * mIm);
                }
            }
        }
    }

    public static void mulTransK(KMatrix K, ZMatrixRMaj V, ZMatrixRMaj dest) {
        Arrays.fill(dest.data, 0);
        for (int i = 0; i < K.numRows(); i++) {
            int vIdx = i * 2;
            double re = V.data[vIdx];
            double im = V.data[vIdx + 1];
            double[] kRow = K.data[i];
            int[] nzis = K.nzi[i];
            for (int j = 1; j <= nzis[0]; j++) {
                int kIdx = nzis[j];
                double k = kRow[kIdx];
                int destIdx = kIdx * 2;
                if (k == 1) {
                    dest.data[destIdx] += re;
                    dest.data[destIdx + 1] += im;
                } else if (k == -1) {
                    dest.data[destIdx] -= re;
                    dest.data[destIdx + 1] -= im;
                }
            }
        }
    }

    public static void mul(ZMatrixAc Z, ZMatrixRMaj V, ZMatrixRMaj dest) {
        for (int i = 0, idx = 0; i < Z.size(); i++) {
            double re = 0;
            double im = 0;
            for (int j = Z.begins[i]; j < Z.ends[i]; j++) {
                double zRe = Z.res[j];
                double zIm = Z.ims[j];
                int vIdx = Z.cols[j] * 2;
                double vRe = V.data[vIdx];
                double vIm = V.data[vIdx + 1];
                re += (zRe * vRe - zIm * vIm);
                im += (zRe * vIm + zIm * vRe);
            }
            dest.data[idx++] = re;
            dest.data[idx++] = im;
        }
    }
}
