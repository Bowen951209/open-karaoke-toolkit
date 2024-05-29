package net.okt;

import net.okt.gui.*;
import net.okt.system.LyricsProcessor;
import net.okt.system.SaveLoadManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class Main extends JFrame {
    public static final String INIT_FRAME_TITLE = "Open Karaoke Toolkit";
    public static final FileNameExtensionFilter WAV_EXT_FILTER = new FileNameExtensionFilter("*.wav", "wav");
    public static final FileNameExtensionFilter PROPS_EXT_FILTER = new FileNameExtensionFilter("*.properties", "properties");

    public static Main mainFrame;

    private final SaveLoadManager saveLoadManager = new SaveLoadManager(this);
    private final LyricsProcessor lyricsProcessor = new LyricsProcessor(saveLoadManager);
    private final Viewport viewport = new Viewport(saveLoadManager, lyricsProcessor);
    private final Timeline timeline = new Timeline(saveLoadManager, lyricsProcessor, viewport);
    private final LineNumberedScrollableTextArea textArea = getTextArea();

    public Main(String title) {
        // Init settings.
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));

        // Load the sample save file.
        saveLoadManager.load(Objects.requireNonNull(Main.class.getResource("/saves/sample.properties")));
    }

    public static void main(String[] args) {
        mainFrame = new Main(INIT_FRAME_TITLE);
        mainFrame.addComponents();
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public LineNumberedScrollableTextArea getTextArea() {
        if (textArea != null) return textArea;

        LineNumberedScrollableTextArea textArea = new LineNumberedScrollableTextArea();

        textArea.addUndoListener(timeline::markUndo);
        textArea.addRedoListener(timeline::markRedo);

        textArea.addDocumentUpdateCallback(() -> {
            saveLoadManager.setProp("text", textArea.getText());
            lyricsProcessor.setLyrics(saveLoadManager.getProp("text"));
            timeline.resetMarksNum();
            viewport.updateDisplayingAreas(true);
            viewport.repaint();
            timeline.getCanvas().repaint();
        });
        textArea.setText(saveLoadManager.getProp("text")); // I use Chinese for just now, sorry to English speaker :)

        return textArea;
    }

    private void addComponents() {
        setLayout(new BorderLayout());

        // Menu bar on the top.
        addMenuBar();

        // Top split pane(text area, viewport, config menu).
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.setResizeWeight(1); // make right part fixed when resizing.
        topSplitPane.setPreferredSize(new Dimension(getWidth(), (int) (getHeight() * .7f)));
        // sp1 hold: textArea & viewport. We have to separate like this because JSplitPane only support 2 splits.
        JSplitPane sp1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        viewport.setPreferredSize(new Dimension((int) (getWidth() * .6), 0));
        topSplitPane.add(sp1);
        sp1.add(textArea);
        sp1.add(viewport);
        topSplitPane.add(new SideConfigPanel(new Dimension(240, 150), saveLoadManager, viewport, timeline));

        // Main split pane (sep top pane and bottom timeline).
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(1); // make right part fixed when resizing.
        timeline.setPreferredSize(getMinimumSize());
        mainSplitPane.add(topSplitPane);
        mainSplitPane.add(timeline);

        // Add main split pane to frame.
        add(mainSplitPane);

        setVisible(true);
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
        fileMenu.add(loadAudio);

        // save project
        JMenuItem saveProject = getSaveProject(fileChooser);
        fileMenu.add(saveProject);

        // export
        JMenuItem export = getExportMenuItem(fileChooser);
        fileMenu.add(export);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private JMenuItem getExportMenuItem(JFileChooser fileChooser) {
        VideoExportDialog videoExportDialog = new VideoExportDialog(saveLoadManager, viewport, fileChooser);
        JMenuItem item = new JMenuItem("Export");
        item.addActionListener(e -> videoExportDialog.show());

        return item;
    }

    private JMenuItem getLoadProject(JFileChooser fileChooser) {
        JMenuItem loadProject = new JMenuItem("Load Project");
        loadProject.addActionListener(e -> {
            fileChooser.setDialogTitle("Choose Project");
            fileChooser.setFileFilter(Main.PROPS_EXT_FILTER);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                saveLoadManager.load(selectedFile);
            }
        });
        return loadProject;
    }

    private JMenuItem getSaveProject(JFileChooser fileChooser) {
        JMenuItem saveProject = new JMenuItem("Save Project");
        saveProject.addActionListener(e -> {
            fileChooser.setDialogTitle("Save as");
            fileChooser.setFileFilter(Main.PROPS_EXT_FILTER);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String extension = "." + PROPS_EXT_FILTER.getExtensions()[0];
                if (!selectedFile.getName().endsWith(extension)) {
                    selectedFile = new File(selectedFile + extension);
                }

                saveLoadManager.saveFileAs(selectedFile);
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
            }
        });
        return loadAudio;
    }


}