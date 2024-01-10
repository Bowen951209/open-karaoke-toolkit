package net.bowen.system;

import net.bowen.Main;
import net.bowen.audioUtils.Audio;
import net.bowen.audioUtils.BoxWaveform;
import net.bowen.gui.Timeline;

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

    public List<String> getTextList() {
        return textList;
    }

    public int redundantMarkQuantity() {
        int numSlashN = Collections.frequency(textList, "\n");
        int q = data.marks.size() - (textList.size() - numSlashN + 1);
        return Math.max(0, q);
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
            setLoadedAudio(new File(props.getProperty("audio")));
            setText(props.getProperty("text"));
            mainFrame.getTextArea().setText(data.text); // also update to text area.
            data.defaultFontSize = Integer.parseInt(props.getProperty("defaultFontSize"));
            mainFrame.getDefaultFontSizeBar().set(data.defaultFontSize);// also update to bar.
            data.linkedFontSize = Integer.parseInt(props.getProperty("linkedFontSize"));
            mainFrame.getLinkedFontSizeBar().set(data.linkedFontSize);// also update to bar.
            // marks
            String[] marksStrings = props.getProperty("marks").split(",");
            data.marks.clear();
            for (String string : marksStrings) {
                data.marks.add(Long.valueOf(string));
            }
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

    private static class Data implements Serializable {
        final ArrayList<Long> marks = new ArrayList<>();
        URL loadedAudioURL;
        String text = "";
        int defaultFontSize;
        int linkedFontSize;
    }
}
