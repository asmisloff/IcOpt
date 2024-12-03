package ic.matrix;

import org.ejml.data.CMatrixRMaj;

public class ZMatrixAc {

    final float[] res;
    final float[] ims;
    final int[] cols;
    final int[] begins;
    final int[] ends;

    public int size() {
        return begins.length;
    }

    public ZMatrixAc(int size, int maxLineIdx) {
        int capacity = size * maxLineIdx;
        res = new float[capacity];
        ims = new float[capacity];
        cols = new int[capacity];
        ends = new int[size];
        begins = new int[size];
        for (int i = 0; i < size; i++) {
            begins[i] = i * maxLineIdx;
            ends[i] = i * maxLineIdx;
        }
    }

    public void add(int rowIdx, int colIdx, float re, float im) {
        int idx = ends[rowIdx]++;
        cols[idx] = colIdx;
        res[idx] = re;
        ims[idx] = im;
    }

    public void mul(KMatrix kMatrix, CMatrixRMaj out) {
        for (int k = 0; k < kMatrix.data.length; k++) {
            byte[] kRow = kMatrix.data[k];
            for (int z = 0; z < size(); z++) {
                float real = 0f;
                float imag = 0f;
                for (int idx = begins[z]; idx < ends[z]; idx++) {
                    int col = cols[idx];
                    float re = res[idx];
                    float im = ims[idx];
                    if (kRow[col] == 1) {
                        real += re;
                        imag += im;
                    } else if (kRow[col] == -1) {
                        real -= re;
                        imag -= im;
                    }
                }
                out.set(k, z, real, imag);
            }
        }
    }

    public CMatrixRMaj toCMatrixRMaj() {
        CMatrixRMaj m = new CMatrixRMaj(size(), size());
        for (int row = 0; row < size(); row++) {
            for (int idx = begins[row]; idx < ends[row]; ++idx) {
                int col = cols[idx];
                float re = res[idx];
                float im = ims[idx];
                m.set(row, col, re, im);
            }
        }
        return m;
    }

    public String rowToString(int rowIdx) {
        StringBuilder sb = new StringBuilder();
        for (int idx = begins[rowIdx]; idx < ends[rowIdx]; ++idx) {
            int colIdx = cols[idx];
            float re = res[idx];
            float im = ims[idx];
            sb.append(colIdx).append(": ").append('(').append(re).append(", ").append(im).append("); ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
