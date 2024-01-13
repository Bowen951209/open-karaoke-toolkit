package net.bowen.gui;

import net.bowen.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class Viewport extends JPanel {
    private Font defaultFont = new Font(Font.SANS_SERIF, Font.BOLD, 50);
    private Font samllFont = new Font(Font.SANS_SERIF, Font.BOLD, 40);
    private Point linkedWordTrans = new Point(defaultFont.getSize(), 0);

    private final SaveLoadManager saveLoadManager;
    private final int[] renderingLines = {0, 1};

    public void setDefaultFont(Font defaultFont) {
        this.defaultFont = defaultFont;
        linkedWordTrans = new Point(defaultFont.getSize(), 0);
    }

    public void setLinkedFont(Font linkedFont) {
        this.samllFont = linkedFont;
    }


    public Viewport(SaveLoadManager saveLoadManager) {
        super();
        this.saveLoadManager = saveLoadManager;

        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.translate(0, defaultFont.getSize()); // translate to initial position.

        // Draw the string.
        if (saveLoadManager.getText() != null) {
            drawText(g2d);
        }
    }


    private void drawText(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1));

        long time = saveLoadManager.getLoadedAudio().getTimePosition();

        // Draw strings.
        final int secondLineIndent = saveLoadManager.getIndentSize();
        final int lineSpace = 70;
        int lineIndex = 0;
        int translatedX = 0;
        List<String> textList = saveLoadManager.getTextList();
        refreshRenderingLines(time);
        for (int i = 0; i < textList.size(); i++) {
            String s = textList.get(i);

            // If no available mark for this word, break.
            int endMarkIndex = i + 1 - lineIndex;
            if (saveLoadManager.getMarks().size() - 1 < endMarkIndex)
                break;

            // If meet \n, next line.
            if (s.equals("\n")) {
                lineIndex++;

                // Translate back to original
                g2d.translate(-translatedX, 0);
                translatedX = 0;

                if (lineIndex % 2 == 0) { // on first line
                    g2d.translate(0, -lineSpace);
                } else { // on second line
                    g2d.translate(secondLineIndent, lineSpace);
                    translatedX += secondLineIndent;
                }

                continue;
            }

            // If line not turn to render, continue.
            if (lineIndex != renderingLines[0] && lineIndex != renderingLines[1])
                continue;

            // The shape of the font
            Area fontArea = new Area();
            for (int j = 0; j < s.length(); j++) { // j should <= 1
                String c = String.valueOf(s.charAt(j));
                // Get the glyph vector.
                GlyphVector glyphVector;
                if (j == 0) {
                    glyphVector = defaultFont.createGlyphVector(g2d.getFontRenderContext(), c);
                } else {
                    glyphVector = samllFont.createGlyphVector(g2d.getFontRenderContext(), c);
                    glyphVector.setGlyphPosition(0, linkedWordTrans);
                }

                fontArea.add(new Area(glyphVector.getGlyphOutline(0)));
            }

            // The rectangle needs to consider the time point of the mark.
            Rectangle colorRect = getRectangle(endMarkIndex, time, s.length() == 2);

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
            translatedX += space;
        }
    }

    private void refreshRenderingLines(long time) {
        List<String> textList = saveLoadManager.getTextList();
        List<Long> marks = saveLoadManager.getMarks();
        renderingLines[0] = 0;
        renderingLines[1] = 1;
        for (int i = 0, line = 0; i < textList.size(); i++) {
            String s = textList.get(i);
            if (s.equals("\n") && i < marks.size()) {
                long lastWordEndTime = marks.get(i - line);
                if (lastWordEndTime < time) {
                    if (line % 2 == 0) {
                        renderingLines[0] = line + 2;
                    } else {
                        renderingLines[1] = line + 2;
                    }
                }
                line++;
            }
        }
    }

    private Rectangle getRectangle(int i, long time, boolean linkedWord) {
        ArrayList<Long> marks = saveLoadManager.getMarks();

        final int dFontSize = defaultFont.getSize();
        final int sFontSize = samllFont.getSize();
        long wordStartTime = marks.get(i - 1);
        long wordEndTime = marks.get(i);
        long wordPeriod = wordEndTime - wordStartTime;
        float ratio = (float) (time - wordStartTime) / (float) wordPeriod;
        int rectMaxSize = linkedWord ? dFontSize + sFontSize : dFontSize;
        int w = (int) (ratio * rectMaxSize);

        return new Rectangle(0, -dFontSize, w, dFontSize + 20); // 20 is the needed adjustment.
    }
}