package net.okt.audioUtils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Audio {
    private final Clip clip;
    private final int totalTime;

    public int getTotalTime() {
        return totalTime;
    }

    public Audio(File src) {
        try {
            this.clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(src)) {
            clip.open(audioInputStream);
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            totalTime = (int) ((float) frames / format.getFrameRate() * 1000);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void play() {
        clip.start();
    }

    public void pause() {
        clip.stop();
    }

    public void close() {
        clip.close();
    }

    public void setTimeTo(int ms) {
        float ratio = (float) ms / totalTime;
        clip.setFramePosition((int) (ratio * clip.getFrameLength()));
    }

    /**
     * @return The current playing time.
     * */
    public int getTimePosition() {
        float timeScale = (float) clip.getFramePosition() / clip.getFrameLength();
        return (int) (timeScale * totalTime);
    }
}
