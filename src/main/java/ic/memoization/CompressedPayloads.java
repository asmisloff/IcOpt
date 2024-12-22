package ic.memoization;

import org.jetbrains.annotations.Nullable;
import ru.vniizht.asuterkortes.counter.latticemodel.ic.memoization.TrainPosition;

import java.util.List;

import static java.lang.Math.round;

public class CompressedPayloads {

    private final long[] data;
    private int hashCode;
    private int size;

    public CompressedPayloads(List<TrainPosition> sortedPositions) {
        hashCode = 0;
        size = 0;
        data = new long[sortedPositions.size()];
        for (TrainPosition pos : sortedPositions) {
            if (!pos.isHalted()) {
                long code = encode(pos);
                data[size++] = code;
                int elementHash = (int) (code ^ (code >>> 32));
                hashCode = 31 * hashCode + elementHash;
            }
        }
    }

    public long encode(TrainPosition pos) {
        return encodeTrackNumber(pos.getLineIndex()) |
                encodeCoordinate(pos.getCoord()) |
                encodeAmperages(pos.getActiveAmp(), pos.getFullAmp());
    }

    static long encodeTrackNumber(int trackNumber) {
        return ((long) trackNumber) << 61;
    }

    static long encodeCoordinate(double x) {
        long c = round(x * 1000) << 32;
        if (x < 0) {
            long DROP_3_MSB_MASK = 0x1fffffffffffffffL;
            c &= DROP_3_MSB_MASK;
        }
        return c;
    }

    static long encodeAmperages(double activeAmp, @Nullable Double fullAmp) {
        long SHORT_MASK = 0xffffL;
        long amp = (round(activeAmp) & SHORT_MASK) << 16;
        if (fullAmp == null) {
            fullAmp = activeAmp;
        }
        amp |= round(fullAmp) & SHORT_MASK;
        return amp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompressedPayloads that = (CompressedPayloads) o;
        if (this.size != that.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.data[i] != that.data[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}