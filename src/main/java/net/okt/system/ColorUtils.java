package net.okt.system;

import java.awt.*;

public class ColorUtils {
    public static int rgbaToInt(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        return a << 24 | r << 16 | g << 8 | b;
    }
}
