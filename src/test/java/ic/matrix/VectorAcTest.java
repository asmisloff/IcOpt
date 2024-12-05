package ic.matrix;

import org.ejml.data.Complex_F32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VectorAcTest {

    private final VectorAc vec;
    //@formatter:off
    private final float[] res = new float[] {  1,  2, 0, 0, 0, 0, 0, 0,  3,  4 };
    private final float[] ims = new float[] { -1, -2, 0, 0, 0, 0, 0, 0, -3, -4 };
    //@formatter:on

    VectorAcTest() {
        int size = size();
        vec = new VectorAc(size);
    }

    @Test
    void getRe() {
        insert();
        float[] actual = new float[size()];
        for (int i = 0; i < size(); i++) {
            actual[i] = vec.getRe(i);
        }
        assertArrayEquals(res, actual);
    }

    @Test
    void getIm() {
        insert();
        float[] actual = new float[size()];
        for (int i = 0; i < size(); i++) {
            actual[i] = vec.getIm(i);
        }
        assertArrayEquals(ims, actual);
    }

    @Test
    void get() {
        insert();
        float[] actualRes = new float[size()];
        float[] actualIms = new float[size()];
        Complex_F32 buf = new Complex_F32();
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
        float[] expectedData = { 1, -1, 2, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -3, 4, -4 };
        assertArrayEquals(expectedData, vec.data);
        assertArrayEquals(new short[] { 4, 0, 2, 16, 18, 0, 0, 0, 0, 0, 0 }, vec.nzi);
    }

    private int size() {
        return res.length;
    }
}