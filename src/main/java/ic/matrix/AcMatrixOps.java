package ic.matrix;

import org.ejml.data.CMatrixRMaj;

import java.util.Arrays;

public class AcMatrixOps {

    public static void mul(KMatrix K, ZMatrixAc Z, CMatrixRMaj dest) {
        for (int k = 0; k < K.data.length; k++) {
            byte[] kRow = K.data[k];
            for (int z = 0; z < Z.size(); z++) {
                float real = 0f;
                float imag = 0f;
                for (int idx = Z.begins[z]; idx < Z.ends[z]; idx++) {
                    int col = Z.cols[idx];
                    float re = Z.res[idx];
                    float im = Z.ims[idx];
                    if (kRow[col] == 1) {
                        real += re;
                        imag += im;
                    } else if (kRow[col] == -1) {
                        real -= re;
                        imag -= im;
                    }
                }
                dest.set(k, z, real, imag);
            }
        }
    }

    public static void mul(CMatrixRMaj M, ZMatrixAc Z, CMatrixRMaj dest) {
        int idx = 0;
        for (int i = 0, anchor = 0; i < M.numRows; i++, anchor += 2 * M.numCols) {
            for (int j = 0; j < Z.size(); j++) {
                float re = 0;
                float im = 0;
                for (int k = Z.begins[j]; k < Z.ends[j]; k++) {
                    int col = Z.cols[k];
                    float zRe = Z.res[k];
                    float zIm = Z.ims[k];
                    int mIdx = anchor + 2 * col;
                    float mRe = M.data[mIdx];
                    float mIm = M.data[mIdx + 1];
                    re += (mRe * zRe - mIm * zIm);
                    im += (mRe * zIm + zRe * mIm);
                }
                dest.data[idx++] = re;
                dest.data[idx++] = im;
            }
        }
    }

    public static void mulTransK(CMatrixRMaj M, KMatrix K, CMatrixRMaj dest) {
        int stride = M.numCols * 2;
        for (int r = 0, idx = 0, anchor = 0;
             r < M.numRows;
             ++r, anchor += stride
        ) {
            for (int c = 0; c < K.numRows(); c++) {
                float re = 0f;
                float im = 0f;
                byte[] kRow = K.data[c];
                short[] nzis = K.nzi[c];
                short nonZeroEltQty = nzis[0];
                for (int k = 1; k <= nonZeroEltQty; k++) {
                    int colIdx = nzis[k];
                    int mDataIdx = anchor + colIdx * 2;
                    if (kRow[colIdx] == 1) {
                        re += M.data[mDataIdx];
                        im += M.data[mDataIdx + 1];
                    } else { // -1
                        re -= M.data[mDataIdx];
                        im -= M.data[mDataIdx + 1];
                    }
                }
                dest.data[idx++] = re;
                dest.data[idx++] = im;
            }
        }
    }

    public static void mul(KMatrix K, VectorAc V, CMatrixRMaj dest) {
        short nonZeroEltQty = V.nzi[0];
        for (int i = 0, idx = 0; i < K.numRows(); i++) {
            byte[] kRow = K.data[i];
            float re = 0;
            float im = 0;
            for (int j = 1; j <= nonZeroEltQty; j++) {
                int vIdx = V.nzi[j];
                int kIdx = vIdx / 2;
                int k = kRow[kIdx];
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

    public static void mul(CMatrixRMaj M, VectorAc V, CMatrixRMaj dest) {
        for (int idx = 0, anchor = 0;
             anchor < M.data.length;
             anchor += V.data.length
        ) {
            float re = 0;
            float im = 0;
            for (int j = 1; j <= V.nzi[0]; ++j) {
                int c = V.nzi[j];
                int i = anchor + c;
                float mRe = M.data[i];
                float mIm = M.data[i + 1];
                float vRe = V.data[c];
                float vIm = V.data[c + 1];
                re += (mRe * vRe - mIm * vIm);
                im += (mRe * vIm + vRe * mIm);
            }
            dest.data[idx++] = re;
            dest.data[idx++] = im;
        }
    }

    public static void mulTransK(KMatrix K, CMatrixRMaj V, CMatrixRMaj dest) {
        Arrays.fill(dest.data, 0);
        for (int i = 0; i < K.numRows(); i++) {
            int vIdx = i * 2;
            float re = V.data[vIdx];
            float im = V.data[vIdx + 1];
            byte[] kRow = K.data[i];
            short[] nzis = K.nzi[i];
            for (int j = 1; j <= nzis[0]; j++) {
                int kIdx = nzis[j];
                int k = kRow[kIdx];
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

    public static void mul(ZMatrixAc Z, CMatrixRMaj V, CMatrixRMaj dest) {
        for (int i = 0, idx = 0; i < Z.size(); i++) {
            float re = 0;
            float im = 0;
            for (int j = Z.begins[i]; j < Z.ends[i]; j++) {
                float zRe = Z.res[j];
                float zIm = Z.ims[j];
                int vIdx = Z.cols[j] * 2;
                float vRe = V.data[vIdx];
                float vIm = V.data[vIdx + 1];
                re += (zRe * vRe - zIm * vIm);
                im += (zRe * vIm + zIm * vRe);
            }
            dest.data[idx++] = re;
            dest.data[idx++] = im;
        }
    }
}
