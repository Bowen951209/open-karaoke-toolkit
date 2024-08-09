package net.okt.gui;

import net.okt.system.ColorUtils;
import net.okt.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;

public class SideConfigPanel extends JScrollPane {
    private final SaveLoadManager saveLoadManager;
    private final Viewport viewport;
    private final Timeline timeline;
    private final JPanel panel;

    public SideConfigPanel(Dimension minSize, SaveLoadManager saveLoadManager, Viewport viewport, Timeline timeline) {
        this.saveLoadManager = saveLoadManager;
        this.viewport = viewport;
        this.timeline = timeline;
        this.panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        setViewportView(panel);

        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setMinimumSize(minSize);

        getVerticalScrollBar().setUnitIncrement(15);

        panel.add(getViewportConfigPanel());
        addSeparator();

        panel.add(getTextConfigPanel());
        addSeparator();

        panel.add(getReadyDotsConfigPanel());
    }

    private void addSeparator() {
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
    }

    private JPanel getViewportConfigPanel() {
        JPanel panel = new BoxLayoutYAxisPanel();

        var resolutionPanel = new DoubleTextBarPanel("Resolution", 4, "w:", "h:",
                "resolutionX", "resolutionY", saveLoadManager, viewport);
        resolutionPanel.addDocumentListener(viewport::resetBufferedImage);
        viewport.resetBufferedImage(); // init the buffered image, or else it will be null.

        Color backgroundColor = new Color(saveLoadManager.getPropInt("backgroundColor"), true);
        var backgroundColorChooserBtn = new ColorChooserButton(backgroundColor);
        backgroundColorChooserBtn.addColorChangedListener(newColor -> {
            saveLoadManager.setProp("backgroundColor", ColorUtils.rgbaToInt(newColor));
            viewport.repaint();
        });
        backgroundColorChooserBtn.callListeners();// update to the saveLoadManager for init.

        panel.add(new TitleLabel("Viewport Settings"));
        panel.add(resolutionPanel);
        panel.add(backgroundColorChooserBtn.getPanel("Background Color"));

        return panel;
    }

    private JPanel getTextConfigPanel() {
        JPanel panel = new BoxLayoutYAxisPanel();

        var textPosPanel = new DoubleTextBarPanel("Position", 3, "x:", "y:",
                "textPosX", "textPosY", saveLoadManager, viewport);

        var defaultFontSizeBar =
                new SlidableNumberBar("Default Font Size", 3, "defaultFontSize", saveLoadManager);
        defaultFontSizeBar.addDocumentListener(() -> {
            viewport.updateDisplayingAreas(true);
            viewport.repaint();
        });

        var linkedFontSizeBar =
                new SlidableNumberBar("Linked Font Size", 3, "linkedFontSize", saveLoadManager);
        linkedFontSizeBar.addDocumentListener(() -> {
            viewport.updateDisplayingAreas(true);
            viewport.repaint();
        });

        var lineIndentSizeBar =
                new SlidableNumberBar("2nd Line Indent", 3, "indentSize", saveLoadManager);
        lineIndentSizeBar.addDocumentListener(viewport::repaint);

        var lineSpaceSizeConfigBar =
                new SlidableNumberBar("Line Space", 3, "lineSpace", saveLoadManager);
        lineSpaceSizeConfigBar.addDocumentListener(viewport::repaint);

        var textDisappearTimeConfigBar =
                new SlidableNumberBar("Disappear Time (ms)", 4, "textDisappearTime", saveLoadManager);
        textDisappearTimeConfigBar.addDocumentListener(() -> {
            timeline.getCanvas().repaint();
            viewport.repaint();
        });

        var textBaseStrokeSizeConfigBar =
                new SlidableNumberBar("Base Stroke", 2, "textStroke", saveLoadManager);
        textBaseStrokeSizeConfigBar.addDocumentListener(viewport::repaint);

        var textIntersectStrokeSizeConfigBar =
                new SlidableNumberBar("Intersect Stroke", 2, "intersectStroke", saveLoadManager);
        textIntersectStrokeSizeConfigBar.addDocumentListener(viewport::repaint);

        var textColorChooserBtn = new ColorChooserButton(saveLoadManager.getPropInt("textColor"));
        textColorChooserBtn.addColorChangedListener(newColor -> {
            saveLoadManager.setProp("textColor", newColor.getRGB());
            viewport.repaint();
        });
        textColorChooserBtn.callListeners();// update to the saveLoadManager for init.

        var textIntersectStrokeColorChooserBtn =
                new ColorChooserButton(saveLoadManager.getPropInt("intersectStrokeColor"));
        textIntersectStrokeColorChooserBtn.addColorChangedListener(newColor -> {
            saveLoadManager.setProp("intersectStrokeColor", newColor.getRGB());
            viewport.repaint();
        });
        textIntersectStrokeColorChooserBtn.callListeners();// update to the saveLoadManager for init.

        panel.add(new TitleLabel("Text Settings"));
        panel.add(getFontComboBox());
        panel.add(textPosPanel);
        panel.add(defaultFontSizeBar);
        panel.add(linkedFontSizeBar);
        panel.add(lineIndentSizeBar);
        panel.add(lineSpaceSizeConfigBar);
        panel.add(textDisappearTimeConfigBar);
        panel.add(textBaseStrokeSizeConfigBar);
        panel.add(textIntersectStrokeSizeConfigBar);
        panel.add(textColorChooserBtn.getPanel("Text Color"));
        panel.add(textIntersectStrokeColorChooserBtn.getPanel("Intersect Stroke Color"));

        return panel;
    }

    private JPanel getReadyDotsConfigPanel() {
        JPanel panel = new BoxLayoutYAxisPanel();

        var dotsPosPanel = new DoubleTextBarPanel("Position", 3, "x:", "y:",
                "dotsPosX", "dotsPosY", saveLoadManager, viewport);

        var readyDotsSizeConfigBar = new SlidableNumberBar("Size", 3, "dotsSize", saveLoadManager);
        readyDotsSizeConfigBar.addDocumentListener(viewport::repaint);

        var readyDotsNumComboBox = new TitledComboBox<>("Number of Dots", new Integer[]{3, 4, 5});
        readyDotsNumComboBox.setSelectedItem(saveLoadManager.getPropInt("dotsNum"));
        readyDotsNumComboBox.addActionListener(() -> {
            saveLoadManager.setProp("dotsNum", (int) readyDotsNumComboBox.getSelectedElement());
            timeline.getCanvas().repaint();
            viewport.repaint();
        });

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

        panel.add(new TitleLabel("Ready Dots Settings"));
        panel.add(dotsPosPanel);
        panel.add(readyDotsSizeConfigBar);
        panel.add(readyDotsNumComboBox);
        panel.add(readyDotsTimeConfigBar);
        panel.add(readyDotsStrokeSizeConfigBar);
        panel.add(readyDotsColorChooserBtn.getPanel("Dots Color"));

        return panel;
    }

    private JComboBox<String> getFontComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();

        // Get system fonts.
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String fontName : graphicsEnvironment.getAvailableFontFamilyNames())
            comboBox.addItem(fontName);

        // Select the saveLoadManager specified font.
        comboBox.setSelectedItem(saveLoadManager.getProp("font"));

        comboBox.addActionListener(e -> {
            String selectedString = (String) comboBox.getSelectedItem();

            saveLoadManager.setProp("font", selectedString);
            viewport.setFont(new Font(selectedString, Font.BOLD, 1));
            viewport.updateDisplayingAreas(true);
        });

        return comboBox;
    }
}
