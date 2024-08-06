package net.okt.system;

import java.awt.*;
import java.awt.geom.Area;
import net.okt.gui.Viewport;

/**
 * Same as {@link Area}, but record a line number for the cache checking in
 * {@link Viewport#updateDisplayingAreas(boolean)}
 */
public class LyricsArea extends Area {
    public final int line;

    public LyricsArea(Shape shape, int line) {
        super(shape);
        this.line = line;
    }
}
