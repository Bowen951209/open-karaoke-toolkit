package net.okt.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LyricsProcessorTest {
    @Test
    void lyricsToMarkTest() {
        LyricsProcessor lyricsProcessor = new LyricsProcessor();

        // sample 1
        lyricsProcessor.setLyrics("ab\nc'd\n\nefg");
        lyricsProcessor.genTextToMarksList();

        String[] texts1 = new String[8];
        for (int i = 0; i < 8; i++) {
            texts1[i] = lyricsProcessor.getTextBeforeMark(i);
        }

        assertArrayEquals(new String[] {null, "a", "b", "cd", null, "e", "f" ,"g"}, texts1);

        // sample 2
        lyricsProcessor.setLyrics("a\nbc\n\nd\nef'g");
        lyricsProcessor.genTextToMarksList();
        String[] texts2 = new String[8];
        for (int i = 0; i < 8; i++) {
            texts2[i] = lyricsProcessor.getTextBeforeMark(i);
        }

        assertArrayEquals(new String[] {null, "a", "b", "c", null, "d", "e" ,"fg"}, texts2);
    }
}