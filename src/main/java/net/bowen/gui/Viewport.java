package net.bowen.gui;

import net.bowen.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;
import java.util.ArrayList;

public class Viewport extends JPanel {
    private static final Font defaultFont = new Font(Font.SANS_SERIF, Font.BOLD, 50);
    private static final Font samllFont = new Font(Font.SANS_SERIF, Font.BOLD, 40);
    private static final Point linkedWordTrans = new Point(defaultFont.getSize(), 0);

    private final SaveLoadManager saveLoadManager;

    public Viewport(SaveLoadManager saveLoadManager) {
        super();
        this.saveLoadManager = saveLoadManager;

        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(0, 50); // translate to initial position.

        // Draw the string.
        if (saveLoadManager.getText() != null) {
            drawText(g2d);
        }
    }


    private void drawText(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1));

        long time = saveLoadManager.getLoadedAudio().getTimePosition();

        // Draw strings.
        int endMarkIndex = 1;
        for (String s : saveLoadManager.getTextList()) {
            // If no available mark for this word, break.
            if (saveLoadManager.getMarks().size() - 1 < endMarkIndex)
                break;

            // If meet \n, next line.
            if (s.equals("\n")) {
                g2d.translate(-300, 50); // translate calculation for temporary.
                continue;
            }

            // The shape of the font
            Area fontArea = new Area();
            for (int i = 0; i < s.length(); i++) { // i should <= 1
                String c = String.valueOf(s.charAt(i));
                // Get the glyph vector.
                GlyphVector glyphVector;
                if (i == 0) {
                    glyphVector = defaultFont.createGlyphVector(g2d.getFontRenderContext(), c);
                } else {
                    glyphVector = samllFont.createGlyphVector(g2d.getFontRenderContext(), c);
                    glyphVector.setGlyphPosition(0, linkedWordTrans);
                }

                fontArea.add(new Area(glyphVector.getGlyphOutline(0)));
            }

            // The rectangle needs to consider the time point of the mark
            Rectangle colorRect = getRectangle(endMarkIndex, time, s.length() == 2);
            endMarkIndex++;

            // Intersection area of colorRect and font Shape.
            Area intersectArea = new Area(fontArea);
            intersectArea.intersect(new Area(colorRect));

            // Fill
            g2d.setColor(Color.BLUE);
            g2d.fill(intersectArea);

            // Border
            g2d.setColor(Color.BLACK);
            g2d.draw(fontArea);

            // Set word space interval.
            int space = s.length() == 2 ? samllFont.getSize() + defaultFont.getSize() : defaultFont.getSize();
            g2d.translate(space, 0);
        }
    }

    private Rectangle getRectangle(int i, long time, boolean linkedWord) {
        ArrayList<Long> marks = saveLoadManager.getMarks();

        long wordStartTime = marks.get(i - 1);
        long wordEndTime = marks.get(i);
        long wordPeriod = wordEndTime - wordStartTime;
        int w = (int) ((float) (time - wordStartTime) / (float) wordPeriod * (linkedWord ? 90 : 50));
        return new Rectangle(0, -40, w, 50);
    }
}