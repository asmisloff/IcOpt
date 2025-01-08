package ic.matrix.util;

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
}