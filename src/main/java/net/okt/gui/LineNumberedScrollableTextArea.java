package net.okt.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class LineNumberedScrollableTextArea extends JScrollPane {
    private final JTextArea textArea;
    private final UndoManager undoManager = new UndoManager();
    private final Set<Runnable> undoListeners = new HashSet<>();
    private final Set<Runnable> redoListeners = new HashSet<>();

    private Set<Runnable> documentUpdateCallbacks;

    public LineNumberedScrollableTextArea() {
        super();
        textArea = new JTextArea(); // The component where you can type text.

        setViewportView(textArea); // Wrap the text area with scroll pane, so it becomes scrollable.

        // View line number before each line.
        documentUpdateCallback();

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentUpdateCallback();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentUpdateCallback();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentUpdateCallback();
            }
        });

        textArea.getDocument().addUndoableEditListener(undoManager);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    // Ctrl Z: Undo
                    if (e.getKeyCode() == KeyEvent.VK_Z && undoManager.canUndo()) {
                        undoManager.undo();
                        undoListeners.forEach(Runnable::run);
                    }
                    //Ctrl Z: Redo
                    else if (e.getKeyCode() == KeyEvent.VK_Y && undoManager.canRedo()) {
                        undoManager.redo();
                        redoListeners.forEach(Runnable::run);
                    }
                }
            }
        });

        setRowHeaderView(new TextLineNumber(textArea));

        setMinimumSize(getPreferredSize());
    }

    public void addUndoListener(Runnable e) {
        undoListeners.add(e);
    }

    public void addRedoListener(Runnable e) {
        redoListeners.add(e);
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String s) {
        textArea.setText(s);
    }

    public void addDocumentUpdateCallback(Runnable e) {
        if (documentUpdateCallbacks == null) documentUpdateCallbacks = new HashSet<>();
        documentUpdateCallbacks.add(e);
    }

    private void documentUpdateCallback() {
        if (documentUpdateCallbacks != null) documentUpdateCallbacks.forEach(Runnable::run);
    }
}
