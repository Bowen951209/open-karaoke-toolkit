package net.bowen;

import net.bowen.gui.LineNumberedScrollableTextArea;
import net.bowen.gui.Viewport;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private Viewport viewport;

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
        viewport = new Viewport();
        add(viewport, BorderLayout.CENTER);
    }

    public void addLyricsTextPanel() {
        JPanel lyricsPanel = new JPanel();
        lyricsPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        LineNumberedScrollableTextArea textArea = new LineNumberedScrollableTextArea(
                new Dimension((int) (this.getSize().width / 5f), (int) (this.getSize().height * 0.9f
                )));
        textArea.addDocumentUpdateCallback(()->{
            viewport.setDisplayString(textArea.getText());
            viewport.repaint();
        });
        textArea.setText("請由此編輯!");

        lyricsPanel.add(textArea);
        add(lyricsPanel, BorderLayout.LINE_START);
    }

    public static void main(String[] args) {
        new Main("Open Karaoke Toolkit");
    }
}