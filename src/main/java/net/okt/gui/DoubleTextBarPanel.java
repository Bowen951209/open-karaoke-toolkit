package net.okt.gui;

import javax.swing.*;
import java.awt.*;

public class DoubleTextBarPanel extends JPanel {
    public DoubleTextBarPanel(String title, SlidableNumberBar bar1, SlidableNumberBar bar2) {
        setMaximumSize(new Dimension(500, 10));
        add(new JLabel(title));
        add(bar1);
        add(bar2);
    }
}
