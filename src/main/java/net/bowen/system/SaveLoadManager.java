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

import static net.bowen.gui.Timeline.PIXEL_TIME_RATIO;

/**
 * This class store the state the user is at. For example, loaded audio, text, etc.
 */
public class SaveLoadManager {
    private final Main mainFrame;

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
    public String getText() {
        return data.text;
    }

    public void setLoadedAudio(URL audio) {
        mainFrame.getTimeline().setDisplayFileName(new File(audio.getFile()).getName());

        if (loadedAudio != null)
            loadedAudio.close();

        data.loadedAudioURL = audio;
        loadedAudio = new Audio(audio);
        try {
            mainFrame.setTitle(Main.INIT_FRAME_TITLE + " - *" + new File(audio.toURI()).getName());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Timeline timeline = mainFrame.getTimeline();
        if (timeline == null) return;

        Timeline.Canvas canvas = timeline.getCanvas();


        int canvasWidth = (int) (getLoadedAudio().getTotalTime() * PIXEL_TIME_RATIO);
        canvas.setPreferredSize(new Dimension(canvasWidth, 0));

        mainFrame.getTimeline().setWaveImg(BoxWaveform.loadImage(
                audio,
                new Dimension(canvasWidth, 50), 1,
                new Color(5, 80, 20)));

        timeline.timeStop();
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
    }
}
