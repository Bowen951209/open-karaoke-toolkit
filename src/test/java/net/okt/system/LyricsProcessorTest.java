package net.okt.system;

import net.okt.Main;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LyricsProcessorTest {
    private final Main mainFrame = new Main("test");
    private final SaveLoadManager saveLoadManager = new SaveLoadManager(mainFrame);
    private final LyricsProcessor lyricsProcessor = new LyricsProcessor(saveLoadManager);
    private final String lyricsSample1 = """
            ab
            c'd

            efg""";
    private final String lyricsSample2 = """
            a
            bc

            d
            ef'g""";
    private final String lyricsSample3 = """
            abc
            def
            ghi
            
            jkl
            mno
            """;

    @Test
    void lineStartMarkTest() {
        // sample 1
        lyricsProcessor.setLyrics(lyricsSample1);

        assertEquals(0, lyricsProcessor.getStartMarkAtLine(0));
        assertEquals(2, lyricsProcessor.getStartMarkAtLine(1));
        // skip line 2 because we don't care the start mark of a blank line, which is undefined.
        assertEquals(4, lyricsProcessor.getStartMarkAtLine(3));

        // sample 2
        lyricsProcessor.setLyrics(lyricsSample2);

        assertEquals(0, lyricsProcessor.getStartMarkAtLine(0));
        assertEquals(1, lyricsProcessor.getStartMarkAtLine(1));
        // skip line 2 because we don't care the start mark of a blank line, which is undefined.
        assertEquals(4, lyricsProcessor.getStartMarkAtLine(3));
        assertEquals(5, lyricsProcessor.getStartMarkAtLine(4));
    }

    @Test
    void textBeforeMarkTestForLyrics1() {
        lyricsProcessor.setLyrics(lyricsSample1);

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("a", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("b", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("cd", lyricsProcessor.getTextBeforeMark(3));
        assertNull(lyricsProcessor.getTextBeforeMark(4));
        assertEquals("e", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("f", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("g", lyricsProcessor.getTextBeforeMark(7));
    }

    @Test
    void textBeforeMarkTestForLyrics2() {
        lyricsProcessor.setLyrics(lyricsSample2);

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("a", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("b", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("c", lyricsProcessor.getTextBeforeMark(3));
        assertNull(lyricsProcessor.getTextBeforeMark(4));
        assertEquals("d", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("e", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("fg", lyricsProcessor.getTextBeforeMark(7));
    }

    @Test
    void textBeforeMarkTestForLyrics3() {
        lyricsProcessor.setLyrics(lyricsSample3);

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("a", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("b", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("c", lyricsProcessor.getTextBeforeMark(3));
        assertEquals("d", lyricsProcessor.getTextBeforeMark(4));
        assertEquals("e", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("f", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("g", lyricsProcessor.getTextBeforeMark(7));
        assertEquals("h", lyricsProcessor.getTextBeforeMark(8));
        assertEquals("i", lyricsProcessor.getTextBeforeMark(9));
        assertNull(lyricsProcessor.getTextBeforeMark(10));
        assertEquals("j", lyricsProcessor.getTextBeforeMark(11));
        assertEquals("k", lyricsProcessor.getTextBeforeMark(12));
        assertEquals("l", lyricsProcessor.getTextBeforeMark(13));
        assertEquals("m", lyricsProcessor.getTextBeforeMark(14));
        assertEquals("n", lyricsProcessor.getTextBeforeMark(15));
        assertEquals("o", lyricsProcessor.getTextBeforeMark(16));
    }

    @Test
    void displayingLinesTestForLyrics1() {
        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600, 700, 800));

        lyricsProcessor.setLyrics(lyricsSample1);

        lyricsProcessor.setTime(50); // before mark 0
        assertArrayEquals(new int[] {0 , 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(150); // mark 0 ~ 1
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(250); // mark 1 ~ 2
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(350); // mark 2 ~ 3
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());

        // skip mark 3 ~ 4 because we don't care the displaying lines of a blank line.

        lyricsProcessor.setTime(550); // mark 4 ~ 5
        assertArrayEquals(new int[] {3, 2}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(650); // mark 5 ~ 6
        assertArrayEquals(new int[] {3, 2}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(750); // mark 6 ~ 7
        assertArrayEquals(new int[] {3, 2}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(850); // after mark 7
        assertArrayEquals(new int[] {3, 2}, lyricsProcessor.getDisplayingLines());
    }

    @Test
    void displayingLinesTestForLyrics2() {
        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600, 700, 800));

        lyricsProcessor.setLyrics(lyricsSample2);

        lyricsProcessor.setTime(50); // before mark 0
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(150); // mark 0 ~ 1
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(250); // mark 1 ~ 2
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(350); // mark 2 ~ 3
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());

        // skip mark 3 ~ 4 because we don't care the displaying lines of a blank line.

        lyricsProcessor.setTime(550); // mark 4 ~ 5
        assertArrayEquals(new int[] {3, 4}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(650); // mark 5 ~ 6
        assertArrayEquals(new int[] {3, 4}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(750); // mark 6 ~ 7
        assertArrayEquals(new int[] {3, 4}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(850); // after mark 7
        assertArrayEquals(new int[] {3, 4}, lyricsProcessor.getDisplayingLines());
    }

    @Test
    void displayingLinesTestForLyrics3() {
        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(
                100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700
        ));

        lyricsProcessor.setLyrics(lyricsSample3);

        lyricsProcessor.setTime(50); // before mark 0
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(150); // mark 0 ~ 1
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(250); // mark 1 ~ 2
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(350); // mark 2 ~ 3
        assertArrayEquals(new int[] {0, 1}, lyricsProcessor.getDisplayingLines());

        lyricsProcessor.setTime(450); // mark 3 ~ 4
        assertArrayEquals(new int[] {2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(550); // mark 4 ~ 5
        assertArrayEquals(new int[] {2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(650); // mark 5 ~ 6
        assertArrayEquals(new int[] {2, 1}, lyricsProcessor.getDisplayingLines());

        lyricsProcessor.setTime(750); // mark 6 ~ 7
        assertArrayEquals(new int[] {2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(850); // mark 7 ~ 8
        assertArrayEquals(new int[] {2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(950); // mark 8 ~ 9
        assertArrayEquals(new int[] {2, 1}, lyricsProcessor.getDisplayingLines());

        // skip mark 9 ~ 10 because we don't care the displaying lines of a blank line.

        lyricsProcessor.setTime(1150); // mark 10 ~ 11
        assertArrayEquals(new int[] {4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1250); // mark 11 ~ 12
        assertArrayEquals(new int[] {4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1350); // mark 12 ~ 13
        assertArrayEquals(new int[] {4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1450); // mark 13 ~ 14
        assertArrayEquals(new int[] {4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1550); // mark 14 ~ 15
        assertArrayEquals(new int[] {4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1650); // mark 15 ~ 16
        assertArrayEquals(new int[] {4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1750); // after mark 16
        assertArrayEquals(new int[] {4, 5}, lyricsProcessor.getDisplayingLines());
    }

    //TODO: write tests for the bunch of methods in LyricsProcessor.
}