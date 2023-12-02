package net.bowen.system;

import net.bowen.audioUtils.Audio;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This class store the state the user is at. For example, loaded audio, text, etc.
 * */
public class SaveLoadManager {
    private final JFrame mainFrame;
    private Audio loadedAudio;

    public SaveLoadManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public Audio getLoadedAudio() {
        return loadedAudio;
    }

    public void setLoadedAudio(URL loadedAudio) {
        this.loadedAudio = new Audio(loadedAudio);
        try {
            mainFrame.setTitle(mainFrame.getTitle() + " - *" + new File(loadedAudio.toURI()).getName());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLoadedAudio(File f) {
        try {
            this.loadedAudio = new Audio(f.toURI().toURL());
            mainFrame.setTitle(mainFrame.getTitle() + " - *" + f.getName());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
