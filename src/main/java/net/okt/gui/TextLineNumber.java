package net.okt.gui;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;


/**
 * This class will display line numbers for a related text component. The text
 * component must use the same line height for each line. TextLineNumber
 * supports wrapped lines and will highlight the line number of the current
 * line in the text component.
 * <p>
 * This class was designed to be used as a component added to the row header
 * of a JScrollPane.
 * <p>
 * Modified from <a href="https://github.com/tips4java/tips4java/blob/main/source/TextLineNumber.java">here</a>
 */
public class TextLineNumber extends JPanel implements CaretListener, DocumentListener {
    private static final float DIGIT_ALIGNMENT = 1.0f;

    private final JTextComponent component;
    private Color currentLineForeground;
    private int minimumDisplayDigits;

    private int lastDigits;
    private int lastHeight;
    private int lastLine;
    private HashMap<String, FontMetrics> fonts;

    /**
     * Create a line number component for a text component. This minimum
     * display width will be based on 3 digits.
     *
     * @param component the related text component
     */
    public TextLineNumber(JTextComponent component) {
        this(component, 3);
    }

    /**
     * Create a line number component for a text component.
     *
     * @param component            the related text component
     * @param minimumDisplayDigits the number of digits used to calculate
     *                             the minimum width of the component
     */
    public TextLineNumber(JTextComponent component, int minimumDisplayDigits) {
        this.component = component;

        setFont(component.getFont());

        setPreferredSize();
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.GRAY));
        setCurrentLineForeground(Color.RED);
        setMinimumDisplayDigits(minimumDisplayDigits);

        component.getDocument().addDocumentListener(this);
        component.addCaretListener(this);
    }

    /**
     * Gets the current line rendering Color
     *
     * @return the Color used to render the current line number
     */
    public Color getCurrentLineForeground() {
        return currentLineForeground == null ? getForeground() : currentLineForeground;
    }

    /**
     * The Color used to render the current line digits. Default is Color.RED.
     *
     * @param currentLineForeground the Color used to render the current line
     */
    public void setCurrentLineForeground(Color currentLineForeground) {
        this.currentLineForeground = currentLineForeground;
    }

    /**
     * Specify the minimum number of digits used to calculate the preferred
     * width of the component. Default is 3.
     *
     * @param minimumDisplayDigits the number digits used in the preferred
     *                             width calculation
     */
    public void setMinimumDisplayDigits(int minimumDisplayDigits) {
        this.minimumDisplayDigits = minimumDisplayDigits;
        setPreferredSize();
    }

    /**
     * Calculate the size needed to display the maximum line number.
     */
    private void setPreferredSize() {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);
        Dimension d = getPreferredSize();
        Insets insets = getInsets();
        FontMetrics fontMetrics = getFontMetrics(getFont());

        int preferredWidth = (int) d.getWidth();

        // Update the height every time.
        int lineHeight = fontMetrics.getHeight(); // Get the height of a single line
        int preferredHeight = insets.top + insets.bottom + (lineHeight * lines);

        //  Update sizes when number of digits in the line number changes
        if (lastDigits != digits) {
            lastDigits = digits;
            int width = fontMetrics.charWidth('0') * digits;
            preferredWidth = insets.left + insets.right + width;
        }

        d.setSize(preferredWidth, preferredHeight);
        setPreferredSize(d);
        setSize(d);
    }

    /**
     * Draw the line numbers
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //	Determine the width of the space available to draw the line number.
        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;

        //  Determine the rows to draw within the clipped bounds.
        Rectangle clip = g2d.getClipBounds();
        int rowStartOffset = component.viewToModel2D(new Point(0, clip.y));
        int endOffset = component.viewToModel2D(new Point(0, clip.y + clip.height));

        while (rowStartOffset <= endOffset) {
            try {
                if (isCurrentLine(rowStartOffset))
                    g2d.setColor(getCurrentLineForeground());
                else
                    g2d.setColor(getForeground());

                //  Get the line number as a string and then determine the
                //  "X" and "Y" offsets for drawing the string.
                String lineNumber = getTextLineNumber(rowStartOffset);
                int stringWidth = fontMetrics.stringWidth(lineNumber);
                int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                int y = getOffsetY(rowStartOffset, fontMetrics);
                g2d.drawString(lineNumber, x, y);

                //  Move to the next row.
                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     *  We need to know if the caret is currently positioned on the line we
     *  are about to paint so the line number can be highlighted.
     */
    private boolean isCurrentLine(int rowStartOffset) {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        return root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition);
    }

    /*
     *	Get the line number to be drawn. The empty string will be returned
     *  when a line of text has wrapped.
     */
    protected String getTextLineNumber(int rowStartOffset) {
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex(rowStartOffset);
        Element line = root.getElement(index);

        if (line.getStartOffset() == rowStartOffset)
            return String.valueOf(index + 1);
        else
            return "";
    }

    /*
     *  Determine the X offset to properly align the line number when drawn
     */
    private int getOffsetX(int availableWidth, int stringWidth) {
        return (int) ((availableWidth - stringWidth) * DIGIT_ALIGNMENT);
    }

    /*
     *  Determine the Y offset for the current row
     */
    private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics) throws BadLocationException {
        //  Get the bounding rectangle of the row

        Rectangle2D r = component.modelToView2D(rowStartOffset);
        int lineHeight = fontMetrics.getHeight();
        int y = (int) (r.getY() + r.getHeight());
        int descent = 0;

        //  The text needs to be positioned above the bottom of the bounding
        //  rectangle based on the descent of the font(s) contained on the row.

        if (r.getHeight() == lineHeight)  // default font is being used
        {
            descent = fontMetrics.getDescent();
        } else  // We need to check all the attributes for font changes
        {
            if (fonts == null)
                fonts = new HashMap<>();

            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex(rowStartOffset);
            Element line = root.getElement(index);

            for (int i = 0; i < line.getElementCount(); i++) {
                Element child = line.getElement(i);
                AttributeSet as = child.getAttributes();
                String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
                Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
                String key = fontFamily + fontSize;

                FontMetrics fm = fonts.get(key);

                if (fm == null) {
                    Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    fm = component.getFontMetrics(font);
                    fonts.put(key, fm);
                }

                descent = Math.max(descent, fm.getDescent());
            }
        }

        return y - descent;
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        //  Get the line the caret is positioned on

        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex(caretPosition);

        //  Need to repaint so the correct line number can be highlighted

        if (lastLine != currentLine) {
            getParent().repaint();
            lastLine = currentLine;
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged();
    }

    /*
     *  A document change may affect the number of displayed lines of text.
     *  Therefore the lines numbers will also change.
     */
    private void documentChanged() {
        //  View of the component has not been updated at the time
        //  the DocumentEvent is fired

        SwingUtilities.invokeLater(() -> {
            try {
                int endPos = component.getDocument().getLength();
                Rectangle2D rect = component.modelToView2D(endPos);

                if (rect != null && rect.getY() != lastHeight) {

                    setPreferredSize();
                    getParent().repaint();
                    lastHeight = (int) rect.getY();
                }
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}