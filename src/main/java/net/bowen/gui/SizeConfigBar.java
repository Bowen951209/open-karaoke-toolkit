package net.bowen.gui;

import net.bowen.system.SimpleDocumentListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public abstract class SizeConfigBar extends JPanel {
    private final int maxValue;
    private final JTextFieldLimit textField;

    protected int size;

    private int oVal;
    private int mouseLastX;

    public void set(int size) {
        this.size = size;
        textField.setText(String.valueOf(size));
    }

    /**
     * @param limitDigit limit digit of font size.
     */
    public SizeConfigBar(int limitDigit, String text) {
        textField = new JTextFieldLimit(true, limitDigit, "-1");
        this.maxValue = (int) (Math.pow(10, limitDigit) - 1);

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
                size = Math.max(delta + oVal, 0);
                size = Math.min(size, maxValue);
                oVal = size;
                textField.setText(String.valueOf(size));
                mouseLastX = e.getX();
            }
        });

        textField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if (!textField.getText().isEmpty())
                size = Integer.parseInt(textField.getText());

            documentCallback();
        }));

        JLabel label = new JLabel(text);
        add(label);
        add(textField);
    }

    public abstract void documentCallback();
}
