package ic.matrix;

import org.ejml.data.ZMatrixRMaj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class AcMatrixOps {

    public static ZMatrixRMaj KZ(@NotNull IMatrixCsr K, @NotNull ZMatrixCsr Z, @Nullable ZMatrixRMaj dest) {
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
                for (int k = Z.begin(col); k < Z.end(col); k++) {
                    int outIdx = a + Z.cols.get(k) * 2;
                    dest.data[outIdx++] += kElt * Z.data.getRe(k);
                    dest.data[outIdx] += kElt * Z.data.getIm(k);
                }
            }
        }
        return dest;
    }

    public static ZMatrixRMaj KZKT(@NotNull ZMatrixRMaj KZ, @NotNull IMatrixCsr K, @Nullable ZMatrixRMaj dest) {
        if (dest == null) {
            dest = new ZMatrixRMaj(KZ.numRows, KZ.numRows);
        } else {
            dest.reshape(KZ.numRows, KZ.numRows);
        }
        int kzStride = KZ.numCols * 2;
        int kzRowAnchor = 0;
        int destIdx = 0;
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
