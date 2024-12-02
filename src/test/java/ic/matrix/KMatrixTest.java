package ic.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KMatrixTest {

    @Test
    void set() {
        KMatrix k = new KMatrix(5, 10);
        k.set(0, 4, 1);
        k.set(2, 3, -1);
        assertEquals(1, k.data[0][4]);
        assertEquals(-1, k.data[2][3]);
    }

    @Test
    void rMulT() {
    }
}