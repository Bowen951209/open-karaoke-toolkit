package net.okt.system;

import net.okt.gui.Viewport;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_ARGB;

public class VideoMaker {
    public static final Map<String, Integer> CODEC_MAP = getCodecMap();

    public static void genVideo(String filename, String codec, int fps, int bitrate, long timeLength, int width,
                                int height, Viewport viewport) {
        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
        long totalFrames = (long) (fps * timeLength * 0.001f);
        float frameLength = 1000f / fps;

        FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(filename, width, height);
        frameRecorder.setVideoCodec(CODEC_MAP.get(codec));
        frameRecorder.setFormat("mp4");
        frameRecorder.setFrameRate(fps);
        frameRecorder.setVideoBitrate(bitrate);

        try {
            frameRecorder.start();
            for (long i = 0; i < totalFrames; i++) {
                long time = (long) (i * frameLength);
                viewport.drawToBufImg(time);

                Frame frame = java2DFrameConverter.getFrame(viewport.getBufferedImage());
                frameRecorder.record(frame, AV_PIX_FMT_ARGB);

                System.out.print("\rframes: " + i + "/" + totalFrames);
            }

            frameRecorder.close();
            donePrint(filename, timeLength, codec);
        } catch (FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void donePrint(String filename, long timeLength, String codec) {
        System.out.println("\nVideo Outputed To: " + filename + ".");
        System.out.println("Length: " + timeLength + "ms.");
        System.out.println("Codec: " + codec + ".");
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
