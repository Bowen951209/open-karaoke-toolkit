package net.bowen;

import net.bowen.gui.*;
import net.bowen.system.SaveLoadManager;

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
    private final Viewport viewport = new Viewport(saveLoadManager);
    private final Timeline timeline = new Timeline(saveLoadManager, viewport);
    private final LineNumberedScrollableTextArea textArea = getTextArea();

    public final SlidableNumberBar textPosXConfigBar;
    public final SlidableNumberBar textPosYConfigBar;
    public final SlidableNumberBar dotsPosXConfigBar;
    public final SlidableNumberBar dotsPosYConfigBar;
    public final SlidableNumberBar defaultFontSizeBar;
    public final SlidableNumberBar linkedFontSizeBar;
    public final SlidableNumberBar lineIndentSizeBar;
    public final SlidableNumberBar lineSpaceSizeConfigBar;
    public final SlidableNumberBar readyDotsTimeConfigBar = new SlidableNumberBar(4, "Time (ms)");
    public final TitledComboBox<Integer> readyDotsNumComboBox = new TitledComboBox<>("Number of Dots", new Integer[]{3, 4, 5});

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
            timeline.resetMarksNum();
            viewport.repaint();
            timeline.getCanvas().repaint();
        });
        textArea.setText(saveLoadManager.getProp("text")); // I use Chinese for just now, sorry to English speaker :)

        return textArea;
    }

    private Main(String title) {
        // Init settings.
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenSize.width / 1.7f), (int) (screenSize.height / 1.7f));

        // Init the components.
        this.textPosXConfigBar = new SlidableNumberBar(3, "x:");
        textPosXConfigBar.fixSize(60);
        this.textPosXConfigBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("textPosX", b.getVal());
            viewport.repaint();
        });

        this.textPosYConfigBar = new SlidableNumberBar(3, "y:");
        textPosYConfigBar.fixSize(60);
        this.textPosYConfigBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("textPosY", b.getVal());
            viewport.repaint();
        });

        this.dotsPosXConfigBar = new SlidableNumberBar(3, "x:");
        dotsPosXConfigBar.fixSize(60);
        this.dotsPosXConfigBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("dotsPosX", b.getVal());
            viewport.repaint();
        });

        this.dotsPosYConfigBar = new SlidableNumberBar(3, "y:");
        dotsPosYConfigBar.fixSize(60);
        this.dotsPosYConfigBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("dotsPosY", b.getVal());
            viewport.repaint();
        });

        this.defaultFontSizeBar = new SlidableNumberBar(3, "Default Font Size");
        this.defaultFontSizeBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("defaultFontSize", b.getVal());
            viewport.setDefaultFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getPropInt("defaultFontSize")));
            viewport.repaint();
        });

        this.linkedFontSizeBar = new SlidableNumberBar(3, "Linked Font Size");
        this.linkedFontSizeBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("linkedFontSize", b.getVal());
            viewport.setLinkedFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getPropInt("linkedFontSize")));
            viewport.repaint();
        });

        this.lineIndentSizeBar = new SlidableNumberBar(3, "2nd Line Indent");
        this.lineIndentSizeBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("indentSize", b.getVal());
            viewport.repaint();
        });

        this.lineSpaceSizeConfigBar = new SlidableNumberBar(3, "Line Space");
        this.lineSpaceSizeConfigBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("lineSpace", b.getVal());
            viewport.repaint();
        });
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
        topSplitPane.add(getConfPanel());

        // Main split pane (sep top pane and bottom timeline).
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(1); // make right part fixed when resizing.
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
    private JScrollPane getConfPanel() {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        // The title font.
        Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 13);

        // The components.
        JLabel textSettingsLabel = new JLabel("Text Settings");
        textSettingsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textSettingsLabel.setFont(titleFont);
        JPanel textPosPanel = new JPanel();
        textPosPanel.setMaximumSize(new Dimension(500, 10));
        textPosPanel.add(new JLabel("Position"));
        textPosPanel.add(textPosXConfigBar);
        textPosPanel.add(textPosYConfigBar);
        JPanel dotsPosPanel = new JPanel();
        dotsPosPanel.setMaximumSize(new Dimension(500, 10));
        dotsPosPanel.add(new JLabel("Position"));
        dotsPosPanel.add(dotsPosXConfigBar);
        dotsPosPanel.add(dotsPosYConfigBar);
        JLabel readyDotsSettingsLabel = new JLabel("Ready Dots Settings");
        readyDotsSettingsLabel.setBackground(Color.BLACK);
        readyDotsSettingsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyDotsSettingsLabel.setFont(titleFont);
        readyDotsNumComboBox.addActionListener(() -> {
            saveLoadManager.setProp("dotsNum", (int) readyDotsNumComboBox.getSelectedElement());
            timeline.getCanvas().repaint();
            viewport.repaint();
        });
        readyDotsTimeConfigBar.addDocumentListener((b) -> {
            saveLoadManager.setProp("dotsPeriod", b.getVal());
            timeline.getCanvas().repaint();
            viewport.repaint();
        });
        readyDotsTimeConfigBar.setDragStep(5);

        // Text settings.
        panel.add(textSettingsLabel);
        panel.add(textPosPanel);
        panel.add(defaultFontSizeBar);
        panel.add(linkedFontSizeBar);
        panel.add(lineIndentSizeBar);
        panel.add(lineSpaceSizeConfigBar);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));

        // Ready dots settings.
        panel.add(readyDotsSettingsLabel);
        panel.add(dotsPosPanel);
        panel.add(readyDotsNumComboBox);
        panel.add(readyDotsTimeConfigBar);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(200, 150));
        return scrollPane;
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
        mainFrame = new Main(INIT_FRAME_TITLE);
        mainFrame.addComponents();
    }
}