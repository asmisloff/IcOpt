package ic.matrix;

public class ZMatrixDc {

    public final double[] data;

    public ZMatrixDc(int size) {
        data = new double[size];
    }

    public void set(int idx, double value) {
        data[idx] = value;
    }

    public double get(int idx) {
        return data[idx];
    }

    public int size() {
        return data.length;
    }
}
