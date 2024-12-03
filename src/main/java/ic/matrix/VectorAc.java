package ic.matrix;

import org.ejml.data.CMatrixRMaj;
import org.ejml.data.Complex_F32;

public class VectorAc {

    private final float[] data;
    private final short[] nzi;

    public VectorAc(int size) {
        data = new float[size * 2];
        nzi = new short[size + 1];
    }

    public float getRe(int idx) {
        return data[idx * 2];
    }

    public float getIm(int idx) {
        return data[idx * 2 + 1];
    }

    public void get(int idx, Complex_F32 dest) {
        idx *= 2;
        dest.real = data[idx];
        dest.imaginary = data[idx + 1];
    }

    public void set(int idx, float re, float im) {
        idx *= 2;
        data[idx] = re;
        data[idx + 1] = im;
    }

    public void insert(int idx, float re, float im) {
        idx *= 2;
        nzi[++nzi[0]] = (short) idx;
        data[idx] = re;
        data[idx + 1] = im;
    }

    public void mulR(CMatrixRMaj m, CMatrixRMaj res) {
        int stride = m.numCols * 2;
        for (int r = 0, idx = 0, anchor = 0;
             r < m.numRows;
             ++r, anchor += stride
        ) {
            float re = 0;
            float im = 0;
            for (int j = 1; j <= nzi[0]; ++j) {
                int c = nzi[j];
                int i = anchor + c;
                float mRe = m.data[i];
                float mIm = m.data[i + 1];
                float thisRe = data[c];
                float thisIm = m.data[c + 1];
                re += (mRe * thisRe - mIm * thisIm);
                im += (mRe * thisIm + thisRe * mIm);
            }
            res.data[idx++] = re;
            res.data[idx++] = im;
        }
    }
}
