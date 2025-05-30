package net.okt.system;

import net.okt.Main;
import net.okt.audioUtils.Audio;
import net.okt.audioUtils.BoxWaveform;
import net.okt.gui.LineNumberedScrollableTextArea;
import net.okt.gui.Timeline;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

import static net.okt.gui.Timeline.PIXEL_TIME_RATIO;

/**
 * This class store the state the user is at. For example, loaded audio, text, etc.
 */
public class SaveLoadManager {
    private final Main mainFrame;
    private final ArrayList<Integer> marks = new ArrayList<>();
    private final Properties props = new Properties();
    private Audio loadedAudio;

    public SaveLoadManager(Main mainFrame) {
        this.mainFrame = mainFrame;
        initProps();
    }

    public void initProps() {
        setProp("backgroundColor", Color.WHITE.getRGB());
        setProp("defaultFontSize", 10);
        setProp("dotsColor", Color.BLUE.getRGB());
        setProp("dotsNum", 4);
        setProp("dotsPeriod", 2500);
        setProp("dotsPosX", 0);
        setProp("dotsPosY", 0);
        setProp("dotsSize", 10);
        setProp("dotsStroke", 45);
        setProp("font", "SansSerif");
        setProp("indentSize", 15);
        setProp("lineSpace", 15);
        setProp("linkedFontSize", 7);
        setProp("resolutionX", 1920);
        setProp("resolutionY", 1080);
        setProp("textColor", Color.BLUE.getRGB());
        setProp("intersectStrokeColor", Color.WHITE.getRGB());
        setProp("textDisappearTime", 1500);
        setProp("textPosX", 0);
        setProp("textPosY", 20);
        setProp("textStroke", 45);
        setProp("intersectStroke", 48);
    }

    public Audio getLoadedAudio() {
        return loadedAudio;
    }

    public void setLoadedAudio(File audio) {
        Timeline timeline = mainFrame.getTimeline();
        if (timeline == null) return;
        Timeline.Canvas canvas = timeline.getCanvas();

        // If there is a currently loaded audio, close it first.
        if (loadedAudio != null)
            loadedAudio.close();

        try {
            loadedAudio = new Audio(audio);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setProp("audio", audio.getPath());

        // Display the file name on the timeline.
        timeline.setDisplayFileName(audio.getName());

        // Ready the wave img.
        int imgWidth = (int) (getLoadedAudio().getTotalTime() * PIXEL_TIME_RATIO * Timeline.SLIDER_MAX_VAL * 0.01f);

        timeline.setWaveImg(BoxWaveform.loadImage(
                audio,
                new Dimension(imgWidth, 50),
                new Color(5, 80, 20)));

        // Stop the timer and revalidate the canvas.
        timeline.timeStop();
        canvas.setSize();
        canvas.revalidate();

        System.out.println("Loaded audio: " + audio);
    }

    public ArrayList<Integer> getMarks() {
        return marks;
    }

    public String getProp(String key) {
        return props.getProperty(key);
    }

    public int getPropInt(String key) {
        String prop = getProp(key);
        if (prop == null) return -1;
        return Integer.parseInt(prop);
    }

    public void setProp(String key, String val) {
        props.setProperty(key, val);
    }

    public void setProp(String key, int val) {
        props.setProperty(key, String.valueOf(val));
    }

    public void saveFileAs(File file) {
        // Marks.
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer t : marks)
            stringBuilder.append(t).append(",");
        setProp("marks", stringBuilder.toString());

        // Make the audio path relative to the properties file path.
        Path audioPath = Path.of(getProp("audio"));
        Path base = file.toPath().getParent();
        String audioRelativePath = base.relativize(audioPath).toString().replace("\\", "/");
        setProp("audio", audioRelativePath);

        // Save to file.
        try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            props.store(fileWriter, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set the audio path property back to full path.
        setProp("audio", audioPath.toString());

        System.out.println("Project saved to: " + file);
    }

    public void load(File file, LineNumberedScrollableTextArea textArea) {
        mainFrame.setTitle(Main.INIT_FRAME_TITLE + " - " + file.getName());

        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            // Load the properties file.
            props.load(inputStreamReader);

            textArea.setText(getProp("text")); // Update to text area.

            // Marks.
            marks.clear();
            String marksProp = getProp("marks");
            if (!marksProp.isEmpty()) {
                String[] marksStrings = getProp("marks").split(",");
                for (String string : marksStrings)
                    marks.add(Integer.valueOf(string));
            }

            // Audio.
            File audioFile = new File(file.getParent(), getProp("audio"));
            if (!audioFile.exists()) {
                // Pop up a message.
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Audio file missing(probably moved or deleted), please redirect it.",
                        "Report",
                        JOptionPane.WARNING_MESSAGE
                );

                JFileChooser fcr = new JFileChooser(".");
                fcr.setDialogTitle("Please redirect the file: ");
                fcr.setFileFilter(FileExtensionUtils.AUDIO_EXT_FILTER);
                if (fcr.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    audioFile = fcr.getSelectedFile();
                } else {
                    System.out.println("File chooser not approved. Closing the program now.");
                    System.exit(1);
                }
            }
            setLoadedAudio(audioFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Loaded project: " + file);
    }
}
