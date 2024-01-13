package net.bowen;

import net.bowen.gui.SizeConfigBar;
import net.bowen.gui.LineNumberedScrollableTextArea;
import net.bowen.gui.Timeline;
import net.bowen.gui.Viewport;
import net.bowen.system.SaveLoadManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class Main extends JFrame {
    public static final String INIT_FRAME_TITLE = "Open Karaoke Toolkit";
    private static final FileNameExtensionFilter WAV_EXT_FILTER = new FileNameExtensionFilter("*.wav", "wav");
    private static final FileNameExtensionFilter PROPS_EXT_FILTER = new FileNameExtensionFilter("*.properties", "properties");


    private final SaveLoadManager saveLoadManager = new SaveLoadManager(this);
    private final Viewport viewport = new Viewport(saveLoadManager);
    private final LineNumberedScrollableTextArea textArea;
    private final Timeline timeline = new Timeline(saveLoadManager, viewport);

    public final SizeConfigBar defaultFontSizeBar = new SizeConfigBar(3, "Default Font Size") {
        @Override
        public void documentCallback() {
            saveLoadManager.setDefaultFontSize(super.size);
            viewport.setDefaultFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getDefaultFontSize()));
            viewport.repaint();
        }
    };
    public final SizeConfigBar linkedFontSizeBar = new SizeConfigBar(3, "Linked Font Size") {
        @Override
        public void documentCallback() {
            saveLoadManager.setLinkedFontSize(super.size);
            viewport.setLinkedFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getLinkedFontSize()));
            viewport.repaint();
        }
    };
    public final SizeConfigBar lineIndentSizeBar = new SizeConfigBar(3, "2nd Line Indent") {
        @Override
        public void documentCallback() {
            saveLoadManager.setIndentSize(super.size);
            viewport.repaint();
        }
    };
    public final SizeConfigBar lineSpaceSizeConfigBar = new SizeConfigBar(3, "Line Space") {
        @Override
        public void documentCallback() {
            saveLoadManager.setLineSpace(super.size);
            viewport.repaint();
        }
    };

    public Timeline getTimeline() {
        return timeline;
    }

    public LineNumberedScrollableTextArea getTextArea() {
        if (textArea != null) return textArea;

        LineNumberedScrollableTextArea textArea = new LineNumberedScrollableTextArea();

        textArea.addUndoListener(timeline::markUndo);
        textArea.addRedoListener(timeline::markRedo);

        textArea.addDocumentUpdateCallback(() -> {
            saveLoadManager.setText(textArea.getText());
            viewport.repaint();
            timeline.getCanvas().repaint();
        });
        textArea.setText(saveLoadManager.getText()); // I use Chinese for just now, sorry to English speaker :)

        return textArea;
    }

    private Main(String title) {
        // Init settings.
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));
        setLayout(new BorderLayout());

        // Menu bar on the top.
        addMenuBar();

        // Top split pane(text area, viewport, config menu).
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.setPreferredSize(new Dimension(getWidth(), (int) (getHeight() * .7f)));
        // sp1 hold: textArea & viewport. We have to separate like this because JSplitPane only support 2 splits.
        JSplitPane sp1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        viewport.setPreferredSize(new Dimension((int) (getWidth() * .6), 0));
        topSplitPane.add(sp1);
        this.textArea = getTextArea();
        sp1.add(textArea);
        sp1.add(viewport);
        topSplitPane.add(getConfPanel());

        // Main split pane (sep top pane and bottom timeline).
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        timeline.setPreferredSize(getMinimumSize());
        mainSplitPane.add(topSplitPane);
        mainSplitPane.add(timeline);

        // Add main split pane to frame.
        add(mainSplitPane);

        // Load the sample save file.
        saveLoadManager.load(Objects.requireNonNull(Main.class.getResource("/saves/sample.properties")));

        setVisible(true);
    }

    /**
     * Get the configure panel.
     */
    private JPanel getConfPanel() {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        panel.add(defaultFontSizeBar);
        panel.add(linkedFontSizeBar);
        panel.add(lineIndentSizeBar);
        panel.add(lineSpaceSizeConfigBar);

        Dimension size = new Dimension(150, 0);
        panel.setMinimumSize(size);

        return panel;
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

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
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

    public static void main(String[] args) {
        new Main(INIT_FRAME_TITLE);
    }
}