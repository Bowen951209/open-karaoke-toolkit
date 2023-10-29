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

        g2d.translate(50, 250);

        if (displayString != null) {
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 50);
            g2d.setStroke(new BasicStroke(1));

            String[] strings = displayString.split("\n");
            for (String string : strings) {
                GlyphVector glyphVector = font.createGlyphVector(g2d.getFontRenderContext(), string);

                for (int stringIndex = 0; stringIndex < string.length(); stringIndex++) {
                    g2d.translate(20, 0);

                    int time = (int) (System.currentTimeMillis() / 10 % 300);
                    Rectangle colorRect = new Rectangle(0, -40, time, 50);

                    Shape fontShape = glyphVector.getGlyphOutline(stringIndex);
                    Area intersectArea = new Area(fontShape);
                    intersectArea.intersect(new Area(colorRect));

                    g2d.setColor(Color.BLUE);
                    g2d.fill(intersectArea);

                    g2d.setColor(Color.BLACK);
                    g2d.draw(fontShape);
                }
                g2d.translate(0, 50);
            }
        }
    }

}
