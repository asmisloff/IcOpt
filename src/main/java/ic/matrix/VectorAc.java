package ic.matrix;

import org.ejml.data.Complex_F64;

public class VectorAc {

    final double[] data;
    final short[] nzi;

    public VectorAc(int size) {
        data = new double[size * 2];
        nzi = new short[size + 1];
    }

    public double getRe(int idx) {
        return data[idx * 2];
    }

    public double getIm(int idx) {
        return data[idx * 2 + 1];
    }

    public void get(int idx, Complex_F64 dest) {
        idx *= 2;
        dest.real = data[idx];
        dest.imaginary = data[idx + 1];
    }

    public void set(int idx, double re, double im) {
        idx *= 2;
        data[idx] = re;
        data[idx + 1] = im;
    }

    public void insert(int idx, double re, double im) {
        if (re != 0 || im != 0) {
            idx *= 2;
            nzi[++nzi[0]] = (short) idx;
            data[idx] = re;
            data[idx + 1] = im;
        }
    }

    public int size() {
        return data.length / 2;
    }
}
