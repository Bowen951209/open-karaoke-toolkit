package net.okt.gui;

import net.okt.system.SaveLoadManager;
import net.okt.system.VideoMaker;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VideoExportDialog {
    private final SaveLoadManager saveLoadManager;
    private final Viewport viewport;
    private final JComboBox<String> formatComboBox;
    private final JComboBox<String> codecComboBox;
    private final JComboBox<Integer> fpsComboBox;
    private final JComboBox<Integer> bitrateComboBox;
    private final SlidableNumberBar timeBar;
    private final TextFieldFileChooser textFieldFileChooser;
    private final JComponent[] inputs;

    public VideoExportDialog(SaveLoadManager saveLoadManager, Viewport viewport, JFileChooser fileChooser) {
        this.saveLoadManager = saveLoadManager;
        this.viewport = viewport;

        String[] formatOptions = {"mp4", "mov", "avi"};
        String[] codecOptions = VideoMaker.CODEC_MAP.keySet().toArray(new String[0]);
        Integer[] fpsOptions = {24, 25, 30, 50, 60, 120};
        Integer[] bitrateOptions = {500, 1000, 2500, 5000, 7500, 10000, 15000, 20000};

        this.formatComboBox = new JComboBox<>(formatOptions);
        this.codecComboBox = new JComboBox<>(codecOptions);
        this.fpsComboBox = new JComboBox<>(fpsOptions);
        this.bitrateComboBox = new JComboBox<>(bitrateOptions);
        this.timeBar = new SlidableNumberBar(null, 7, 45000);
        String file = fileChooser.getCurrentDirectory() + File.separator + "output";
        this.textFieldFileChooser = new TextFieldFileChooser(file);

        this.inputs = new JComponent[]{
                new JLabel("Format:"),
                formatComboBox,
                new JLabel("Codec:"),
                codecComboBox,
                new JLabel("Fps:"),
                fpsComboBox,
                new JLabel("Bitrate(kbps):"),
                bitrateComboBox,
                new JLabel("Time Length(ms):"),
                getVidLengthPanel(),
                new JLabel("Save Location:"),
                textFieldFileChooser
        };

        // Settings.
        // If the selected format is changed, also change to the file chooser.
        this.formatComboBox.addActionListener(e -> {
            String selectedFormat = (String) formatComboBox.getSelectedItem();
            textFieldFileChooser.setExtensionFilter("." + selectedFormat, selectedFormat);
        });
        this.formatComboBox.setSelectedIndex(0); // activate the action listener to init the extension filter.
    }

    public void show() {
        int option = JOptionPane.showConfirmDialog(null, inputs, "Export To Video",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            int time = timeBar.getVal();
            Integer fps = (Integer) fpsComboBox.getSelectedItem();
            Integer bitrate = (Integer) (bitrateComboBox.getSelectedItem());

            String selectedFormat = (String) formatComboBox.getSelectedItem();
            String selectedCodec = (String) codecComboBox.getSelectedItem();
            if (fps == null || bitrate == null || selectedCodec == null)
                throw new NullPointerException("One of the combo boxes selected value is null");
            bitrate *= 1000;

            // If the extension doesn't match the selected format, go to the start again.
            boolean isCorrectExtension = textFieldFileChooser.getSelectedFile().getName().endsWith("." + selectedFormat);
            if (!isCorrectExtension) {
                JOptionPane.showMessageDialog(
                        null, "Extension doesn't match the selected format!",
                        "WARNING", JOptionPane.WARNING_MESSAGE
                );
                show();
                return; // return to quit this call of the method.
            }

            String filePath = textFieldFileChooser.getSelectedFile().getAbsolutePath();

            int videoWidth = viewport.getBufferedImage().getWidth();
            int videoHeight = viewport.getBufferedImage().getHeight();

            ProgressBarDialog progressBarDialog =
                    new ProgressBarDialog("Progress", "Are you sure you want to cancel the output?");

            VideoMaker videoMaker = new VideoMaker(filePath, selectedFormat, selectedCodec, fps, bitrate, time,
                    videoWidth, videoHeight, viewport, saveLoadManager, progressBarDialog);
            videoMaker.start();

            progressBarDialog.setManualCloseOperation(videoMaker::stopProcessing);
            progressBarDialog.setVisible(true);
        }
    }

    private JPanel getVidLengthPanel() {
        JCheckBox fullVidCheckBox = new JCheckBox("Full Length");
        fullVidCheckBox.addItemListener(e -> {
            timeBar.setEnabled(!fullVidCheckBox.isSelected());

            var loadedAudio = saveLoadManager.getLoadedAudio();
            if (loadedAudio == null) return;
            timeBar.setVal(loadedAudio.getTotalTime());
        });

        JPanel timeLengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeLengthPanel.add(fullVidCheckBox);
        timeLengthPanel.add(timeBar);

        fullVidCheckBox.setSelected(true);

        return timeLengthPanel;
    }
}
