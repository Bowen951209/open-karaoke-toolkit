package net.bowen;

import net.bowen.gui.LineNumberedScrollableTextArea;
import net.bowen.gui.Timeline;
import net.bowen.gui.Viewport;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private final Viewport viewport = new Viewport();

    private Main(String title) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));
        setLayout(new BorderLayout());

        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.setPreferredSize(new Dimension(getWidth(), (int) (getHeight() * .7f)));
        addTextArea(topSplitPane);
        addViewPort(topSplitPane);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.add(topSplitPane);
        addTimeline(mainSplitPane);

        add(mainSplitPane);


        setVisible(true);
    }

    private void addViewPort(JComponent targetComponent) {
        targetComponent.add(viewport);
    }

    private void addTextArea(JComponent targetComponent) {
        LineNumberedScrollableTextArea textArea = new LineNumberedScrollableTextArea();

        textArea.addDocumentUpdateCallback(() -> {
            viewport.setDisplayString(textArea.getText());
            viewport.repaint();
        });
        textArea.setText("""
                請由此編輯!
                這是第二行"""); // I use Chinese for just now, sorry to English speaker :)

        new Timer(10, (e) -> viewport.repaint()).start();

        targetComponent.add(textArea);
    }

    private void addTimeline(JComponent targetComponent) {
        Timeline timeline = new Timeline();
        targetComponent.add(timeline);
    }

    public static void main(String[] args) {
        new Main("Open Karaoke Toolkit");
    }
}