package net.bowen.system;

import net.bowen.Main;
import net.bowen.audioUtils.Audio;
import net.bowen.audioUtils.BoxWaveform;
import net.bowen.gui.Timeline;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static net.bowen.gui.Timeline.PIXEL_TIME_RATIO;

/**
 * This class store the state the user is at. For example, loaded audio, text, etc.
 */
public class SaveLoadManager {
    private final Main mainFrame;
    
    // TODO: String -> char
    private final List<String> textList = new ArrayList<>();

    private final Data data = new Data();
    private Audio loadedAudio;

    public SaveLoadManager(Main mainFrame) {
        this.mainFrame = mainFrame;
    }

    public Audio getLoadedAudio() {
        return loadedAudio;
    }

    public ArrayList<Long> getMarks() {
        return data.marks;
    }

    public void setText(String text) {
        data.text = text;
        textList.clear();

        // Set the textList.
        for (int i = 0; i < text.length(); i++) {
            String thisWord = String.valueOf(text.charAt(i));

            if (i + 1 < text.length()) {// IF not last word
                char nextChar = text.charAt(i + 1);

                if (nextChar == '\'') {// linked word case
                    String linkedWord = thisWord;
                    i += 2;
                    linkedWord += String.valueOf(text.charAt(i));
                    textList.add(linkedWord);
                } else {
                    textList.add(thisWord);
                }
            } else {
                textList.add(thisWord);
            }
        }
    }

    public String getText() {
        return data.text;
    }

    public void setDefaultFontSize(int s) {
        data.defaultFontSize = s;
    }

    public int getDefaultFontSize() {
        return data.defaultFontSize;
    }

    public void setLinkedFontSize(int s) {
        data.linkedFontSize = s;
    }

    public int getLinkedFontSize() {
        return data.linkedFontSize;
    }

    public void setIndentSize(int s) {
        data.indentSize = s;
    }

    public int getIndentSize() {
        return data.indentSize;
    }

    public void setLineSpace(int s) {
        data.lineSpace = s;
    }

    public int getLineSpace() {
        return data.lineSpace;
    }


    public List<String> getTextList() {
        return textList;
    }

    public void setLoadedAudio(URL audio) {
        if (loadedAudio != null)
            loadedAudio.close();

        data.loadedAudioURL = audio;
        loadedAudio = new Audio(audio);

        String path = audio.getPath();
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        mainFrame.getTimeline().setDisplayFileName(fileName);

        Timeline timeline = mainFrame.getTimeline();
        if (timeline == null) return;

        Timeline.Canvas canvas = timeline.getCanvas();


        int imgWidth = (int) (getLoadedAudio().getTotalTime() * PIXEL_TIME_RATIO * Timeline.SLIDER_MAX_VAL * 0.01f);

        mainFrame.getTimeline().setWaveImg(BoxWaveform.loadImage(
                audio,
                new Dimension(imgWidth, 50), 1,
                new Color(5, 80, 20)));

        timeline.timeStop();
        canvas.setSize();
        canvas.revalidate();
    }

    public void setLoadedAudio(File f) {
        try {
            setLoadedAudio(f.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Loaded audio: " + f);
    }

    public int getRedundantMarkQuantity() {
        int numSlashN = Collections.frequency(textList, "\n");
        int numDoubleSlashN = getNumDoubleSlashN();
        int q = data.marks.size() - (textList.size() - numSlashN + numDoubleSlashN + 1);
        return Math.max(0, q);
    }

    public void saveFileAs(File file) {
        Properties props = new Properties();

        URI base = new File(System.getProperty("user.dir")).toURI();
        String audioRelativePath;
        try {
            audioRelativePath = String.valueOf(base.relativize(data.loadedAudioURL.toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // General information.
        props.setProperty("audio", audioRelativePath);
        props.setProperty("text", data.text);
        props.setProperty("defaultFontSize", String.valueOf(data.defaultFontSize));
        props.setProperty("linkedFontSize", String.valueOf(data.linkedFontSize));
        props.setProperty("indentSize", String.valueOf(data.indentSize));
        props.setProperty("lineSpace", String.valueOf(data.lineSpace));

        // Marks.
        StringBuilder stringBuilder = new StringBuilder();
        for (Long t : data.marks) {
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
            Properties props = new Properties();
            props.load(inputStreamReader);

            // Set the values to data.

            // Text
            setText(props.getProperty("text"));
            mainFrame.getTextArea().setText(data.text); // also update to text area.

            // size bars
            data.defaultFontSize = Integer.parseInt(props.getProperty("defaultFontSize"));
            mainFrame.defaultFontSizeBar.set(data.defaultFontSize);// also update to bar.

            data.linkedFontSize = Integer.parseInt(props.getProperty("linkedFontSize"));
            mainFrame.linkedFontSizeBar.set(data.linkedFontSize);// also update to bar.

            data.indentSize = Integer.parseInt(props.getProperty("indentSize"));
            mainFrame.lineIndentSizeBar.set(data.indentSize);// also update to bar.

            data.lineSpace = Integer.parseInt(props.getProperty("lineSpace"));
            mainFrame.lineSpaceSizeConfigBar.set(data.lineSpace);// also update to bar.

            // marks
            String[] marksStrings = props.getProperty("marks").split(",");
            data.marks.clear();
            for (String string : marksStrings)
                data.marks.add(Long.valueOf(string));

            // Audio
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
        String text = data.text;
        int frequency = 0;
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '\n' && text.charAt(i + 1) == '\n')
                frequency++;
        }

        return frequency;
    }

    private static class Data implements Serializable {
        final ArrayList<Long> marks = new ArrayList<>();
        URL loadedAudioURL;
        String text = "";
        int defaultFontSize;
        int linkedFontSize;
        int indentSize;
        int lineSpace;
    }
}
