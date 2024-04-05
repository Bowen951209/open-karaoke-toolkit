package net.okt.system;

import net.okt.gui.ProgressBarDialog;
import net.okt.gui.Viewport;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoMaker extends Thread {
    public static final Map<String, Integer> CODEC_MAP = getCodecMap();
    private static final Java2DFrameConverter FRAME_CONVERTER = new Java2DFrameConverter();

    private final AtomicBoolean shouldRun = new AtomicBoolean(true);
    private final String filename, format, codec;
    private final int fps, bitrate, width, height;
    private final long timeLength;
    private final Viewport viewport;
    private final SaveLoadManager saveLoadManager;
    private final ProgressBarDialog progressBarDialog;

    public VideoMaker(String filename, String format, String codec, int fps, int bitrate, long timeLength, int width,
                      int height, Viewport viewport, SaveLoadManager saveLoadManager, ProgressBarDialog progressBarDialog) {
        this.filename = filename;
        this.format = format;
        this.codec = codec;
        this.fps = fps;
        this.bitrate = bitrate;
        this.timeLength = timeLength;
        this.width = width;
        this.height = height;
        this.viewport = viewport;
        this.saveLoadManager = saveLoadManager;
        this.progressBarDialog = progressBarDialog;
    }

    private static Map<String, Integer> getCodecMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("H.264/AVC", avcodec.AV_CODEC_ID_H264);
        map.put("H.265/HEVC", avcodec.AV_CODEC_ID_HEVC);
        map.put("AV1", avcodec.AV_CODEC_ID_AV1);
        map.put("VP9", avcodec.AV_CODEC_ID_VP9);
        map.put("PNG(transparent background)", avcodec.AV_CODEC_ID_PNG);

        return map;
    }

    public void stopProcessing() {
        shouldRun.set(false);
        System.out.println("Output is stopped.");
    }

    @Override
    public void run() {
        int totalFrames = (int) (fps * timeLength * 0.001f);
        float frameLength = 1000f / fps;

        progressBarDialog.progressBar.setMinimum(0);
        progressBarDialog.progressBar.setMaximum(totalFrames);

        FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(saveLoadManager.getProp("audio"));
        try {
            audioGrabber.start();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
        int audioChannels = audioGrabber.getAudioChannels();

        FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(filename, width, height, audioChannels);
        FFmpegLogCallback.set();

        // Video
        int codecID = CODEC_MAP.get(codec);
        frameRecorder.setVideoCodec(codecID);
        frameRecorder.setFormat(format);
        frameRecorder.setFrameRate(fps);
        frameRecorder.setVideoBitrate(bitrate);
        if (codecID == avcodec.AV_CODEC_ID_PNG)
            frameRecorder.setPixelFormat(avutil.AV_PIX_FMT_RGBA);

        // Audio
        frameRecorder.setAudioCodec(audioGrabber.getAudioCodec());
        frameRecorder.setSampleFormat(audioGrabber.getSampleFormat());
        frameRecorder.setSampleRate(audioGrabber.getSampleRate());

        // Recording frames.
        try {
            frameRecorder.start();

            // Record video.
            for (long i = 0; i < totalFrames; i++) {
                if (!shouldRun.get()) return; // if stop processing, return.

                long time = (long) (i * frameLength);
                viewport.drawToBufImg(time);

                Frame frame = FRAME_CONVERTER.getFrame(viewport.getBufferedImage());
                frameRecorder.record(frame, avutil.AV_PIX_FMT_ARGB);  // video

                progressBarDialog.progressBar.setValue((int) i);
                progressBarDialog.progressBar.setString("frames: " + i + "/" + totalFrames);
            }

            // Record audio.
            Frame audioFrame;
            while ((audioFrame = audioGrabber.grabFrame()) != null) {
                // Because audio and recorder have a different frame rate, we need to correct the time stamp.
                frameRecorder.setTimestamp(audioFrame.timestamp);
                frameRecorder.record(audioFrame);

                // We break if we have recorded the needed length of time.
                if (audioFrame.timestamp * 0.001f > timeLength) break;

                // The audio recording is fast, so no need to print the progress.
            }

            audioGrabber.close();
            frameRecorder.close();

            String finishMsg = "Video outputted to: " + filename;
            System.out.println(finishMsg);
            progressBarDialog.showFinish(finishMsg);
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }
}
