package net.bowen.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;

public class Viewport extends JPanel {
    private String displayString;

    public void setDisplayString(String s) {
        this.displayString = s;
    }

    public Viewport() {
        super();
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(50, 250); // translate to initial position.

        if (displayString != null) {
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 50);
            g2d.setStroke(new BasicStroke(1));

            String[] strings = displayString.split("\n");
            for (String string : strings) { // for each line of text
                // Get the glyph vector.
                GlyphVector glyphVector = font.createGlyphVector(g2d.getFontRenderContext(), string);

                // for each char in the string
                for (int stringIndex = 0; stringIndex < string.length(); stringIndex++) {
                    // Stretch the gap between each word(Each word are already in relative position).
                    g2d.translate(20, 0);

                    int time = (int) (System.currentTimeMillis() / 10 % 300);
                    // The width of the color rectangle is decided by time.
                    Rectangle colorRect = new Rectangle(0, -40, time, 50);

                    // The shape of the font
                    Shape fontShape = glyphVector.getGlyphOutline(stringIndex);

                    // Intersection area of colorRect and font Shape.
                    Area intersectArea = new Area(fontShape);
                    intersectArea.intersect(new Area(colorRect));

                    // Fill
                    g2d.setColor(Color.BLUE);
                    g2d.fill(intersectArea);

                    // Border
                    g2d.setColor(Color.BLACK);
                    g2d.draw(fontShape);
                }

                g2d.translate(0, 50); // Next line go down.
            }
        }
    }

}
