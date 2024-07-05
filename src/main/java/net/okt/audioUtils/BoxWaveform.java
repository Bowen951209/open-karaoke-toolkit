package net.okt.audioUtils;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Class modified from @GOXR3PLUS STUDIO
 */
public class BoxWaveform {

    //TODO: Enable this for more formats with ffmpeg.
    private static void drawImage(BufferedImage img, float[] samples, int boxWidth, Dimension size, Color color) {
        Graphics2D g2d = img.createGraphics();

        int numSubsets = size.width / boxWidth;
        int subsetLength = samples.length / numSubsets;

        float[] subsets = new float[numSubsets];

        // find average(abs) of each box subset
        int s = 0;
        for (int i = 0; i < subsets.length; i++) {

            double sum = 0;
            for (int k = 0; k < subsetLength; k++) {
                sum += Math.abs(samples[s++]);
            }

            subsets[i] = (float) (sum / subsetLength);
        }

        // find the peak so the waveform can be normalized
        // to the height of the image
        float normal = 0;
        for (float sample : subsets) {
            if (sample > normal)
                normal = sample;
        }

        // normalize and scale
        normal = 32768.0f / normal;
        for (int i = 0; i < subsets.length; i++) {
            subsets[i] *= normal;
            subsets[i] = (subsets[i] / 32768.0f) * ((float) size.height / 2);
        }

        g2d.setColor(color);

        // convert to image coords and do actual drawing
        for (int i = 0; i < subsets.length; i++) {
            int sample = (int) subsets[i];

            int posY = (size.height / 2) - sample;
            int negY = (size.height / 2) + sample;

            int x = i * boxWidth;

            g2d.drawLine(x, posY, x, negY);
        }

        g2d.dispose();
    }


    // handle most WAV and AIFF files
    public static BufferedImage loadImage(File file, Dimension size, int boxWidth, Color color) {
        float[] samples;


        //Now procceeed orgasm...
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file)){
            AudioFormat fmt = in.getFormat();

            if (fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                throw new UnsupportedAudioFileException("unsigned");
            }

            boolean big = fmt.isBigEndian();
            int chans = fmt.getChannels();
            int bits = fmt.getSampleSizeInBits();
            int bytes = bits + 7 >> 3;

            int frameLength = (int) in.getFrameLength();
            int bufferLength = chans * bytes * 1024;

            samples = new float[frameLength];
            byte[] buf = new byte[bufferLength];

            int i = 0;
            int bRead;
            while ((bRead = in.read(buf)) > -1) {

                for (int b = 0; b < bRead; ) {
                    double sum = 0;

                    // (sums to mono if multiple channels)
                    for (int c = 0; c < chans; c++) {
                        if (bytes == 1) {
                            sum += buf[b++] << 8;

                        } else {
                            int sample = 0;

                            // (quantizes to 16-bit)
                            if (big) {
                                sample |= (buf[b++] & 0xFF) << 8;
                                sample |= (buf[b++] & 0xFF);
                                b += bytes - 2;
                            } else {
                                b += bytes - 2;
                                sample |= (buf[b++] & 0xFF);
                                sample |= (buf[b++] & 0xFF) << 8;
                            }

                            final int sign = 1 << 15;
                            final int mask = -1 << 16;
                            if ((sample & sign) == sign) {
                                sample |= mask;
                            }

                            sum += sample;
                        }
                    }

                    samples[i++] = (float) (sum / chans);
                }
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }

        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);

        drawImage(img, samples, boxWidth, size, color);

        return img;
    }
}
