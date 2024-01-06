package net.bowen.gui;

import net.bowen.system.SaveLoadManager;
import net.bowen.system.SimpleDocumentListener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class FontSizeConfigBar extends JTextFieldLimit {
    private int oVal, size;

    /**
     * @param limitDigit limit digit of font size.
     */
    public FontSizeConfigBar(int limitDigit, int defaultSize, SaveLoadManager saveLoadManager, Viewport viewport) {
        super(true, limitDigit, String.valueOf(defaultSize));

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
            saveLoadManager.setFontSize(size);
            viewport.setDefaultFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getFontSize()));
            viewport.repaint();
        }));
    }
}
