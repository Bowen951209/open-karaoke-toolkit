package net.okt.system;

public class MathUtils {
    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }
}
