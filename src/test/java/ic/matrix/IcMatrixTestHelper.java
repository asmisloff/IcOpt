package ic.matrix;

import java.util.function.Supplier;

public class IcMatrixTestHelper {

    public static double measureTimeMillis(Runnable action, int timesToRepeat) {
        long t0 = System.nanoTime();
        for (int i = 0; i < timesToRepeat; i++) {
            action.run();
        }
        return (1e-6 * (System.nanoTime() - t0)) / timesToRepeat;
    }

    public static <T> TimedValue<T> measureTimeMillis(Supplier<T> supplier, int timesToRepeat) {
        T res = null;
        long t0 = System.nanoTime();
        for (int i = 0; i < timesToRepeat; i++) {
            res = supplier.get();
        }
        return new TimedValue<>(res, (1e-6 * (System.nanoTime() - t0)) / timesToRepeat);
    }
}