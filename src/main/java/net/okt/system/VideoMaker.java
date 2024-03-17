package net.okt.system;

import net.okt.gui.Viewport;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_ARGB;

public class VideoMaker {
    public static final Map<String, Integer> CODEC_MAP = getCodecMap();

    public static void genVideo(String filename, String codec, int fps, int bitrate, long timeLength, int width,
                                int height, Viewport viewport, SaveLoadManager saveLoadManager) {

        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
        int totalFrames = (int) (fps * timeLength * 0.001f);
        float frameLength = 1000f / fps;

        FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(saveLoadManager.getProp("audio"));
        try {
            audioGrabber.start();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
        int audioChannels = audioGrabber.getAudioChannels();

        FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(filename, width, height, audioChannels);
        // Video
        frameRecorder.setVideoCodec(CODEC_MAP.get(codec));
        frameRecorder.setFormat("mp4");
        frameRecorder.setFrameRate(fps);
        frameRecorder.setVideoBitrate(bitrate);

        // Audio
        frameRecorder.setAudioCodec(audioGrabber.getAudioCodec());
        frameRecorder.setSampleFormat(audioGrabber.getSampleFormat());
        frameRecorder.setSampleRate(audioGrabber.getSampleRate());

        // Recording frames.
        try {
            frameRecorder.start();

            // Record video.
            for (long i = 0; i < totalFrames; i++) {
                long time = (long) (i * frameLength);
                viewport.drawToBufImg(time);

                Frame frame = java2DFrameConverter.getFrame(viewport.getBufferedImage());
                frameRecorder.record(frame, AV_PIX_FMT_ARGB);  // video

                System.out.print("\rframes: " + i + "/" + totalFrames);
            }

            // Record audio.
            Frame audioFrame;
            while ((audioFrame = audioGrabber.grabFrame()) != null) {
                // Because audio and recorder has a different frame rate, we need to correct the time stamp.
                frameRecorder.setTimestamp(audioFrame.timestamp);
                frameRecorder.record(audioFrame);

                // We break if we have recorded the needed length of time.
                if (audioFrame.timestamp * 0.001f > timeLength) break;

                // The audio recording is fast, so no need to print the progress.
            }

            audioGrabber.close();
            frameRecorder.close();
            System.out.println("\nVideo Outputed To: " + filename + ".");
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Integer> getCodecMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("H.264/AVC", avcodec.AV_CODEC_ID_H264);
        map.put("H.265/HEVC", avcodec.AV_CODEC_ID_HEVC);
        map.put("AV1", avcodec.AV_CODEC_ID_AV1);
        map.put("VP9", avcodec.AV_CODEC_ID_VP9);

        return map;
    }
}
