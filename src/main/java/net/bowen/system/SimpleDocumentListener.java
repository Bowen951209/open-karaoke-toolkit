package net.bowen.system;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A simpler document listener that insert, remove, and changed all do the same thing.
 * */
public class SimpleDocumentListener implements DocumentListener {
    private final Runnable r;

    public SimpleDocumentListener(Runnable r) {
        this.r = r;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        r.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        r.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        r.run();
    }
}
