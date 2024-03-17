package net.okt.gui;

import net.okt.system.SaveLoadManager;
import net.okt.system.VideoMaker;

import javax.swing.*;

public class VideoExportDialog {
    private final SaveLoadManager saveLoadManager;
    private final Viewport viewport;
    private final JComboBox<String> codecComboBox;
    private final JComboBox<Integer> fpsComboBox;
    private final JComboBox<Integer> bitrateComboBox;
    private final SlidableNumberBar timeBar;
    private final TextFieldFileChooser textFieldFileChooser;
    private final JComponent[] inputs;

    public VideoExportDialog(SaveLoadManager saveLoadManager, Viewport viewport, JFileChooser fileChooser) {
        this.saveLoadManager = saveLoadManager;
        this.viewport = viewport;

        String[] codecOptions = VideoMaker.CODEC_MAP.keySet().toArray(new String[0]);
        Integer[] fpsOptions = {24, 25, 30, 50, 60, 120};
        Integer[] bitrateOptions = {500, 1000, 2500, 5000, 7500, 10000, 15000, 20000};

        this.codecComboBox = new JComboBox<>(codecOptions);
        this.fpsComboBox = new JComboBox<>(fpsOptions);
        this.bitrateComboBox = new JComboBox<>(bitrateOptions);
        this.timeBar = new SlidableNumberBar(null, 7, 45000);
        this.textFieldFileChooser = new TextFieldFileChooser(fileChooser.getCurrentDirectory() + "\\output.mp4");

        this.inputs = new JComponent[]{
                new JLabel("Codec:"),
                codecComboBox,
                new JLabel("Fps:"),
                fpsComboBox,
                new JLabel("Bitrate(kbps):"),
                bitrateComboBox,
                new JLabel("Time Length(ms):"),
                timeBar,
                new JLabel("Save Location:"),
                textFieldFileChooser
        };
    }

    public void show() {
        int option = JOptionPane.showConfirmDialog(null, inputs, "Export To Video",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            int time = timeBar.getVal();
            Integer fps = (Integer) fpsComboBox.getSelectedItem();
            Integer bitrate = (Integer) (bitrateComboBox.getSelectedItem());
            String codecSelection = (String) codecComboBox.getSelectedItem();
            if (fps == null || bitrate == null || codecSelection == null)
                throw new NullPointerException("One of the combo boxes selected value is null");
            bitrate *= 1000;

            boolean isMP4Extension = textFieldFileChooser.getSelectedFile().getName().endsWith(".mp4");
            if (!isMP4Extension) throw new IllegalArgumentException("Format is not mp4.");

            String filePath = textFieldFileChooser.getSelectedFile().getAbsolutePath();

            int videoWidth = viewport.getBufferedImage().getWidth();
            int videoHeight = viewport.getBufferedImage().getHeight();
            VideoMaker.genVideo(filePath, codecSelection, fps, bitrate, time,
                    videoWidth, videoHeight, viewport, saveLoadManager);
        }
    }
}
