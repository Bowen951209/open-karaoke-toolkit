package net.okt.system;

import net.okt.Main;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LyricsProcessorTest {
    private final Main mainFrame = new Main("test");
    private final LyricsProcessor lyricsProcessor = new LyricsProcessor(mainFrame.getSaveLoadManager());
    private final String lyricsSample1 = """
            ab
            c'd

            efg""";
    private final String lyricsSample2 = """
            a
            bc

            d
            ef'g""";

    @Test
    void lineStartMarkTest() {
        // sample 1
        lyricsProcessor.setLyrics(lyricsSample1);
        lyricsProcessor.process();

        assertEquals(0, lyricsProcessor.getStartMarkAtLine(0));
        assertEquals(2, lyricsProcessor.getStartMarkAtLine(1));
        // skip line 2 because we don't care the start mark of a blank line, which is undefined.
        assertEquals(4, lyricsProcessor.getStartMarkAtLine(3));

        // sample 2
        lyricsProcessor.setLyrics(lyricsSample2);
        lyricsProcessor.process();

        assertEquals(0, lyricsProcessor.getStartMarkAtLine(0));
        assertEquals(1, lyricsProcessor.getStartMarkAtLine(1));
        // skip line 2 because we don't care the start mark of a blank line, which is undefined.
        assertEquals(4, lyricsProcessor.getStartMarkAtLine(3));
        assertEquals(5, lyricsProcessor.getStartMarkAtLine(4));
    }

    @Test
    void textBeforeMarkTest() {
        // sample 1
        lyricsProcessor.setLyrics(lyricsSample1);
        lyricsProcessor.process();

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("a", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("b", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("cd", lyricsProcessor.getTextBeforeMark(3));
        assertNull(lyricsProcessor.getTextBeforeMark(4));
        assertEquals("e", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("f", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("g", lyricsProcessor.getTextBeforeMark(7));

        // sample 2
        lyricsProcessor.setLyrics(lyricsSample2);
        lyricsProcessor.process();

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("a", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("b", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("c", lyricsProcessor.getTextBeforeMark(3));
        assertNull(lyricsProcessor.getTextBeforeMark(4));
        assertEquals("d", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("e", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("fg", lyricsProcessor.getTextBeforeMark(7));
    }

    //TODO: write tests for the bunch of methods in LyricsProcessor.
}