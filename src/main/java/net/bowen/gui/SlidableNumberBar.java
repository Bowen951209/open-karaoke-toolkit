package net.bowen.gui;

import net.bowen.system.SimpleDocumentListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SlidableNumberBar extends JPanel {
    private final int maxValue;
    private final JTextFieldLimit textField;

    protected int val;

    private int oVal;
    private int mouseLastX;

    public void setValue(int val) {
        this.val = val;
        textField.setText(String.valueOf(val));
    }

    public int getVal() {
        return val;
    }

    public void addDocumentListener(Runnable e) {
        textField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            // If the text is not empty, convert the string to int value.
            if (!textField.getText().isEmpty())
                val = Integer.parseInt(textField.getText());

            // The passed in listener.
            e.run();
        }));
    }

    /**
     * @param limitDigit limit digit of font size.
     */
    public SlidableNumberBar(int limitDigit, String text) {
        super(new FlowLayout(FlowLayout.LEFT));
        textField = new JTextFieldLimit(true, limitDigit, "-1");
        this.maxValue = (int) (Math.pow(10, limitDigit) - 1);

        // Set the size.
        setPreferredSize(new Dimension(150, 30));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

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
                int delta = e.getX() - mouseLastX;
                val = Math.max(delta + oVal, 0);
                val = Math.min(val, maxValue);
                oVal = val;
                textField.setText(String.valueOf(val));
                mouseLastX = e.getX();
            }
        });

        JLabel label = new JLabel(text);
        add(label);
        add(textField);
    }
}
