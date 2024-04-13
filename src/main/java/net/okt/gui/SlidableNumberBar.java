package net.okt.gui;

import net.okt.system.SaveLoadManager;
import net.okt.system.SimpleDocumentListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import java.util.Set;

public class SlidableNumberBar extends JPanel {
    private final int maxValue;
    private final TextFieldLimit textField;
    private final Set<Runnable> docListeners = new HashSet<>();

    private String propKey;
    private SaveLoadManager saveLoadManager;
    private int dragStep = 1;
    private int oVal;
    private int val;
    private int mouseLastX;

    public void setDragStep(int v) {
        this.dragStep = v;
    }

    public int getVal() {
        return val;
    }

    /**
     * @param limitDigit limit digit of font size.
     */
    public SlidableNumberBar(String text, int limitDigit, int defaultVal) {
        super(new FlowLayout(FlowLayout.LEFT));
        textField = new TextFieldLimit(true, limitDigit, String.valueOf(defaultVal));
        this.val = defaultVal;
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
            if (!textField.getText().isEmpty()) {
                val = Integer.parseInt(textField.getText());
                if (propKey != null) saveLoadManager.setProp(propKey, val);

                docListeners.forEach(Runnable::run);
            }
        }));

        JLabel label = new JLabel(text);
        add(label);
        add(textField);
    }

    /**
     * @param limitDigit limit digit of font size.
     */
    public SlidableNumberBar(String text, int limitDigit, String propKey, SaveLoadManager saveLoadManager) {
        this(text, limitDigit, saveLoadManager.getPropInt(propKey));
        this.propKey = propKey;
        this.saveLoadManager = saveLoadManager;
    }

    public void addDocumentListener(Runnable e) {
        docListeners.add(e);
    }
}
