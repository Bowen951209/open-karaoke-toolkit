package net.bowen;

import net.bowen.gui.LineNumberedScrollableTextArea;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private Main(String title) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));
        setLayout(new BorderLayout());

        addViewPort();
        addLyricsTextPanel();

        setVisible(true);
    }

    private void addViewPort() {
        JPanel viewportPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawArc(0, 0, 50, 50, 0, 360);
            }
        };
        viewportPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        add(viewportPanel, BorderLayout.CENTER);
    }

    public void addLyricsTextPanel() {
        JPanel lyricsPanel = new JPanel();
        lyricsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        lyricsPanel.add(new LineNumberedScrollableTextArea(
                new Dimension((int) (this.getSize().width / 5f), (int) (this.getSize().height * 0.9f)
                )));
        add(lyricsPanel, BorderLayout.LINE_START);
    }

    public static void main(String[] args) {
        new Main("Open Karaoke Toolkit");
    }
}