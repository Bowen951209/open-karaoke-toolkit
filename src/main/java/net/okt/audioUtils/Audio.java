package net.okt.audioUtils;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.nio.ShortBuffer;

public class Audio {
    private final SourceDataLine line;
    private final FFmpegFrameGrabber grabber;
    private final int sampleRate;

    private Thread playThread;
    private boolean isPlaying;
    private boolean isFinished;
    private float speed = 1;
    /**
     * The {@link #line} position where last time method {@link #setTimeTo(int)} was called.
     */
    private long jumpLinePosition;
    /**
     * The specified jump time set by {@link #setTimeTo(int)}.
     */
    private long jumpPosition;

    public Audio(File audioFile) throws Exception {
        // Create a FFmpegFrameGrabber to grab audio frames.
        grabber = new FFmpegFrameGrabber(audioFile);
        grabber.start();

        this.sampleRate = grabber.getSampleRate();

        // Get the audio format.
        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, grabber.getAudioChannels(),
                true, false);

        // Create a SourceDataLine, which can play the audio.
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
    }

    public void play() {
        isPlaying = true;
        isFinished = false;

        playThread = new Thread(() -> {
            line.start();

            // Play the audio.
            Frame frame;
            try {
                while ((frame = grabber.grabFrame()) != null && isPlaying) {
                    if (frame.samples != null) {
                        ShortBuffer sb = (ShortBuffer) frame.samples[0];
                        byte[] audioBytes = new byte[sb.remaining() * 2];
                        for (int i = 0; sb.remaining() > 0; i += 2) {
                            short val = sb.get();
                            audioBytes[i] = (byte) (val & 0xff);
                            audioBytes[i + 1] = (byte) ((val >> 8) & 0xff);
                        }
                        line.write(audioBytes, 0, audioBytes.length);
                    }
                }

                if (frame == null) {
                    line.drain();
                    line.stop();
                    isFinished = true;
                }
            } catch (FrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
        });
        playThread.start();
    }

    public void close() {
        isPlaying = false;
        line.flush();
        line.stop();
        line.close();
        try {
            grabber.close();
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void pause() {
        if (playThread == null) return;

        isPlaying = false;
        line.stop();
    }

    public void setTimeTo(int ms) {
        try {
            // Flush the buffer to clean the data left. This can make sure playback without previous sound left.
            line.flush();

            grabber.setAudioTimestamp(ms * 1000L);
            jumpPosition = ms;
            jumpLinePosition = line.getMicrosecondPosition() / 1000;
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setSpeed(float speed) {
        // Reset jumpLinePosition and jumpPosition for correct time position calculation.
        setTimeTo(getTimePosition());

        this.speed = speed;
        grabber.setSampleRate((int) (sampleRate / speed));
    }

    /**
     * @return The current playing time.
     */
    public int getTimePosition() {
        int linePosition = (int) (line.getMicrosecondPosition() / 1000);
        return (int) ((linePosition - jumpLinePosition) * speed + jumpPosition);
    }

    public int getTotalTime() {
        return (int) grabber.getLengthInTime() / 1000;
    }

    /**
     * @return If the audio has finished playing.
     */
    public boolean isFinished() {
        return isFinished;
    }
}
