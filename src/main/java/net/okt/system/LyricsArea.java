package net.okt.system;

import java.awt.*;
import java.awt.geom.Area;

public class LyricsArea extends Area {
    public final int line;

    public LyricsArea(Shape shape, int line) {
        super(shape);
        this.line = line;
    }
}
