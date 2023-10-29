package net.bowen.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class LineNumberedScrollableTextArea extends JScrollPane {
    private final JTextArea textArea, lineHead;
    private final StringBuilder lineStringBuilder;
    private Set<Runnable> documentUpdateCallbacks;
    private int maxLine;

    public String getText() {
        return textArea.getText();
    }

    public void addDocumentUpdateCallback(Runnable e) {
        if (documentUpdateCallbacks == null) documentUpdateCallbacks = new HashSet<>();
        documentUpdateCallbacks.add(e);
    }

    // Reference: https://www.tutorialspoint.com/how-can-we-display-the-line-numbers-inside-a-jtextarea-in-java
    public LineNumberedScrollableTextArea(Dimension preferredSize) {
        super();
        textArea = new JTextArea(); // The component where you can type text.


        setViewportView(textArea); // Wrap the text area with scroll pane, so it becomes scrollable.
        setPreferredSize(preferredSize);

        // View line number before each line.
        lineHead = new JTextArea();
        lineHead.setEditable(false);
        lineHead.setBackground(Color.BLACK);
        lineHead.setForeground(Color.WHITE);
        lineStringBuilder = new StringBuilder();
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

        setRowHeaderView(lineHead);
    }

    private void documentUpdateCallback() {
        refreshLineHead();

        if (documentUpdateCallbacks != null) documentUpdateCallbacks.forEach(Runnable::run);
    }

    private void refreshLineHead() {
        int caretPosition = textArea.getDocument().getLength();
        Element root = textArea.getDocument().getDefaultRootElement();
        int currentMaxLine = root.getElementIndex(caretPosition) + 1;

        // if smaller than previous max
        if (currentMaxLine < maxLine) {
            lineStringBuilder.delete(
                    lineStringBuilder.indexOf(String.valueOf(currentMaxLine + 1)),
                    lineStringBuilder.length()
            );
            lineHead.setText(lineStringBuilder.toString());
            maxLine = currentMaxLine;
            return;
        }

        // if bigger than previous max
        while (currentMaxLine > maxLine) {
            maxLine++;
            lineStringBuilder.append(maxLine).append("\n");
        }
        lineHead.setText(lineStringBuilder.toString());
    }
}
