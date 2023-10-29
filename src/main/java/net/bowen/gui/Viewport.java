package net.bowen.gui;

import javax.swing.*;
import java.awt.*;

public class Viewport extends JPanel {
    private String displayString;

    public void setDisplayString(String s) {
        this.displayString = s;
    }

    public Viewport() {
        super();
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawArc(0, 0, 50, 50, 0, 360);

        if (displayString != null) {
            String[] strings = displayString.split("\n");
            for (int i = 0; i < strings.length; i++) {
                g.drawString(strings[i], 50, (i + 1) * 20);
            }
        }
    }

}
