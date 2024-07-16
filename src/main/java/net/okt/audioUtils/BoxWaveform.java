package net.okt.audioUtils;


import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ShortBuffer;


/**
 * Class modified from @GOXR3PLUS STUDIO
 */
public class BoxWaveform {
    public static BufferedImage loadImage(File file, Dimension size, Color color) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file.getPath())) {
            grabber.start();

            long lengthInNanoTime = grabber.getLengthInTime();
            long nanoPerPixel = lengthInNanoTime / size.width;
            int arrSize = size.width;
            short[] samples = new short[arrSize];

            short peakVal = 0; // the max of all samples.
            int idx = 0;
            for (int i = 0; i < arrSize; i++) {
                grabber.setAudioTimestamp(i * nanoPerPixel);
                Frame audioFrame = grabber.grabSamples();
                ShortBuffer buffer = (ShortBuffer) audioFrame.samples[0];

                // Only the 0th element represent the value of this timestamp.
                samples[idx] = buffer.get(0);

                // update peak value if larger.
                if (samples[idx] > peakVal)
                    peakVal = samples[idx];

                idx++;
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
     * @param peakVal The max of all samples. This is for scaling the waveform, so it'll look clearer.
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
    }
}
