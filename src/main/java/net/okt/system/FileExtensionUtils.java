package net.okt.system;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileExtensionUtils {
    public static final FileNameExtensionFilter AUDIO_EXT_FILTER = new FileNameExtensionFilter(
        "m4a, mp3, aac, ogg, flac, alac, wav, aiff, dsd, pcm",
        "m4a", "mp3", "aac", "ogg", "flac", "alac", "wav", "aiff", "dsd", "pcm"
    );

    public static final FileNameExtensionFilter PROPS_EXT_FILTER = new FileNameExtensionFilter(
            "properties",
            "properties"
    );

    public static boolean isAudioFile(File file) {
        return AUDIO_EXT_FILTER.accept(file) && file.isFile();
    }

    public static boolean isPropertiesFile(File file) {
        return PROPS_EXT_FILTER.accept(file) && file.isFile();
    }

    /***
     * Check if OKT can load the passed in file object. If the passed in file is a directory, then will check if the files
     * inside contains a loadable file, but won't check the inner directories recursively.
     *
     * @return true if the file can be loaded by OKT.
     */
    public static boolean isLoadable(File file) {
        if (file.isFile()) {
            return AUDIO_EXT_FILTER.accept(file) || PROPS_EXT_FILTER.accept(file);
        } else {
            File[] files = file.listFiles();
            if (files == null) return false;

            for (File childFile : files) {
                if (childFile.isDirectory())
                    continue;
                if (AUDIO_EXT_FILTER.accept(childFile) || PROPS_EXT_FILTER.accept(childFile))
                    return true;
            }

            return false;
        }

    }
}
