package ic.matrix;

import org.ejml.data.ZMatrixRMaj;

public class ZMatrixAc {

    double[] res;
    double[] ims;
    int[] cols;
    int[] begins;
    int[] ends;
    int stride;

    public int size() {
        return begins.length;
    }

    public ZMatrixAc(int size, int stride) {
        assert stride >= 1;
        this.stride = stride;
        int capacity = size * stride;
        res = new double[capacity];
        ims = new double[capacity];
        cols = new int[capacity];
        ends = new int[size];
        begins = new int[size];
        for (int i = 0, anchor = 0; i < size; i++, anchor += stride) {
            begins[i] = anchor;
            ends[i] = anchor;
        }
    }

    public void add(int rowIdx, int colIdx, double re, double im) {
        if (re != 0 || im != 0) {
            ensureCapacity(rowIdx);
            int idx = ends[rowIdx]++;
            cols[idx] = colIdx;
            res[idx] = re;
            ims[idx] = im;
        }
    }

    private void ensureCapacity(int rowIdx) {
        int idx = ends[rowIdx];
        if (idx == res.length || rowIdx < ends.length - 1 && idx == begins[rowIdx + 1]) {
            realloc();
        }
    }

    private void realloc() {
        stride = extendedStride();
        int capacity = size() * stride;
        double[] newRes = new double[capacity];
        double[] newIms = new double[capacity];
        int[] newCols = new int[capacity];
        for (int i = 0, newBegin = 0; i < size(); i++, newBegin += stride) {
            int oldBegin = begins[i];
            int oldEnd = ends[i];
            int len = oldEnd - oldBegin;
            try {
                System.arraycopy(res, oldBegin, newRes, newBegin, len);
                System.arraycopy(ims, oldBegin, newIms, newBegin, len);
                System.arraycopy(cols, oldBegin, newCols, newBegin, len);
            } catch (Exception e) {
                System.out.printf(e.toString());
            }
            begins[i] = newBegin;
            ends[i] = newBegin + len;
        }
        res = newRes;
        ims = newIms;
        cols = newCols;
    }

    private int extendedStride() {
        if (stride < 2) { // Если stride == 1, то по формуле ниже никогда ничего не увеличится.
            stride = 2;
        }
        return Math.min(stride * 3 / 2, size());
    }

    public ZMatrixRMaj toZMatrixRMaj() {
        ZMatrixRMaj m = new ZMatrixRMaj(size(), size());
        for (int row = 0; row < size(); row++) {
            for (int idx = begins[row]; idx < ends[row]; ++idx) {
                int col = cols[idx];
                double re = res[idx];
                double im = ims[idx];
                m.set(row, col, re, im);
            }
        }
        return m;
    }

    public String rowToString(int rowIdx) {
        StringBuilder sb = new StringBuilder();
        for (int idx = begins[rowIdx]; idx < ends[rowIdx]; ++idx) {
            int colIdx = cols[idx];
            double re = res[idx];
            double im = ims[idx];
            sb.append(colIdx).append(": ").append('(').append(re).append(", ").append(im).append("); ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
