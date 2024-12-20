package ic.matrix;

import org.ejml.data.Complex_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VectorAcTest {

    private final VectorAc vec;
    //@formatter:off
    private final double[] res = new double[] {  1,  2, 0, 0, 0, 0, 0, 0,  3,  4 };
    private final double[] ims = new double[] { -1, -2, 0, 0, 0, 0, 0, 0, -3, -4 };
    //@formatter:on

    VectorAcTest() {
        int size = size();
        vec = new VectorAc(size);
    }

    @Test
    void getRe() {
        insert();
        double[] actual = new double[size()];
        for (int i = 0; i < size(); i++) {
            actual[i] = vec.getRe(i);
        }
        assertArrayEquals(res, actual);
    }

    @Test
    void getIm() {
        insert();
        double[] actual = new double[size()];
        for (int i = 0; i < size(); i++) {
            actual[i] = vec.getIm(i);
        }
        assertArrayEquals(ims, actual);
    }

    @Test
    void get() {
        insert();
        double[] actualRes = new double[size()];
        double[] actualIms = new double[size()];
        Complex_F64 buf = new Complex_F64();
        for (int i = 0; i < vec.size(); i++) {
            vec.get(i, buf);
            actualRes[i] = buf.real;
            actualIms[i] = buf.imaginary;
        }
        assertArrayEquals(res, actualRes);
        assertArrayEquals(ims, actualIms);
    }

    @Test
    void set() {
        int idx = 5;
        int re = 5;
        int im = 5;
        vec.set(idx, re, im);
        assertEquals(re, vec.getRe(idx));
        assertEquals(im, vec.getIm(idx));
        assertEquals(0, vec.nzi[0]);
    }

    @Test
    void insert() {
        for (int i = 0; i < size(); i++) {
            vec.insert(i, res[i], ims[i]);
        }
        double[] expectedData = { 1, -1, 2, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -3, 4, -4 };
        assertArrayEquals(expectedData, vec.data);
        assertArrayEquals(new short[] { 4, 0, 2, 16, 18, 0, 0, 0, 0, 0, 0 }, vec.nzi);
    }

    private int size() {
        return res.length;
    }
}