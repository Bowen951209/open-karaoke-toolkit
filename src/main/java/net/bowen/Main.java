package net.bowen;

import net.bowen.gui.LineNumberedScrollableTextArea;
import net.bowen.gui.Timeline;
import net.bowen.gui.Viewport;
import net.bowen.system.SaveLoadManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class Main extends JFrame {
    private final Viewport viewport = new Viewport();
    private final SaveLoadManager saveLoadManager = new SaveLoadManager(this);

    private Main(String title) {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));
        setLayout(new BorderLayout());

        addMenuBar();

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
        Timeline timeline = new Timeline(saveLoadManager);
        targetComponent.add(timeline);
    }

    private void addMenuBar() {
        // The menu bar
        JMenuBar menuBar = new JMenuBar();


        // -- File Menu --
        JMenu fileMenu = new JMenu("File");
        // load audio
        JMenuItem loadAudio = new JMenuItem("Load Audio");
        FileNameExtensionFilter wavExtensionFilter = new FileNameExtensionFilter("*.wav", "wav");
        JFileChooser audioFileChooser = new JFileChooser();
        audioFileChooser.setFileFilter(wavExtensionFilter);
        loadAudio.addActionListener((e) -> {
            if (audioFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                System.out.println(audioFileChooser.getSelectedFile());
                saveLoadManager.setLoadedAudio(audioFileChooser.getSelectedFile());
            }
        });
        saveLoadManager.setLoadedAudio(getClass().getResource("/audios/LiuLongKid.wav")); // for temporary test

        fileMenu.add(loadAudio);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        new Main("Open Karaoke Toolkit");
    }
}