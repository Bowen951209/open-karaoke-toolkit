package net.okt.system;

import javax.swing.filechooser.FileNameExtensionFilter;

public class FileExtensionUtils {
    public static final FileNameExtensionFilter AUDIO_EXT_FILTER = new FileNameExtensionFilter(
        "m4a, mp3, aac, ogg, flac, alac, wav, aiff, dsd, pcm",
        "m4a", "mp3", "aac", "ogg", "flac", "alac", "wav", "aiff", "dsd", "pcm"
    );
    public static final FileNameExtensionFilter PROPS_EXT_FILTER = new FileNameExtensionFilter(
            "properties",
            "properties"
    );
}
