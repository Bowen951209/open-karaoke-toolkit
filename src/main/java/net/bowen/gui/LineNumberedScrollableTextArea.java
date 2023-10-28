package net.bowen.gui;

import javax.swing.*;
import java.awt.*;

public class LineNumberedScrollableTextArea extends JScrollPane {

    public LineNumberedScrollableTextArea(Dimension preferredSize) {
        super();
        JTextArea textArea = new JTextArea(); // The component where you can type text.


        setViewportView(textArea); // Wrap the text area with scroll pane, so it becomes scrollable.
        setPreferredSize(preferredSize);

        // View line number before each line.
        JTextArea lineHead = new JTextArea();
        lineHead.setEditable(false);
        lineHead.setBackground(Color.BLACK);
        lineHead.setForeground(Color.WHITE);
        StringBuilder lineString = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            lineString.append(i).append("\n");
        }
        lineHead.setText(lineString.toString());

        setRowHeaderView(lineHead);
    }
}
