package ic.memoization;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompressedPayloadsTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "0 | "
    })
    void encode(int trackNumber,
                double coordinate,
                double activeAmp,
                double fullAmp) {

    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "1 | 00100000 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
            "2 | 01000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
            "3 | 01100000 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
            "4 | 10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
            "5 | 10100000 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
            "6 | 11000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000"
    })
    void encodeTrackNumber(int given, String expectedBinStr) {
        long expected = Long.parseUnsignedLong(expectedBinStr.replace(" ", ""), 2);
        long actual = CompressedPayloads.encodeTrackNumber(given);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            " 0.0     | 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
            " 1.0     | 00000000 00000000 00000011 11101000 00000000 00000000 00000000 00000000",
            "-1.0     | 00011111 11111111 11111100 00011000 00000000 00000000 00000000 00000000",
            " 20000.0 | 00000001 00110001 00101101 00000000 00000000 00000000 00000000 00000000",
            "-20000.0 | 00011110 11001110 11010011 00000000 00000000 00000000 00000000 00000000"
    })
    void encodeCoordinate(double x, String expectedBinStr) {
        long expected = Long.parseUnsignedLong(expectedBinStr.replace(" ", ""), 2);
        long actual = CompressedPayloads.encodeCoordinate(x);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            " 0.0 | 0.0 | 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
            " 1.0 | 1.1 | 00000000 00000000 00000000 00000000 00000000 00000001 00000000 00000001",
            " 1.0 |     | 00000000 00000000 00000000 00000000 00000000 00000001 00000000 00000001",
            " 1.0 |-1.6 | 00000000 00000000 00000000 00000000 00000000 00000001 11111111 11111110"
    })
    void encodeAmperages(double activeAmp, Double fullAmp, String expectedBinStr) {
        long expected = Long.parseUnsignedLong(expectedBinStr.replace(" ", ""), 2);
        long actual = CompressedPayloads.encodeAmperages(activeAmp, fullAmp);
        System.out.println(Long.toBinaryString(expected));
        assertEquals(expected, actual);
    }
}