package ic.matrix;

public class ZMatrixAc {

    float[] res;
    float[] ims;
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
        res = new float[capacity];
        ims = new float[capacity];
        cols = new int[capacity];
        ends = new int[size];
        begins = new int[size];
        for (int i = 0, anchor = 0; i < size; i++, anchor += stride) {
            begins[i] = anchor;
            ends[i] = anchor;
        }
    }

    public void add(int rowIdx, int colIdx, float re, float im) {
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
        float[] newRes = new float[capacity];
        float[] newIms = new float[capacity];
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
