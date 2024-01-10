package net.bowen.gui;

import net.bowen.system.SimpleDocumentListener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public abstract class FontSizeConfigBar extends JTextFieldLimit {
    protected int size;
    private int oVal;

    public void set(int size) {
        this.size = size;
        setText(String.valueOf(size));
    }

    /**
     * @param limitDigit limit digit of font size.
     */
    public FontSizeConfigBar(int limitDigit) {
        super(true, limitDigit, "-1");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Cursor hMoveCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                setCursor(hMoveCursor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Cursor hMoveCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                setCursor(hMoveCursor);

                oVal = Integer.parseInt(getText());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                requestFocus(); // *This is for even mouse is outside the component, drag callback still calls.
                int delta = e.getX() / 10;
                size = Math.max(delta + oVal, 0);
                size = Math.min(size, 999);
                setText(String.valueOf(size));
            }
        });

        getDocument().addDocumentListener(new SimpleDocumentListener(()-> {
            if (!getText().isEmpty())
                size = Integer.parseInt(getText());

            documentCallback();
        }));
    }

    public abstract void documentCallback();
}
