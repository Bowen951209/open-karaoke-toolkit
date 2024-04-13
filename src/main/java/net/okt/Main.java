package net.okt;

import net.okt.gui.*;
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
    private final Viewport viewport = new Viewport(saveLoadManager);
    private final Timeline timeline = new Timeline(saveLoadManager, viewport);
    private final LineNumberedScrollableTextArea textArea = getTextArea();

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

        // Load the sample save file.
        saveLoadManager.load(Objects.requireNonNull(Main.class.getResource("/saves/sample.properties")));
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

        setVisible(true);
    }

    /**
     * Get the configure panel.
     */
    private JScrollPane getConfPanel() {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);


        var defaultFontSizeBar =
                new SlidableNumberBar("Default Font Size", 3, "defaultFontSize", saveLoadManager);
        defaultFontSizeBar.addDocumentListener(() -> {
            viewport.setDefaultFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getPropInt("defaultFontSize")));
            viewport.repaint();
        });
        viewport.setDefaultFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getPropInt("defaultFontSize")));

        var linkedFontSizeBar =
                new SlidableNumberBar("Linked Font Size", 3, "linkedFontSize", saveLoadManager);
        linkedFontSizeBar.addDocumentListener(() -> {
            viewport.setLinkedFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getPropInt("linkedFontSize")));
            viewport.repaint();
        });
        viewport.setLinkedFont(new Font(Font.SANS_SERIF, Font.BOLD, saveLoadManager.getPropInt("linkedFontSize")));

        var lineIndentSizeBar =
                new SlidableNumberBar("2nd Line Indent", 3, "indentSize", saveLoadManager);
        lineIndentSizeBar.addDocumentListener(viewport::repaint);

        var lineSpaceSizeConfigBar =
                new SlidableNumberBar("Line Space", 3, "lineSpace", saveLoadManager);
        lineSpaceSizeConfigBar.addDocumentListener(viewport::repaint);

        var textDisappearTimeConfigBar =
                new SlidableNumberBar("Disappear Time (ms)", 4, "textDisappearTime", saveLoadManager);
        textDisappearTimeConfigBar.addDocumentListener(() -> {
            getTimeline().getCanvas().repaint();
            viewport.repaint();
        });

        var textStrokeSizeConfigBar =
                new SlidableNumberBar("Stroke", 2, "textStroke", saveLoadManager);
        textStrokeSizeConfigBar.addDocumentListener(viewport::repaint);

        var textColorChooserBtn = new ColorChooserButton(saveLoadManager.getPropInt("textColor"));
        textColorChooserBtn.addColorChangedListener(newColor -> {
            saveLoadManager.setProp("textColor", newColor.getRGB());
            viewport.repaint();
        });
        textColorChooserBtn.callListeners();// update to the saveLoadManager for init.

        var readyDotsTimeConfigBar = new SlidableNumberBar("Time (ms)", 4, "dotsPeriod", saveLoadManager);
        readyDotsTimeConfigBar.addDocumentListener(() -> {
            timeline.getCanvas().repaint();
            viewport.repaint();
        });
        readyDotsTimeConfigBar.setDragStep(5);

        var readyDotsStrokeSizeConfigBar =
                new SlidableNumberBar("Stroke", 2, "dotsStroke", saveLoadManager);
        readyDotsStrokeSizeConfigBar.addDocumentListener(viewport::repaint);

        var readyDotsColorChooserBtn = new ColorChooserButton(saveLoadManager.getPropInt("dotsColor"));
        readyDotsColorChooserBtn.addColorChangedListener(newColor -> {
            saveLoadManager.setProp("dotsColor", newColor.getRGB());
            viewport.repaint();
        });
        readyDotsColorChooserBtn.callListeners();// update to the saveLoadManager for init.

        var resolutionPanel = new DoubleTextBarPanel("Resolution", 4, "w:", "h:",
                "resolutionX", "resolutionY", saveLoadManager, viewport);
        resolutionPanel.addDocumentListenr(viewport::resetBufferedImage);
        viewport.resetBufferedImage(); // init the buffered image, or else it will be null.

        var textPosPanel = new DoubleTextBarPanel("Position", 3, "x:", "y:",
                "textPosX", "textPosY", saveLoadManager, viewport);

        var dotsPosPanel = new DoubleTextBarPanel("Position", 3, "x:", "y:",
                "dotsPosX", "dotsPosY", saveLoadManager, viewport);

        var readyDotsNumComboBox = new TitledComboBox<>("Number of Dots", new Integer[]{3, 4, 5});
        readyDotsNumComboBox.setSelectedItem(saveLoadManager.getPropInt("dotsNum"));
        readyDotsNumComboBox.addActionListener(() -> {
            saveLoadManager.setProp("dotsNum", (int) readyDotsNumComboBox.getSelectedElement());
            timeline.getCanvas().repaint();
            viewport.repaint();
        });

        // Viewport settings.
        panel.add(new TitleLabel("Viewport Settings"));
        panel.add(resolutionPanel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));

        // Text settings.
        panel.add(new TitleLabel("Text Settings"));
        panel.add(textPosPanel);
        panel.add(defaultFontSizeBar);
        panel.add(linkedFontSizeBar);
        panel.add(lineIndentSizeBar);
        panel.add(lineSpaceSizeConfigBar);
        panel.add(textDisappearTimeConfigBar);
        panel.add(textStrokeSizeConfigBar);
        panel.add(textColorChooserBtn);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));

        // Ready dots settings.
        panel.add(new TitleLabel("Ready Dots Settings"));
        panel.add(dotsPosPanel);
        panel.add(readyDotsNumComboBox);
        panel.add(readyDotsTimeConfigBar);
        panel.add(readyDotsStrokeSizeConfigBar);
        panel.add(readyDotsColorChooserBtn);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new Dimension(240, 150));
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

    public static void main(String[] args) {
        mainFrame = new Main(INIT_FRAME_TITLE);
        mainFrame.addComponents();
    }
}