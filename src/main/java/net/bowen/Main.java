package net.bowen;

import net.bowen.gui.LineNumberedScrollableTextArea;
import net.bowen.gui.Viewport;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private final JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final Viewport viewport = new Viewport();

    private Main(String title) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));
        setLayout(new BorderLayout());

        add(mainSplitPane);

        addLyricsTextPanel();
        addViewPort();

        setVisible(true);
    }

    private void addViewPort() {
        mainSplitPane.add(viewport);
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
        textArea.setText("""
                請由此編輯!
                這是第二行"""); // I use Chinese for just now, sorry to English speaker :)

        new Timer(10, (e)-> viewport.repaint()).start();

        lyricsPanel.add(textArea);
//        add(lyricsPanel, BorderLayout.LINE_START);
        mainSplitPane.add(lyricsPanel);
    }

    public static void main(String[] args) {
        new Main("Open Karaoke Toolkit");
    }
}