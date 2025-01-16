package ic.matrix;

import org.junit.jupiter.api.Test;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMs;
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
            actual[i] = vec.data.getRe(i);
        }
        assertArrayEquals(res, actual);
    }

    @Test
    void getIm() {
        insert();
        double[] actual = new double[size()];
        for (int i = 0; i < size(); i++) {
            actual[i] = vec.data.getIm(i);
        }
        assertArrayEquals(ims, actual);
    }

    @Test
    void get() {
        insert();
        double[] actualRes = new double[size()];
        double[] actualIms = new double[size()];
        for (int i = 0; i < vec.data.getSize(); i++) {
            actualRes[i] = vec.data.getRe(i);
            actualIms[i] = vec.data.getIm(i);
        }
        assertArrayEquals(res, actualRes);
        assertArrayEquals(ims, actualIms);
    }

    @Test
    void set() {
        int idx = 5;
        int re = 5;
        int im = 5;
        vec.data.set(idx, re, im);
        assertEquals(re, vec.data.getRe(idx));
        assertEquals(im, vec.data.getIm(idx));
        assertEquals(0, vec.nzi.get(0));
    }

    @Test
    void insert() {
        measureTimeMs("insert", 20_000_000, () -> {
            vec.reset(size());
            for (int i = 0; i < size(); i++) {
                if (res[i] != 0 || ims[i] != 0) {
                    vec.insert(i, res[i], ims[i]);
                }
            }
        });
        assertArrayEquals(res, leftPart(vec.data.getDataRe(), res.length));
        assertArrayEquals(ims, leftPart(vec.data.getDataIm(), res.length));
        assertArrayEquals(new int[]{ 0, 1, 8, 9 }, leftPart(vec.nzi.getData(), vec.nzi.getSize()));
    }

    private double[] leftPart(double[] arr, int len) {
        double[] res = new double[len];
        System.arraycopy(arr, 0, res, 0, len);
        return res;
    }

    private int[] leftPart(int[] arr, int len) {
        int[] res = new int[len];
        System.arraycopy(arr, 0, res, 0, len);
        return res;
    }

    private int size() {
        return res.length;
    }
}