package net.okt.audioUtils;


import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ShortBuffer;

public class BoxWaveform {
    public static BufferedImage loadImage(File file, Dimension size, Color color) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file.getPath())) {
            grabber.start();

            long lengthInNanoTime = grabber.getLengthInTime();
            int sampleRate = grabber.getSampleRate();
            int channels = grabber.getAudioChannels();
            float samplePerPixel = (float) lengthInNanoTime / 1000000 * sampleRate / size.width * channels;
            int arrSize = size.width;
            short[] samples = new short[arrSize];

            float bufIdx = 0;
            int idx = 0;
            short peakVal = 0;
            Frame frame;

            grabLoop:
            while ((frame = grabber.grabFrame()) != null) {
                if (frame.samples != null) {
                    ShortBuffer sb = (ShortBuffer) frame.samples[0];
                    while (bufIdx < sb.limit()) {
                        if (idx >= arrSize) break grabLoop;

                        // This make sure that we always take the first channel.
                        samples[idx] = sb.get((int) bufIdx % channels == 0 ? (int) bufIdx : (int) bufIdx - 1);

                        if (samples[idx] > peakVal)
                            peakVal = samples[idx];

                        bufIdx += samplePerPixel;
                        idx++;
                    }

                    bufIdx %= sb.limit();
                }
            }

            BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            drawToImage(img, samples, peakVal, color);
            return img;
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param img     The buffered image to draw to.
     * @param samples The samples to draw. Its length should be the same as img's width.
     * @param peakVal The max value of the samples. This is to normalize the waveform.
     * @param color   The color of the waveform.
     */
    private static void drawToImage(BufferedImage img, short[] samples, short peakVal, Color color) {
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        int midY = img.getHeight() / 2;

        g2d.setColor(color);
        for (int i = 0; i < samples.length; i++) {
            int y = samples[i] * midY / peakVal;
            g2d.drawLine(i, midY - y, i, midY + y);
        }

        g2d.dispose();
    }
}
