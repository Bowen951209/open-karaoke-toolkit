package net.bowen.system;

import net.bowen.Main;
import net.bowen.audioUtils.Audio;
import net.bowen.audioUtils.BoxWaveform;
import net.bowen.gui.Timeline;

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

import static net.bowen.gui.Timeline.PIXEL_TIME_RATIO;

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

    public String getText() {
        return data.text;
    }

    public void setDotsPosX(int x) {
        data.dotsPosX = x;
    }

    public int getDotsPosX() {
        return data.dotsPosX;
    }

    public void setDotsPosY(int y) {
        data.dotsPosY = y;
    }

    public int getDotsPosY() {
        return data.dotsPosY;
    }

    public void setTextPosX(int x) {
        data.textPosX = x;
    }

    public int getTextPosX() {
        return data.textPosX;
    }

    public void setTextPosY(int y) {
        data.textPosY = y;
    }

    public int getTextPosY() {
        return data.textPosY;
    }

    public void setDotsNum(int v) {
        data.dotsNum = v;
    }

    public int getDotsNum() {
        return data.dotsNum;
    }

    public void setDotsPeriod(int v) {
        data.dotsPeriod = v;
    }

    public int getDotsPeriod() {
        return data.dotsPeriod;
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

    public void setLoadedAudio(File audio) {
        Timeline timeline = mainFrame.getTimeline();
        if (timeline == null) return;
        Timeline.Canvas canvas = timeline.getCanvas();

        // If there is a currently loaded audio, close it first.
        if (loadedAudio != null)
            loadedAudio.close();

        data.loadedAudioPath = audio.getPath();
        loadedAudio = new Audio(audio);

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
        int q = data.marks.size() - (textList.size() - numSlashN + numDoubleSlashN + 1);
        return Math.max(0, q);
    }

    public void saveFileAs(File file) {
        Properties props = new Properties();

        URI base = new File(System.getProperty("user.dir")).toURI();
        URI audio = new File(data.loadedAudioPath).toURI();
        String audioRelativePath = String.valueOf(base.relativize(audio));

        // General information.
        props.setProperty("audio", audioRelativePath);
        props.setProperty("text", data.text);
        props.setProperty("textPosX", String.valueOf(data.textPosX));
        props.setProperty("textPosY", String.valueOf(data.textPosY));
        props.setProperty("dotsPosX", String.valueOf(data.dotsPosX));
        props.setProperty("dotsPosY", String.valueOf(data.dotsPosY));
        props.setProperty("dotsNum", String.valueOf(data.dotsNum));
        props.setProperty("dotsPeriod", String.valueOf(data.dotsPeriod));
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
            data.textPosX = Integer.parseInt(props.getProperty("textPosX"));
            mainFrame.textPosXConfigBar.setValue(data.textPosX);// also update to bar.

            data.textPosY = Integer.parseInt(props.getProperty("textPosY"));
            mainFrame.textPosYConfigBar.setValue(data.textPosY);// also update to bar.

            data.dotsPosX = Integer.parseInt(props.getProperty("dotsPosX"));
            mainFrame.dotsPosXConfigBar.setValue(data.dotsPosX);// also update to bar.

            data.dotsPosY = Integer.parseInt(props.getProperty("dotsPosY"));
            mainFrame.dotsPosYConfigBar.setValue(data.dotsPosY);// also update to bar.

            data.dotsNum = Integer.parseInt(props.getProperty("dotsNum"));
            mainFrame.readyDotsNumComboBox.setSelectedItem(data.dotsNum);// also update to bar.

            data.dotsPeriod = Integer.parseInt(props.getProperty("dotsPeriod"));
            mainFrame.readyDotsTimeConfigBar.setValue(data.dotsPeriod);// also update to bar.

            data.defaultFontSize = Integer.parseInt(props.getProperty("defaultFontSize"));
            mainFrame.defaultFontSizeBar.setValue(data.defaultFontSize);// also update to bar.

            data.linkedFontSize = Integer.parseInt(props.getProperty("linkedFontSize"));
            mainFrame.linkedFontSizeBar.setValue(data.linkedFontSize);// also update to bar.

            data.indentSize = Integer.parseInt(props.getProperty("indentSize"));
            mainFrame.lineIndentSizeBar.setValue(data.indentSize);// also update to bar.

            data.lineSpace = Integer.parseInt(props.getProperty("lineSpace"));
            mainFrame.lineSpaceSizeConfigBar.setValue(data.lineSpace);// also update to bar.

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
        String loadedAudioPath;
        String text = "";
        int textPosX;
        int textPosY;
        int dotsPosX;
        int dotsPosY;
        int dotsPeriod;
        int dotsNum;
        int defaultFontSize;
        int linkedFontSize;
        int indentSize;
        int lineSpace;
    }
}
