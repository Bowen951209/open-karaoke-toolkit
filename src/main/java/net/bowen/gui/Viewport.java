package net.bowen.gui;

import net.bowen.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;
import java.util.ArrayList;

public class Viewport extends JPanel {
    private final SaveLoadManager saveLoadManager;
    private final ArrayList<Long> marks;

    private String displayString;

    public void setDisplayString(String s) {
        this.displayString = s;
    }

    public Viewport(SaveLoadManager saveLoadManager) {
        super();
        this.saveLoadManager = saveLoadManager;
        this.marks = saveLoadManager.getMarks();

        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(50, 250); // translate to initial position.

        // Draw the string.
        if (displayString != null) {
            drawText(g2d);
        }
    }


    private void drawText(Graphics2D g2d) {
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 50);
        g2d.setStroke(new BasicStroke(1));

        long time = saveLoadManager.getLoadedAudio().getTimePosition();

        // Draw strings.
        String[] lines = displayString.split("\n");
        for (int i = 0, index = 0; i < lines.length; i++) { // for each line of text
            String line = lines[i];

            // for each char in the string
            for (int j = 0; j < line.length(); j++) {
                String single = String.valueOf(line.charAt(j));
                // Get the glyph vector.
                GlyphVector glyphVector = font.createGlyphVector(g2d.getFontRenderContext(), single);

                // The shape of the font
                Shape fontShape = glyphVector.getGlyphOutline(0);

                // Set word space interval.
                g2d.translate(font.getSize(), 0);

                // The rectangle needs to consider the time point of the mark
                Rectangle colorRect = getRectangle(index, time);
                index++;

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

            g2d.translate(font.getSize() * -3, font.getSize() + 10); // Next line go down and go forward a little bit.
        }
    }

    private Rectangle getRectangle(int i, long time) {
        long lastWordEndTime = 0;
        long thisWordEndTime;
        long wordPeriod = 0;
        if (marks.size() > i + 1) {
            lastWordEndTime = marks.get(i);
            thisWordEndTime = marks.get(i + 1);
            wordPeriod = thisWordEndTime - lastWordEndTime;
        }

        int w = (int) ((float) (time - lastWordEndTime) / (float) wordPeriod * 50);
        return new Rectangle(0, -40, w, 50);
    }
}