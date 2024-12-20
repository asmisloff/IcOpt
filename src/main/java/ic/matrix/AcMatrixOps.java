package ic.matrix;

import org.ejml.data.ZMatrixRMaj;

import java.util.Arrays;

public class AcMatrixOps {

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

    public static void mul(KMatrixCsr K, ZMatrixAc Z, ZMatrixRMaj dest) {
        int kSize = K.numRows() * K.numCols();
        int destIdx = 0;
        for (int k = K.anchor(0); k < kSize; k += K.numCols()) {
            for (int z = 0; z < Z.size(); z++) {
                double real = 0f;
                double imag = 0f;
                for (int idx = Z.begins[z]; idx < Z.ends[z]; idx++) {
                    int col = Z.cols[idx];
                    double re = Z.res[idx];
                    double im = Z.ims[idx];
                    double kv = K.denseData.get(k + col);
                    real += re * kv;
                    imag += im * kv;
                }
                dest.data[destIdx++] = real;
                dest.data[destIdx++] = imag;
            }
        }
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
        short nonZeroEltQty = V.nzi[0];
        for (int i = 0, idx = 0; i < K.numRows(); i++) {
            double[] kRow = K.data[i];
            double re = 0;
            double im = 0;
            for (int j = 1; j <= nonZeroEltQty; j++) {
                int vIdx = V.nzi[j];
                int kIdx = vIdx / 2;
                double k = kRow[kIdx];
                if (k == 1) {
                    re += V.data[vIdx];
                    im += V.data[vIdx + 1];
                } else if (k == -1) {
                    re -= V.data[vIdx];
                    im -= V.data[vIdx + 1];
                }
            }
            dest.data[idx++] = re;
            dest.data[idx++] = im;
        }
    }

    public static void mul(ZMatrixRMaj M, VectorAc V, ZMatrixRMaj dest) {
        for (int idx = 0, anchor = 0;
             anchor < M.data.length;
             anchor += V.data.length
        ) {
            double re = 0;
            double im = 0;
            for (int j = 1; j <= V.nzi[0]; ++j) {
                int c = V.nzi[j];
                int i = anchor + c;
                double mRe = M.data[i];
                double mIm = M.data[i + 1];
                double vRe = V.data[c];
                double vIm = V.data[c + 1];
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
        for (int j = 1; j <= V.nzi[0]; ++j) {
            int c = V.nzi[j];
            for (int i = c, r = 0; r < maxR; i += stride, r += 2) {
                double mRe = M.data[i];
                double mIm = M.data[i + 1];
                double vRe = V.data[c];
                double vIm = V.data[c + 1];
                dest.data[r] += (mRe * vRe - mIm * vIm);
                dest.data[r + 1] += (mRe * vIm + vRe * mIm);
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
