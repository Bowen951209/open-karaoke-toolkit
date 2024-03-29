package net.okt.system;

import net.okt.Main;
import net.okt.audioUtils.Audio;
import net.okt.audioUtils.BoxWaveform;
import net.okt.gui.Timeline;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static net.okt.gui.Timeline.PIXEL_TIME_RATIO;

/**
 * This class store the state the user is at. For example, loaded audio, text, etc.
 */
public class SaveLoadManager {
    private final Main mainFrame;
    /**
     * This list stores the text(lyrics) string as an array, but with some modifications that the linked words would
     * stay in the same elements. For example, the string "ab'c\ndef" would store as the list {a, bc, \n, d, e, f}.
     */
    private final List<String> textList = new ArrayList<>();
    private final ArrayList<Long> marks = new ArrayList<>();
    private final Properties props = new Properties();

    private Audio loadedAudio;

    public SaveLoadManager(Main mainFrame) {
        this.mainFrame = mainFrame;
    }

    public Audio getLoadedAudio() {
        return loadedAudio;
    }

    public ArrayList<Long> getMarks() {
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

    public void setText(String text) {
        setProp("text", text);
        textList.clear();

        // Set the textList.
        for (int i = 0; i < text.length(); i++) {
            String thisWord = String.valueOf(text.charAt(i));

            if (i + 1 < text.length()) {// If not last word
                char nextChar = text.charAt(i + 1);

                if (nextChar == '\'') {// linked word case
                    i += 2;
                    String linkedWord = thisWord + text.charAt(i);
                    textList.add(linkedWord);
                } else { // single word case
                    textList.add(thisWord);
                }
            } else {
                textList.add(thisWord);
            }
        }
    }

    public List<String> getTextList() {
        return textList;
    }

    public void setLoadedAudio(File audio) {
        Timeline timeline = mainFrame.getTimeline();
        if (timeline == null) return;
        Timeline.Canvas canvas = timeline.getCanvas();

        // If there is a currently loaded audio, close it first.
        if (loadedAudio != null)
            loadedAudio.close();

        loadedAudio = new Audio(audio);

        // Store the audio file relative path.
        URI base = new File(System.getProperty("user.dir")).toURI();
        URI audioURI = audio.toURI();
        String audioRelativePath = String.valueOf(base.relativize(audioURI));
        setProp("audio", audioRelativePath);

        // Display the file name on the timeline.
        timeline.setDisplayFileName(audio.getName());

        // Ready the wave img.
        int imgWidth = (int) (getLoadedAudio().getTotalTime() * PIXEL_TIME_RATIO * Timeline.SLIDER_MAX_VAL * 0.01f);

        timeline.setWaveImg(BoxWaveform.loadImage(
                audio,
                new Dimension(imgWidth, 50), 1,
                new Color(5, 80, 20)));

        // Stop the timer and revalidate the canvas.
        timeline.timeStop();
        canvas.setSize();
        canvas.revalidate();

        System.out.println("Loaded audio: " + audio);
    }

    public int getRedundantMarkQuantity() {
        int numSlashN = Collections.frequency(textList, "\n");
        int numDoubleSlashN = getNumDoubleSlashN();
        int q = marks.size() - (textList.size() - numSlashN + numDoubleSlashN + 1);
        return Math.max(0, q);
    }

    public void saveFileAs(File file) {
        // Marks.
        StringBuilder stringBuilder = new StringBuilder();
        for (Long t : marks) {
            stringBuilder.append(t).append(",");
        }
        props.setProperty("marks", stringBuilder.toString());

        // Save to file.
        try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            props.store(fileWriter, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Project saved to: " + file);
    }

    public void load(File file) {
        mainFrame.setTitle(Main.INIT_FRAME_TITLE + " - " + file.getName());

        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            // Load the properties file.
            props.load(inputStreamReader);

            setText(props.getProperty("text")); // Update the text list.
            mainFrame.getTextArea().setText(getProp("text")); // Update to text area.

            // Marks.
            String[] marksStrings = props.getProperty("marks").split(",");
            marks.clear();
            for (String string : marksStrings)
                marks.add(Long.valueOf(string));

            // Audio.
            File audioFile = new File(props.getProperty("audio"));
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
                fcr.setFileFilter(Main.WAV_EXT_FILTER);
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

    public void load(URL url) {
        try {
            load(new File(url.toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private int getNumDoubleSlashN() {
        String text = getProp("text");
        int frequency = 0;
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '\n' && text.charAt(i + 1) == '\n')
                frequency++;
        }

        return frequency;
    }
}
