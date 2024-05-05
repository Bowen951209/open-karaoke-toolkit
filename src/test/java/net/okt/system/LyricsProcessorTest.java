package net.okt.system;

import net.okt.Main;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LyricsProcessorTest {
    @Test
    void lyricsToMarkTest() {
        Main mainFrame = new Main("test");
        LyricsProcessor lyricsProcessor = new LyricsProcessor(mainFrame.getSaveLoadManager());

        // sample 1
        lyricsProcessor.setLyrics("ab\nc'd\n\nefg");
        lyricsProcessor.process();

        String[] texts1 = new String[8];
        for (int i = 0; i < 8; i++) {
            texts1[i] = lyricsProcessor.getTextBeforeMark(i);
        }

        assertArrayEquals(new String[] {null, "a", "b", "cd", null, "e", "f" ,"g"}, texts1);

        // sample 2
        lyricsProcessor.setLyrics("a\nbc\n\nd\nef'g");
        lyricsProcessor.process();
        String[] texts2 = new String[8];
        for (int i = 0; i < 8; i++) {
            texts2[i] = lyricsProcessor.getTextBeforeMark(i);
        }

        assertArrayEquals(new String[] {null, "a", "b", "c", null, "d", "e" ,"fg"}, texts2);
    }

    //TODO: write tests for the bunch of methods in LyricsProcessor.
}