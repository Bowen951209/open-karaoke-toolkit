package net.okt.gui;

import javax.swing.*;
import java.awt.*;

public class TitleLabel extends JLabel {
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 13);

    public TitleLabel(String text) {
        super(text);
        setAlignmentX(CENTER_ALIGNMENT);
        setFont(TITLE_FONT);
    }
}
