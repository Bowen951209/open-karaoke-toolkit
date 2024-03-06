package net.bowen.audioUtils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Audio {
    private final Clip clip;
    private final long totalTime;

    public long getTotalTime() {
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
            totalTime = (long) ((float) frames / format.getFrameRate() * 1000);
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
    public long getTimePosition() {
        float timeScale = (float) clip.getLongFramePosition() / (float) clip.getFrameLength();
        return (long) (timeScale * totalTime);
    }
}
