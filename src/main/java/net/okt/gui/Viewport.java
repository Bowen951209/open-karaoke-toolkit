package net.okt.gui;

import net.okt.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Viewport extends JPanel {
    private final SaveLoadManager saveLoadManager;
    private final int[] renderingLines = {0, 1};

    private Font defaultFont = new Font(Font.SANS_SERIF, Font.BOLD, 50);
    private Font samllFont = new Font(Font.SANS_SERIF, Font.BOLD, 40);
    private Point linkedWordTrans = new Point(defaultFont.getSize(), 0);
    private BufferedImage bufferedImage;
    private boolean shouldShowText = true;

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void resetBufferedImage() {
        int w = saveLoadManager.getPropInt("resolutionX");
        int h = saveLoadManager.getPropInt("resolutionY");
        bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

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
        setBackground(Color.LIGHT_GRAY);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (bufferedImage == null) return;
        drawToBufImg(saveLoadManager.getLoadedAudio().getTimePosition());

        Graphics2D g2d = (Graphics2D) g;

        // After rendering on the buffered image, display it on this panel.
        float aspectRatio = (float) bufferedImage.getWidth() / bufferedImage.getHeight();
        int drawWidth = getWidth(); // width should fit the panel.
        int drawHeight = (int) ((float) drawWidth / aspectRatio);
        g2d.drawImage(bufferedImage, 0, 0, drawWidth, drawHeight, null);
    }

    public void drawToBufImg(long time) {
        Graphics2D imgG2d = (Graphics2D) bufferedImage.getGraphics();

        // Clear the buffered image.
        imgG2d.setBackground(Color.WHITE);
        imgG2d.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        final int x = saveLoadManager.getPropInt("textPosX");
        final int y = saveLoadManager.getPropInt("textPosY");
        imgG2d.translate(x, y + defaultFont.getSize()); // translate to initial position.

        // Draw the string.
        if (saveLoadManager.getProp("text") != null) {
            drawText(imgG2d, time);
        }
    }

    private void drawText(Graphics2D g2d, long time) {
        refreshRenderingLines(time);
        if (!shouldShowText) return;

        final int secondLineIndent = saveLoadManager.getPropInt("indentSize");
        final int lineSpace = saveLoadManager.getPropInt("lineSpace");
        int lineIndex = 0;
        int translatedX = 0;
        List<String> textList = saveLoadManager.getTextList();

        for (int i = 0, offset = 0; i < textList.size(); i++) {
            String s = textList.get(i);

            // Checking if this word is the head of a new paragraph.
            boolean isNewParagraph = false;
            if (i > 2)
                isNewParagraph = textList.get(i - 1).equals("\n") && textList.get(i - 2).equals("\n");

            // Defining which mark has the end time of this word.
            // If the word is the head of a new paragraph, the offset should + 1.
            if (isNewParagraph) offset++;
            int endMarkIndex = i + 1 - lineIndex + offset;

            // If no available mark for this word, break.
            if (saveLoadManager.getMarks().size() - 1 < endMarkIndex)
                break;

            // If is new paragraph, try to draw the ready dots.
            if (isNewParagraph || i == 0)
                drawReadyDots(g2d, time, endMarkIndex - 1);

            // If meet single \n, next line.
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
            g2d.setStroke(new BasicStroke(8));
            g2d.setColor(Color.BLACK);
            g2d.draw(fontArea);

            // Set word space interval.
            int space = s.length() == 2 ? samllFont.getSize() + defaultFont.getSize() : defaultFont.getSize();
            g2d.translate(space, 0);
            translatedX += space;
        }
    }

    /**
     * Refresh the {@link #renderingLines} according to the playing time. After refreshing it, we can know which 2
     * lines we should render on the display.
     */
    private void refreshRenderingLines(long time) {
        List<String> textList = saveLoadManager.getTextList();
        renderingLines[0] = 0;
        renderingLines[1] = 1;

        // The loop process through each line of the text.
        // for each word:
        for (int i = 0, paragraph = 0, line = 0; i < textList.size(); i++) {
            // If it's the end of a line, we process it.
            if (isLineEnd(i)) {
                // If the next word is also "\n", means it's the end line of the paragraph.
                boolean isParagraphEnd = isParagraphEnd(i);
                if (isParagraphEnd) paragraph++;

                long lastWordEndTime = getLastWordEndTime(i, line, paragraph);

                if (lastWordEndTime < time) {
                    addLine(line);
                } else {
                    // If we get here, it means the renderingLines is set to the correct value as it should be.
                    int disappearPeriod = saveLoadManager.getPropInt("textDisappearTime");
                    int readyDotsPeriod = saveLoadManager.getPropInt("dotsPeriod");
                    long lastlastWordEndTime = getLastLastWordEndTime(i, line, paragraph);

                    // If it is the end line of a paragraph, we don't want the 2nd last line to disappear as it would
                    // by default.
                    if (isParagraphEnd) {
                        // What this will do is to reset the changed 2nd last line back to the end line - 1.
                        reset2ndLastLineBack(line);

                        // After a certain period, we want the lines to disappear.
                        // When the time is after the specified period, the text should not show.
                        // When the ready dots should show, text should also show.
                        shouldShowText = time < lastlastWordEndTime + disappearPeriod ||
                                time > lastWordEndTime - readyDotsPeriod;

                        // When the ready dots should show, the renderingLines should set to the new paragraph's lines.
                        if (time > lastWordEndTime - readyDotsPeriod)
                            setToNewParagraphLines(line);
                    } else if (time < lastWordEndTime) shouldShowText = true;

                    return; // We've found the correct rendering lines, no need to keep running in the loop.
                }

                line++;
            } else if (isTextEnd(i)) {
                // We need a special process for the end line of the text, or else the disappearance won't work properly.
                int disappearPeriod = saveLoadManager.getPropInt("textDisappearTime");
                long thisWordEndTime = getThisWordEndTime(i, line, paragraph);
                shouldShowText = time < thisWordEndTime + disappearPeriod;

                // What this will do is to reset the changed 2nd last line back to the end line - 1.
                reset2ndLastLineBack(line);
            }
        }
    }

    private boolean isTextEnd(int textIdx) {
        var textList = saveLoadManager.getTextList();
        return textIdx == textList.size() - 1;
    }

    private boolean isLineEnd(int textIdx) {
        var textList = saveLoadManager.getTextList();
        String s = textList.get(textIdx);
        return s.equals("\n"); // Note: If the text is the end text, I don't consider it as the end of a line.
    }

    /**
     * You can only call this when you have made sure the word of the text index is "\n"
     */
    private boolean isParagraphEnd(int textIdx) {
        var textList = saveLoadManager.getTextList();

        boolean isEnd;
        if (textIdx + 1 < textList.size())
            isEnd = textList.get(textIdx + 1).equals("\n");
        else
            isEnd = false; // Note: If the text is the end text, I don't consider it as the end of a paragraph.

        return isEnd;
    }

    private void addLine(int line) {
        if (renderingLines[0] == line)
            // If the paragraph line is the upper line(index 0), set the upper line(index 0) to line + 2.
            renderingLines[0] = line + 2;
        else
            // If the paragraph line is the lower line(index 1), set the lower line(index 1) to line + 2.
            renderingLines[1] = line + 2;
    }

    private void reset2ndLastLineBack(int paragraphEndLineIdx) {
        if (renderingLines[0] == paragraphEndLineIdx)
            // If the paragraph end line is the upper line(index 0), set the lower line(index 1) to line - 1.
            renderingLines[1] = paragraphEndLineIdx - 1;
        else
            // If the paragraph end line is the lower line(index 1), set the upper line(index 0) to line - 1.
            renderingLines[0] = paragraphEndLineIdx - 1;
    }

    private void setToNewParagraphLines(int paragraphEndLineIdx) {
        if (renderingLines[0] == paragraphEndLineIdx) {
            // If the paragraph end line is the upper line:
            renderingLines[0] += 2;
            renderingLines[1] += 4;
        } else {
            // If the paragraph end line is the lower line:
            renderingLines[0] += 4;
            renderingLines[1] += 2;
        }
    }

    private long getThisWordEndTime(int thisWordIdx, int line, int paragraph) {
        var marks = saveLoadManager.getMarks();
        int markIdx = thisWordIdx - line + paragraph + 1;

        if (markIdx < marks.size())
            return marks.get(markIdx);
        else return Long.MAX_VALUE;
    }

    private long getLastWordEndTime(int thisWordIdx, int line, int paragraph) {
        var marks = saveLoadManager.getMarks();
        int markIdx = thisWordIdx - line + paragraph;

        if (markIdx < marks.size())
            return marks.get(markIdx);
        else return Long.MAX_VALUE;
    }

    private long getLastLastWordEndTime(int thisWordIdx, int line, int paragraph) {
        var marks = saveLoadManager.getMarks();
        int markIdx = thisWordIdx - line + paragraph - 1;

        if (markIdx < marks.size())
            return marks.get(markIdx);
        else return Long.MAX_VALUE;
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

        return new Rectangle(0, -dFontSize, w, dFontSize + 30); // 30 is the needed adjustment.
    }

    private void drawReadyDots(Graphics2D g2d, long time, int wordStartMarkIdx) {
        ArrayList<Long> marks = saveLoadManager.getMarks();
        long wordStartTime = marks.get(wordStartMarkIdx);

        final int period = saveLoadManager.getPropInt("dotsPeriod");
        final long dotsStartTime = wordStartTime - period;
        final int dotsNum = saveLoadManager.getPropInt("dotsNum");
        final int dotSize = 200;

        // If in the period, draw.
        if (time > dotsStartTime && time < wordStartTime) {
            final int startX =
                    saveLoadManager.getPropInt("dotsPosX") - saveLoadManager.getPropInt("textPosX");
            final int startY =
                    saveLoadManager.getPropInt("dotsPosY") - saveLoadManager.getPropInt("textPosY") - defaultFont.getSize();

            Area area = new Area();

            // Dot shapes.
            for (int i = 0, x = startX; i < dotsNum; i++, x += dotSize) {
                // Get the shape.
                Shape arcShape = new Arc2D.Float(x, startY, dotSize, dotSize, 0, 360, Arc2D.OPEN);

                // Draw arc bounds.
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(12));
                g2d.draw(arcShape);

                // Add to area.
                area.add(new Area(arcShape));
            }

            // Scrolling rect.
            int width = (int) ((float) (time - dotsStartTime) / period * dotSize * dotsNum);
            Rectangle rect = new Rectangle(startX, startY, width, dotSize);
            area.intersect(new Area(rect));

            g2d.setColor(Color.CYAN);
            g2d.fill(area);
        }
    }
}