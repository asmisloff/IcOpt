package ic.matrix.util;

import org.ejml.data.ZMatrixRMaj;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

public class IcMatrixTestHelper {

    public static double measureTimeMillis(Runnable action, int timesToRepeat) {
        long t0 = System.nanoTime();
        for (int i = 0; i < timesToRepeat; i++) {
            action.run();
        }
        return (1e-6 * (System.nanoTime() - t0)) / timesToRepeat;
    }

    public static double measureTimeMillis(Runnable action, int timesToRepeat, DoubleConsumer also) {
        long t0 = System.nanoTime();
        for (int i = 0; i < timesToRepeat; i++) {
            action.run();
        }
        double t = (1e-6 * (System.nanoTime() - t0)) / timesToRepeat;
        also.accept(t);
        return t;
    }

    public static <T> TimedValue<T> measureTimeMillis(Supplier<T> supplier, int timesToRepeat) {
        T res = null;
        long t0 = System.nanoTime();
        for (int i = 0; i < timesToRepeat; i++) {
            res = supplier.get();
        }
        return new TimedValue<>(res, (1e-6 * (System.nanoTime() - t0)) / timesToRepeat);
    }

    public static double measureTimeMs(int timesToRepeat, Runnable action) {
        long t0 = System.nanoTime();
        for (int i = 0; i < timesToRepeat; ++i) {
            action.run();
        }
        return (1e-6 * (System.nanoTime() - t0)) / timesToRepeat;
    }

    public static double measureTimeMs(String title, int timesToRepeat, Runnable action) {
        long t0 = System.nanoTime();
        for (int i = 0; i < timesToRepeat; ++i) {
            action.run();
        }
        double t = (1e-6 * (System.nanoTime() - t0)) / timesToRepeat;
        System.out.printf("%s: %.6f мс\n", title, t);
        return t;
    }

    public static double msFrom(long t) {
        return 1e-6 * (System.nanoTime() - t);
    }

    public static ZMatrixRMaj randomDenseZMatrix(int numberOfBlockEdges, int totalNumberOfEdges, int nzCntPerRow) {
        ZMatrixRMaj m = new ZMatrixRMaj(totalNumberOfEdges, totalNumberOfEdges);
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        for (int i = 0; i < totalNumberOfEdges; i++) {
            double re = tlr.nextDouble();
            double im = tlr.nextDouble();
            m.set(i, i, re, im);
            if (i >= numberOfBlockEdges && i < totalNumberOfEdges - 1) {
                for (int k = 0; k < nzCntPerRow - 1; k++) {
                    int j = tlr.nextInt(i + 1, totalNumberOfEdges);
                    re = tlr.nextDouble();
                    im = tlr.nextDouble();
                    m.set(i, j, re, im);
                    m.set(j, i, re, im);
                }
            }
        }
        return m;
    }
}