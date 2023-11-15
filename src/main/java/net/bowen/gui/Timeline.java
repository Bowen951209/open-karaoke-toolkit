package net.bowen.gui;

import javax.swing.*;
import java.awt.*;

public class Timeline extends JPanel {
    // TODO: 2023/11/15 Implement this class.
    public Timeline() {
        super();
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
