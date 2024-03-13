package net.okt.gui;

import net.okt.system.SimpleDocumentListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SlidableNumberBar extends JPanel {
    private final int maxValue;
    private final JTextFieldLimit textField;


    private int dragStep = 1;
    private int oVal;
    private int val;
    private int mouseLastX;

    public void setValue(int val) {
        this.val = val;
        textField.setText(String.valueOf(val));
    }

    public void setDragStep(int v) {
        this.dragStep = v;
    }

    public int getVal() {
        return val;
    }

    public void addDocumentListener(DocListener e) {
        textField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            // The passed in listener.
            e.run(this);
        }));
    }

    /**
     * @param limitDigit limit digit of font size.
     */
    public SlidableNumberBar(int defaultVal, int limitDigit, String text) {
        super(new FlowLayout(FlowLayout.LEFT));
        textField = new JTextFieldLimit(true, limitDigit, String.valueOf(defaultVal));
        this.val = defaultVal;
        this.maxValue = (int) (Math.pow(10, limitDigit) - 1);

        // Set the size.
        fixSize(150);

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Cursor hMoveCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                textField.setCursor(hMoveCursor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Cursor hMoveCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                setCursor(hMoveCursor);

                oVal = Integer.parseInt(textField.getText());
                mouseLastX = e.getX();
            }
        });

        textField.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                requestFocus(); // *This is for even mouse is outside the component, drag callback still calls.
                int delta = (e.getX() - mouseLastX) * dragStep;
                val = Math.max(delta + oVal, 0);
                val = Math.min(val, maxValue);
                oVal = val;
                textField.setText(String.valueOf(val));
                mouseLastX = e.getX();
            }
        });

        textField.getDocument().addDocumentListener(new SimpleDocumentListener(()->{
            // Convert text to value
            if (!textField.getText().isEmpty())
                val = Integer.parseInt(textField.getText());
        }));

        JLabel label = new JLabel(text);
        add(label);
        add(textField);
    }

    /**
     * @param limitDigit limit digit of font size.
     */
    public SlidableNumberBar(int limitDigit, String text) {
        this(-1, limitDigit, text);
    }

    public void fixSize(int width) {
        setPreferredSize(new Dimension(width, 30));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
    }

    public interface DocListener  {
        void run(SlidableNumberBar bar);
    }
}
