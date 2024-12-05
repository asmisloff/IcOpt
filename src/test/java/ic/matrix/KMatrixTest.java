package ic.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KMatrixTest {

    //@formatter:off
    byte[][] data = {
            {  0,  1,  0,  1,  0,  0, -1,  0,  0 },
            {  1,  0,  0, -1,  0,  1,  0,  0,  0 },
            {  1,  0,  0, -1,  0,  1,  0,  1,  0 },
            { -1,  0,  1,  0,  0,  1,  1,  0, -1 },
            { -1,  0,  1,  0,  1,  0, -1,  0,  0 },
            {  0, -1, -1,  0, -1, -1,  0,  1,  1 },
            {  0,  1,  0,  1,  0,  0, -1,  1, -1 }
    };
    //@formatter:on

    private final KMatrix k = new KMatrix(data);

    @Test
    void constructor() {
        short[][] expected = {
                { 3, 1, 3, 6, 0, 0, 0, 0, 0, 0 },
                { 3, 0, 3, 5, 0, 0, 0, 0, 0, 0 },
                { 4, 0, 3, 5, 7, 0, 0, 0, 0, 0 },
                { 5, 0, 2, 5, 6, 8, 0, 0, 0, 0 },
                { 4, 0, 2, 4, 6, 0, 0, 0, 0, 0 },
                { 6, 1, 2, 4, 5, 7, 8, 0, 0, 0 },
                { 5, 1, 3, 6, 7, 8, 0, 0, 0, 0 }
        };
        for (int i = 0; i < expected.length; ++i) {
            assertArrayEquals(expected[i], k.nzi[i]);
        }
    }

    @Test
    void set() {
        k.set(0, 4, 1);
        k.set(2, 3, -1);
        assertEquals(1, k.data[0][4]);
        assertEquals(-1, k.data[2][3]);
    }
}