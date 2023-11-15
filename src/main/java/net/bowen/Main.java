package net.bowen;

import net.bowen.gui.LineNumberedScrollableTextArea;
import net.bowen.gui.Viewport;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private final JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final Viewport viewport = new Viewport();
    private final LineNumberedScrollableTextArea textArea = new LineNumberedScrollableTextArea();

    private Main(String title) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));
        setLayout(new BorderLayout());

        add(mainSplitPane);

        addTextPanel();
        addViewPort();

        setVisible(true);
    }

    private void addViewPort() {
        mainSplitPane.add(viewport);
    }

    public void addTextPanel() {
        textArea.setPreferredSize(new Dimension((int) (getWidth() * .2f), getHeight()));
        textArea.addDocumentUpdateCallback(()->{
            viewport.setDisplayString(textArea.getText());
            viewport.repaint();
        });
        textArea.setText("""
                請由此編輯!
                這是第二行"""); // I use Chinese for just now, sorry to English speaker :)

        new Timer(10, (e)-> viewport.repaint()).start();

        mainSplitPane.add(textArea);
    }

    public static void main(String[] args) {
        new Main("Open Karaoke Toolkit");
    }
}