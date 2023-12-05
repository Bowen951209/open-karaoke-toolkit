package net.bowen.system;

import net.bowen.Main;
import net.bowen.audioUtils.Audio;
import net.bowen.audioUtils.BoxWaveform;
import net.bowen.gui.Timeline;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static net.bowen.gui.Timeline.PIXEL_TIME_RATIO;

/**
 * This class store the state the user is at. For example, loaded audio, text, etc.
 * */
public class SaveLoadManager {
    private final Main mainFrame;
    private Audio loadedAudio;

    public SaveLoadManager(Main mainFrame) {
        this.mainFrame = mainFrame;
    }

    public Audio getLoadedAudio() {
        return loadedAudio;
    }

    public void setLoadedAudio(URL audio) {
        if (loadedAudio != null)
            loadedAudio.close();

        this.loadedAudio = new Audio(audio);
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
    }
}
