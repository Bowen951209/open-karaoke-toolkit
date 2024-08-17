package net.okt.system;

public class MathUtils {
    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else
            return Math.min(value, max);
    }
}
