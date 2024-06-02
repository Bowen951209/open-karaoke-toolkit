package net.okt.gui;

import net.okt.system.LyricsArea;
import net.okt.system.LyricsProcessor;
import net.okt.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

public class Viewport extends JPanel {
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 1);
    private static final AffineTransform ZERO_TRANSFORM = AffineTransform.getScaleInstance(0, 0);

    private final SaveLoadManager saveLoadManager;
    private final LyricsProcessor lyricsProcessor;
    private final LyricsArea[] displayingAreas = new LyricsArea[2];

    private BufferedImage bufferedImage;

    public Viewport(SaveLoadManager saveLoadManager, LyricsProcessor lyricsProcessor) {
        super();
        this.saveLoadManager = saveLoadManager;
        this.lyricsProcessor = lyricsProcessor;

        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.LIGHT_GRAY);
    }

    /**
     * Get the glyph vector of the passed in string.
     * If the string contains a link word, it'll be applied the linkTransform.
     */
    private static GlyphVector getGlyphVector(String s, AffineTransform linkTransform) {
        GlyphVector glyphVector = FONT.createGlyphVector(FRC, s);
        int length = s.length();

        for (int i = 0; i < length; i++) {
            char currentChar = s.charAt(i);
            if (currentChar == '\'' && i > 0 && i < length - 1) {
                // If the left or right are eastern chars, it is a link word. Apply transforms to it.
                if (LyricsProcessor.isEasternChar(s.charAt(i - 1)) ||
                        LyricsProcessor.isEasternChar(s.charAt(i + 1))) {
                    // Scale the quote to zero to make it disappear.
                    glyphVector.setGlyphTransform(i, ZERO_TRANSFORM);
                    i++;
                    // The next char is the link char, apply to linkTransform.
                    glyphVector.setGlyphTransform(i, linkTransform);
                }
            }
        }

        return glyphVector;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void resetBufferedImage() {
        int w = saveLoadManager.getPropInt("resolutionX");
        int h = saveLoadManager.getPropInt("resolutionY");
        bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (saveLoadManager.getLoadedAudio() == null) return;
        drawToBufImg(saveLoadManager.getLoadedAudio().getTimePosition());

        Graphics2D g2d = (Graphics2D) g;

        // After rendering on the buffered image, display it on this panel.
        float aspectRatio = (float) bufferedImage.getWidth() / bufferedImage.getHeight();
        int drawWidth = getWidth(); // width should fit the panel.
        int drawHeight = (int) ((float) drawWidth / aspectRatio);
        g2d.drawImage(bufferedImage, 0, 0, drawWidth, drawHeight, null);
    }

    /**
     * Update the displaying areas. It uses cache if not forceUpdate.
     * @param forceUpdate If you want to ignore the cache and force update.
     */
    public void updateDisplayingAreas(boolean forceUpdate) {
        int[] displayingLines = lyricsProcessor.getDisplayingLines();

        if (forceUpdate) {
            displayingAreas[0] = getAreaAtLine(displayingLines[0]);
            displayingAreas[1] = getAreaAtLine(displayingLines[1]);
        } else {
            if (displayingAreas[0] == null || displayingAreas[0].line != displayingLines[0])
                displayingAreas[0] = getAreaAtLine(displayingLines[0]);

            if (displayingAreas[1] == null || displayingAreas[1].line != displayingLines[1])
                displayingAreas[1] = getAreaAtLine(displayingLines[1]);
        }
    }

    public void drawToBufImg(int time) {
        Graphics2D imgG2d = (Graphics2D) bufferedImage.getGraphics();
        // This setting can prevent the thick stroke artifacts.
        imgG2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Clear the buffered image.
        imgG2d.setBackground(new Color(saveLoadManager.getPropInt("backgroundColor"), true));
        imgG2d.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // Set the time and re-process the things.
        lyricsProcessor.setTime(time);

        // Draw ready dots.
        float readyDotsPercentage = lyricsProcessor.getReadyDotsPercentage();
        if (readyDotsPercentage > 0)
            drawReadyDots(imgG2d, readyDotsPercentage);

        int x = toDrawSize(saveLoadManager.getPropInt("textPosX"));
        int y = toDrawSize(saveLoadManager.getPropInt("textPosY"));
        int defaultFontSize = saveLoadManager.getPropInt("defaultFontSize");
        imgG2d.translate(x, y + defaultFontSize); // translate to initial position.

        // Draw the string.
        if (lyricsProcessor.shouldDisplayText())
            drawText(imgG2d, time);
    }

    private void drawText(Graphics2D g2d, int time) {
        int secondLineIndent = toDrawSize(saveLoadManager.getPropInt("indentSize"));
        int lineSpace = toDrawSize(saveLoadManager.getPropInt("lineSpace"));

        updateDisplayingAreas(false);

        Area topFontArea = displayingAreas[0];
        Area bottomFontArea = displayingAreas[1];

        Area topIntersectArea = new Area(topFontArea);
        Area bottomIntersectArea = new Area(bottomFontArea);
        topIntersectArea.intersect(getRectangleArea(lyricsProcessor.getDisplayingLines()[0], time));
        bottomIntersectArea.intersect(getRectangleArea(lyricsProcessor.getDisplayingLines()[1], time));

        int strokeWidth = (int) (toDrawSize(saveLoadManager.getPropInt("textStroke")) * 0.01);
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        Color textColor = new Color(saveLoadManager.getPropInt("textColor"));

        // base
        g2d.setColor(Color.WHITE);
        g2d.fill(topFontArea);

        // intersect area
        g2d.setColor(textColor);
        g2d.fill(topIntersectArea);

        // outline
        g2d.setColor(Color.BLACK);
        g2d.draw(topFontArea);

        // Translate position to the bottom line.
        g2d.translate(secondLineIndent, lineSpace);

        // base
        g2d.setColor(Color.WHITE);
        g2d.fill(bottomFontArea);

        // intersect area
        g2d.setColor(textColor);
        g2d.fill(bottomIntersectArea);

        // outline
        g2d.setColor(Color.BLACK);
        g2d.draw(bottomFontArea);
    }

    private LyricsArea getAreaAtLine(int line) {
        if (line == -1 || line >= lyricsProcessor.getLyricsLines().size())
            return new LyricsArea(new Area(), -1);

        int defaultFontSize = saveLoadManager.getPropInt("defaultFontSize");
        int linkedFontSize = saveLoadManager.getPropInt("linkedFontSize");
        float defaultScale = toDrawSize(defaultFontSize);
        float linkScale = (float) linkedFontSize / defaultFontSize;

        AffineTransform defaultScaleTransform = AffineTransform.getScaleInstance(defaultScale, defaultScale);
        AffineTransform linkScaleTransform = AffineTransform.getScaleInstance(linkScale, linkScale);

        String string = lyricsProcessor.getLyricsLines().get(line);
        GlyphVector glyphVector = getGlyphVector(string, linkScaleTransform);
        LyricsArea area = new LyricsArea(glyphVector.getOutline(), line);

        area.transform(defaultScaleTransform);

        return area;
    }

    private Area getRectangleArea(int line, int time) {
        var marks = saveLoadManager.getMarks();
        int defaultFontSize = toDrawSize(saveLoadManager.getPropInt("defaultFontSize"));
        int linkedFontSize = toDrawSize(saveLoadManager.getPropInt("linkedFontSize"));
        int lineStartMark = lyricsProcessor.getStartMarkAtLine(line);
        double spaceWidth = FONT.getStringBounds(" ", FRC).getWidth();

        int rectWidth = 0;
        for (int i = lineStartMark + 1; i < marks.size(); i++) {
            String textBeforeMark = lyricsProcessor.getTextBeforeMark(i);
            if (textBeforeMark == null) continue;

            int markTime = marks.get(i);
            boolean isEasternChar = LyricsProcessor.isEasternChar(textBeforeMark.charAt(0));
            boolean isLinkWord = textBeforeMark.length() == 2 && isEasternChar;

            double wordWidth = FONT.getStringBounds(textBeforeMark, FRC).getWidth();
            int addWidth = isLinkWord ? (defaultFontSize + linkedFontSize) : (int) (wordWidth * defaultFontSize);

            if (time < markTime) {
                int lastMarkTime = marks.get(i - 1);
                int wordPeriod = markTime - lastMarkTime;
                float percentage = (float) (time - lastMarkTime) / wordPeriod;

                rectWidth += (int) (percentage * addWidth);

                break;
            }

            rectWidth += addWidth;

            // IF it is a western char, add a space offset.(Except for the first word in the line.)
            if (!isEasternChar && i >= lineStartMark + 1)
                rectWidth += (int) (spaceWidth * defaultFontSize);
        }

        Rectangle rectangle = new Rectangle(0, -defaultFontSize, rectWidth, defaultFontSize * 2);
        // * 2 is the needed adjustment.

        return new Area(rectangle);
    }

    private void drawReadyDots(Graphics2D g2d, float percentage) {
        int dotSize = toDrawSize(saveLoadManager.getPropInt("dotsSize"));
        int dotsNum = saveLoadManager.getPropInt("dotsNum");
        int startX = toDrawSize(saveLoadManager.getPropInt("dotsPosX"));
        int startY = toDrawSize(saveLoadManager.getPropInt("dotsPosY"));
        Color dotsColor = new Color(saveLoadManager.getPropInt("dotsColor"));

        Area arcsArea = new Area();

        // Dot shapes.
        for (int i = 0, x = startX; i < dotsNum; i++, x += dotSize) {
            // Get the shape.
            Shape arcShape = new Arc2D.Float(x, startY, dotSize, dotSize, 0, 360, Arc2D.OPEN);

            // Add to arcsArea.
            arcsArea.add(new Area(arcShape));
        }

        // Scrolling rect.
        int width = (int) (percentage * dotSize * dotsNum);
        Rectangle rect = new Rectangle(startX, startY, width, dotSize);

        // Intersect area.
        Area intersectArea = new Area(arcsArea);
        intersectArea.intersect(new Area(rect));

        // Draw intersect area.
        g2d.setColor(dotsColor);
        g2d.fill(intersectArea);

        // Draw arc bounds.
        g2d.setColor(Color.BLACK);
        int strokeWidth = (int) (toDrawSize(saveLoadManager.getPropInt("dotsStroke")) * 0.01);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(arcsArea);
    }

    /**
     * @return 0.01 * resolutionX * val
     */
    private int toDrawSize(int val) {
        return (int) (val * saveLoadManager.getPropInt("resolutionX") * 0.01);
    }
}