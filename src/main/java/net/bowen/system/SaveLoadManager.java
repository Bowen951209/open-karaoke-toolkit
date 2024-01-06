package net.bowen.system;

import net.bowen.Main;
import net.bowen.audioUtils.Audio;
import net.bowen.audioUtils.BoxWaveform;
import net.bowen.gui.Timeline;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.bowen.gui.Timeline.PIXEL_TIME_RATIO;

/**
 * This class store the state the user is at. For example, loaded audio, text, etc.
 */
public class SaveLoadManager {
    private final Main mainFrame;
    private final List<String> textList = new ArrayList<>();

    private Data data = new Data();
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
                String nextWord = String.valueOf(text.charAt(i + 1));

                if (nextWord.equals("'")) {// linked word case
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

    public void setFontSize(int s) {
        data.fontSize = s;
    }
    public int getFontSize() {
        return data.fontSize;
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
        data.text = mainFrame.getTextArea().getText();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Project saved to: " + file);
    }

    public void load(File file) {
        mainFrame.setTitle(Main.INIT_FRAME_TITLE + " - " + file.getName());

        // Load the serializable.
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            data = (Data) objectInputStream.readObject();

            fileInputStream.close();
            objectInputStream.close();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        setLoadedAudio(data.loadedAudioURL);
        mainFrame.getTextArea().setText(data.text);

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
        URL loadedAudioURL;
        ArrayList<Long> marks = new ArrayList<>();
        String text = "";
        int fontSize;
    }
}
