package net.okt.gui;

import net.okt.system.LyricsArea;
import net.okt.system.LyricsProcessor;
import net.okt.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Viewport extends JPanel {
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final AffineTransform ZERO_TRANSFORM = AffineTransform.getScaleInstance(0, 0);

    private final SaveLoadManager saveLoadManager;
    private final LyricsProcessor lyricsProcessor;
    private final LyricsArea[] displayingAreas = new LyricsArea[2];
    private final GlyphVector[] displayingGlyphVectors = new GlyphVector[2];

    private BufferedImage bufferedImage;

    public Viewport(SaveLoadManager saveLoadManager, LyricsProcessor lyricsProcessor) {
        super();
        this.saveLoadManager = saveLoadManager;
        this.lyricsProcessor = lyricsProcessor;
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 1));
        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.LIGHT_GRAY);
    }

    /**
     * Get the glyph vector of the passed in string.
     * If the string contains a link word, it'll be applied the linkTransform.
     */
    private static GlyphVector getGlyphVector(String s, AffineTransform linkTransform, Font font) {
        GlyphVector glyphVector = font.createGlyphVector(FRC, s);
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
            } else if (currentChar == '_') {
                glyphVector.setGlyphTransform(i, ZERO_TRANSFORM);
                i++;
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
     * Update {@link #displayingAreas} and {@link #displayingGlyphVectors}. Use cache if not forceUpdate.
     *
     * @param forceUpdate If you want to force update even if the displaying lines are the same.
     */
    public void updateDisplayingAreas(boolean forceUpdate) {
        int[] displayingLines = lyricsProcessor.getDisplayingLines();

        if (forceUpdate) {
            displayingGlyphVectors[0] = getGlyphVectorAtLine(displayingLines[0]);
            displayingGlyphVectors[1] = getGlyphVectorAtLine(displayingLines[1]);

            displayingAreas[0] = getAreaAtLine(displayingGlyphVectors[0], displayingLines[0]);
            displayingAreas[1] = getAreaAtLine(displayingGlyphVectors[1], displayingLines[1]);
        } else {
            if (displayingAreas[0] == null || displayingAreas[0].line != displayingLines[0]) {
                displayingGlyphVectors[0] = getGlyphVectorAtLine(displayingLines[0]);
                displayingAreas[0] = getAreaAtLine(displayingGlyphVectors[0], displayingLines[0]);
            }

            if (displayingAreas[1] == null || displayingAreas[1].line != displayingLines[1]) {
                displayingGlyphVectors[1] = getGlyphVectorAtLine(displayingLines[1]);
                displayingAreas[1] = getAreaAtLine(displayingGlyphVectors[1], displayingLines[1]);
            }
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
        topIntersectArea.intersect(getRectangleArea(displayingGlyphVectors[0], lyricsProcessor.getDisplayingLines()[0], time));
        bottomIntersectArea.intersect(getRectangleArea(displayingGlyphVectors[1], lyricsProcessor.getDisplayingLines()[1], time));

        int baseStrokeWidth = (int) (toDrawSize(saveLoadManager.getPropInt("textStroke")) * 0.01);
        Stroke baseStroke = new BasicStroke(baseStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        int intersectStrokeWidth = (int) (toDrawSize(saveLoadManager.getPropInt("intersectStroke")) * 0.01);
        Stroke intersectStroke = new BasicStroke(intersectStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

        Color textColor = new Color(saveLoadManager.getPropInt("textColor"));
        Color intersectStrokeColor = new Color(saveLoadManager.getPropInt("intersectStrokeColor"));

        // ----- The top line. -----
        // base fill
        g2d.setColor(Color.WHITE);
        g2d.fill(topFontArea);

        // base outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(baseStroke);
        g2d.draw(topFontArea);

        // intersect area outline
        g2d.setColor(intersectStrokeColor);
        g2d.setStroke(intersectStroke);
        g2d.draw(topIntersectArea);

        // intersect area fill
        g2d.setColor(textColor);
        g2d.fill(topIntersectArea);


        // Translate position to the bottom line.
        g2d.translate(secondLineIndent, lineSpace);

        // ----- The bottom line. -----
        // base fill
        g2d.setColor(Color.WHITE);
        g2d.fill(bottomFontArea);

        // base outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(baseStroke);
        g2d.draw(bottomFontArea);

        // intersect area outline
        g2d.setColor(intersectStrokeColor);
        g2d.setStroke(intersectStroke);
        g2d.draw(bottomIntersectArea);

        // intersect area fill
        g2d.setColor(textColor);
        g2d.fill(bottomIntersectArea);
    }

    /**
     * @param line The line index in the lyrics.
     * @return The glyph vector at the given line.
     */
    private GlyphVector getGlyphVectorAtLine(int line) {
        var lyricsLines = lyricsProcessor.getLyricsLines();
        if (lyricsLines == null || line == -1 || line >= lyricsLines.size())
            return null;

        int defaultFontSize = saveLoadManager.getPropInt("defaultFontSize");
        int linkedFontSize = saveLoadManager.getPropInt("linkedFontSize");
        float linkScale = (float) linkedFontSize / defaultFontSize;

        AffineTransform linkScaleTransform = AffineTransform.getScaleInstance(linkScale, linkScale);
        String string = lyricsProcessor.getLyricsLines().get(line);

        return getGlyphVector(string, linkScaleTransform, getFont());
    }

    private LyricsArea getAreaAtLine(GlyphVector glyphVector, int line) {
        var lyricsLines = lyricsProcessor.getLyricsLines();
        if (lyricsLines == null || line == -1 || line >= lyricsLines.size())
            return new LyricsArea(new Area(), -1);

        LyricsArea area = new LyricsArea(glyphVector.getOutline(), line);
        area.transform(getDefaultScaleTransform());

        return area;
    }

    /**
     * Get the rectangle area that is used to intersect with glyph vectors.
     * 
     * @param glyphVector The glyph vector at given the line.
     * @param line The line index in the lyrics.
     * @param time The current play time.
     * @return The rectangle as an area.
     */
    private Area getRectangleArea(GlyphVector glyphVector, int line, int time) {
        var marks = saveLoadManager.getMarks();
        int lineStartMark = lyricsProcessor.getStartMarkAtLine(line);
        if (lineStartMark >= marks.size()) return new Area();

        // If the time is before the line start mark,
        // there should not be any progress of rectangle, return empty area.
        if (time < marks.get(lineStartMark)) return new Area();

        int nextMark = lyricsProcessor.getNextMark(time); // the nearest mark after time
        int nextMarkTime = nextMark < marks.size() ? marks.get(nextMark) : Integer.MAX_VALUE;
        int lastMarkTime = nextMark == 0 ? 0 : marks.get(nextMark - 1);

        int numFullGlyph = 0; // the number of glyphs that should be full-filled.
        for (int i = lineStartMark + 1; i < nextMark; i++) {
            String textBeforeMark = lyricsProcessor.getTextBeforeMark(i);
            if (textBeforeMark == null) return new Area();

            if (LyricsProcessor.isEasternChar(textBeforeMark.charAt(0))) { // eastern
                // If it's an eastern word, it should be either a single word or a link word.
                // If it's a link word, remember to add 1 glyph num for the symbol "'".
                // (The word "一'二" displays as "一二" in textBeforeMark, but actual length is 3.)
                numFullGlyph += textBeforeMark.length() == 2 ? 3 : 1;
            } else { // western
                // If it's a western word, it should be either a single word or a sep word.
                if (textBeforeMark.charAt(0) == '_') // if is sep word.
                    numFullGlyph += textBeforeMark.length(); // plus 1 space and minus 1 underscore.
                else
                    numFullGlyph += textBeforeMark.length() + 1; // plus 1 space, or 1 underscore.
                
                // Take the line "aaa bb_cc ddd" for example, we should add "aaa ", "bb_", "cc "... in order.
            }

            // If numFullGlyph is full, set it to max number for the ease of further judging.
            if (numFullGlyph >= glyphVector.getNumGlyphs()) {
                numFullGlyph = glyphVector.getNumGlyphs();
                break;
            }
        }

        // The percentage of how much the end glyph should be filled.
        float endPercentage = (float) (time - lastMarkTime) / (nextMarkTime - lastMarkTime);

        // The end glyph refers to the glyph that should be filled but not 100%, and it is the one right after the last
        // full-filled glyph.
        // The end glyph index is equal to numFullGlyph.
        // For example, if numFullGlyph is 10, the end glyph is at index 10.
        int endGlyph = numFullGlyph;

        // The string the end glyph is in.
        String endString = lyricsProcessor.getTextBeforeMark(nextMark);
        int endStringLength;
        if (endString == null) {
            endStringLength = 0;
        } else {
            if (LyricsProcessor.isLinkWord(endString))
                endStringLength = endString.length() + 1; // A link word should add 1 for the symbol "'".
            else if (LyricsProcessor.isSepWord(endString))
                endStringLength = endString.length() - 1; // A sep word should minus 1 for the symbol "_".
            else
                endStringLength = endString.length(); // Normal word.
        }


        double boundsStartX; // where the full-filled rectangle should end.
        double boundsWidth; // the width of the end word.
        if (endGlyph == glyphVector.getNumGlyphs()) {
            // This line should be whole-line-filled.
            Rectangle2D endBounds = glyphVector.getGlyphOutline(endGlyph - 1).getBounds2D();

            boundsStartX = endBounds.getX();
            boundsWidth = endBounds.getWidth();
            endPercentage = 1;
        } else {
            Shape startGlyph = glyphVector.getGlyphOutline(endGlyph);
            // The end of this word. For example, "ab cde fg", if the end glyph is "c", boundsWidth is at index of "e".
            Rectangle2D endBounds = glyphVector.getGlyphOutline(Math.max(0, endGlyph + endStringLength - 1)).getBounds2D();

            // Transfer the startGlyph to an Area object, because the original bounds got from the glyph would be wrong
            // in some particular fonts, and turning it to an Area object solves the problem. (See #52.)
            boundsStartX = new Area(startGlyph).getBounds2D().getX();
            boundsWidth = endBounds.getMaxX() - boundsStartX;
        }

        double width = boundsStartX + endPercentage * boundsWidth; // the final width of the rectangle.


        Rectangle2D lineBounds = glyphVector.getOutline().getBounds2D();
        Rectangle2D rect = new Rectangle2D.Double(
                0,
                -lineBounds.getHeight(),
                width,
                lineBounds.getHeight() * 2
        ); // the y and the height are actually rough numbers, but can make sure the line is well-covered.

        Area area = new Area(rect);
        area.transform(getDefaultScaleTransform());
        return area;
    }

    private AffineTransform getDefaultScaleTransform() {
        int defaultFontSize = saveLoadManager.getPropInt("defaultFontSize");
        float defaultScale = toDrawSize(defaultFontSize);
        return AffineTransform.getScaleInstance(defaultScale, defaultScale);
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