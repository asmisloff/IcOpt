package ic.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZMatrixAcTest {

    @Test
    void add() {
        int size = 200;
        ZMatrixAc m = new ZMatrixAc(size, 1);
        m.add(0, 0, 1f, 1f);
        m.add(1, 1, 2f, 2f);
        m.add(1, 120, 120f, 120f);
        assertEquals("0: (1.0, 1.0);", m.rowToString(0));
        assertEquals("1: (2.0, 2.0); 120: (120.0, 120.0);", m.rowToString(1));
    }
}