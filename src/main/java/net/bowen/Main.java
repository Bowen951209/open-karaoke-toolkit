package net.bowen;

import net.bowen.gui.LineNumberedScrollableTextArea;
import net.bowen.gui.Timeline;
import net.bowen.gui.Viewport;
import net.bowen.system.SaveLoadManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class Main extends JFrame {
    public static final String INIT_FRAME_TITLE = "Open Karaoke Toolkit";
    private static final FileNameExtensionFilter WAV_EXT_FILTER = new FileNameExtensionFilter("*.wav", "wav");
    private static final FileNameExtensionFilter SER_EXT_FILTER = new FileNameExtensionFilter("*.ser", "ser");


    private final SaveLoadManager saveLoadManager = new SaveLoadManager(this);
    private final Viewport viewport = new Viewport(saveLoadManager);

    private Timeline timeline;

    public Timeline getTimeline() {
        return timeline;
    }

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

        targetComponent.add(textArea);
    }

    private void addTimeline(JComponent targetComponent) {
        timeline = new Timeline(saveLoadManager, viewport);
        targetComponent.add(timeline);
    }

    private void addMenuBar() {
        // The menu bar
        JMenuBar menuBar = new JMenuBar();

        // -- File Menu --
        JMenu fileMenu = new JMenu("File");

        JFileChooser fileChooser = new JFileChooser("src/main/resources/audios"); // for temporary path

        // load project
        JMenuItem loadProject = getLoadProject(fileChooser);
        fileMenu.add(loadProject);

        // load audio
        JMenuItem loadAudio = getLoadAudio(fileChooser);
        saveLoadManager.setLoadedAudio(getClass().getResource("/audios/LiuLongKid.wav")); // for temporary test
        fileMenu.add(loadAudio);

        // save project
        JMenuItem saveProject = getSaveProject(fileChooser);
        fileMenu.add(saveProject);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private JMenuItem getLoadProject(JFileChooser fileChooser) {
        JMenuItem loadProject = new JMenuItem("Load Project");
        loadProject.addActionListener(e -> {
            fileChooser.setDialogTitle("Choose Project");
            fileChooser.setFileFilter(Main.SER_EXT_FILTER);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                saveLoadManager.load(selectedFile);
                System.out.println("Loaded project: " + selectedFile);
            }
        });
        return loadProject;
    }

    private JMenuItem getSaveProject(JFileChooser fileChooser) {
        JMenuItem saveProject = new JMenuItem("Save Project");
        saveProject.addActionListener(e -> {
            fileChooser.setDialogTitle("Save as");
            fileChooser.setFileFilter(Main.SER_EXT_FILTER);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.getName().endsWith(".ser")) {
                    selectedFile = new File(selectedFile + ".ser");
                }

                saveLoadManager.saveFileAs(selectedFile);
                System.out.println("Project saved to: " + selectedFile);
            }
        });
        return saveProject;
    }

    private JMenuItem getLoadAudio(JFileChooser fileChooser) {
        JMenuItem loadAudio = new JMenuItem("Load Audio");
        loadAudio.addActionListener(e -> {
            fileChooser.setDialogTitle("Choose Audio");
            fileChooser.setFileFilter(WAV_EXT_FILTER);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                saveLoadManager.setLoadedAudio(selectedFile);
                System.out.println("Loaded audio: " + selectedFile);
            }
        });
        return loadAudio;
    }

    public static void main(String[] args) {
        new Main(INIT_FRAME_TITLE);
    }
}