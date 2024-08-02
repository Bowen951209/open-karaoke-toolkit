package net.okt.system;

import net.okt.Main;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LyricsProcessorTest {
    private final Main mainFrame = new Main("test", null);
    private final SaveLoadManager saveLoadManager = new SaveLoadManager(mainFrame);
    private final LyricsProcessor lyricsProcessor = new LyricsProcessor(saveLoadManager);
    private final String lyricsSample1 = """
            一二
            三'四

            五六七""";
    private final String lyricsSample2 = """
            一
            二三

            四
            五六'七""";
    private final String lyricsSample3 = """
            一二三
            四五六
            七八九
            
            十一二
            三四五""";

    @Test
    void isEasternCharTest() {
        assertTrue(LyricsProcessor.isEasternChar('漢'));
        assertTrue(LyricsProcessor.isEasternChar('汉'));
        assertTrue(LyricsProcessor.isEasternChar('変'));
        assertTrue(LyricsProcessor.isEasternChar('あ'));
        assertTrue(LyricsProcessor.isEasternChar('한'));
    }

    @Test
    void lineStartMarkTestForLyrics1() {
        lyricsProcessor.setLyrics(lyricsSample1);

        assertEquals(0, lyricsProcessor.getStartMarkAtLine(0));
        assertEquals(2, lyricsProcessor.getStartMarkAtLine(1));
        // skip line 2 because we don't care the start mark of a blank line, which is undefined.
        assertEquals(4, lyricsProcessor.getStartMarkAtLine(3));
    }

    @Test
    void lineStartMarkTestForLyrics2() {
        lyricsProcessor.setLyrics(lyricsSample2);

        assertEquals(0, lyricsProcessor.getStartMarkAtLine(0));
        assertEquals(1, lyricsProcessor.getStartMarkAtLine(1));
        // skip line 2 because we don't care the start mark of a blank line, which is undefined.
        assertEquals(4, lyricsProcessor.getStartMarkAtLine(3));
        assertEquals(5, lyricsProcessor.getStartMarkAtLine(4));
    }

    @Test
    void lineStartMarkTestForLyrics3() {
        lyricsProcessor.setLyrics(lyricsSample3);

        assertEquals(0, lyricsProcessor.getStartMarkAtLine(0));
        assertEquals(3, lyricsProcessor.getStartMarkAtLine(1));
        assertEquals(6, lyricsProcessor.getStartMarkAtLine(2));
        // skip line 3 because we don't care the start mark of a blank line, which is undefined.
        assertEquals(10, lyricsProcessor.getStartMarkAtLine(4));
        assertEquals(13, lyricsProcessor.getStartMarkAtLine(5));
    }

    @Test
    void textBeforeMarkTestForLyrics1() {
        lyricsProcessor.setLyrics(lyricsSample1);

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("一", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("二", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("三四", lyricsProcessor.getTextBeforeMark(3));
        assertNull(lyricsProcessor.getTextBeforeMark(4));
        assertEquals("五", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("六", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("七", lyricsProcessor.getTextBeforeMark(7));
    }

    @Test
    void textBeforeMarkTestForLyrics2() {
        lyricsProcessor.setLyrics(lyricsSample2);

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("一", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("二", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("三", lyricsProcessor.getTextBeforeMark(3));
        assertNull(lyricsProcessor.getTextBeforeMark(4));
        assertEquals("四", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("五", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("六七", lyricsProcessor.getTextBeforeMark(7));
    }

    @Test
    void textBeforeMarkTestForLyrics3() {
        lyricsProcessor.setLyrics(lyricsSample3);

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("一", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("二", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("三", lyricsProcessor.getTextBeforeMark(3));
        assertEquals("四", lyricsProcessor.getTextBeforeMark(4));
        assertEquals("五", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("六", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("七", lyricsProcessor.getTextBeforeMark(7));
        assertEquals("八", lyricsProcessor.getTextBeforeMark(8));
        assertEquals("九", lyricsProcessor.getTextBeforeMark(9));
        assertNull(lyricsProcessor.getTextBeforeMark(10));
        assertEquals("十", lyricsProcessor.getTextBeforeMark(11));
        assertEquals("一", lyricsProcessor.getTextBeforeMark(12));
        assertEquals("二", lyricsProcessor.getTextBeforeMark(13));
        assertEquals("三", lyricsProcessor.getTextBeforeMark(14));
        assertEquals("四", lyricsProcessor.getTextBeforeMark(15));
        assertEquals("五", lyricsProcessor.getTextBeforeMark(16));
    }

    @Test
    void textBeforeMarkTestForEnglish1() {
        lyricsProcessor.setLyrics("""
                one
                two
                three
                four
                five""");

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("one", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("two", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("three", lyricsProcessor.getTextBeforeMark(3));
        assertEquals("four", lyricsProcessor.getTextBeforeMark(4));
        assertEquals("five", lyricsProcessor.getTextBeforeMark(5));
    }

    @Test
    void textBeforeMarkTestForEnglish2() {
        lyricsProcessor.setLyrics("""
                one two
                three
                
                four five
                six""");

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("one", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("two", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("three", lyricsProcessor.getTextBeforeMark(3));
        assertNull(lyricsProcessor.getTextBeforeMark(4));
        assertEquals("four", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("five", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("six", lyricsProcessor.getTextBeforeMark(7));
    }

    @Test
    void textBeforeMarkTestForEnglish3() {
        lyricsProcessor.setLyrics("""
                one two fun_ny
                ka_ra_o_ke three
                
                four hap_py five
                six""");

        assertNull(lyricsProcessor.getTextBeforeMark(0));
        assertEquals("one", lyricsProcessor.getTextBeforeMark(1));
        assertEquals("two", lyricsProcessor.getTextBeforeMark(2));
        assertEquals("fun", lyricsProcessor.getTextBeforeMark(3));
        assertEquals("_ny", lyricsProcessor.getTextBeforeMark(4));
        assertEquals("ka", lyricsProcessor.getTextBeforeMark(5));
        assertEquals("_ra", lyricsProcessor.getTextBeforeMark(6));
        assertEquals("_o", lyricsProcessor.getTextBeforeMark(7));
        assertEquals("_ke", lyricsProcessor.getTextBeforeMark(8));
        assertEquals("three", lyricsProcessor.getTextBeforeMark(9));
        assertNull(lyricsProcessor.getTextBeforeMark(10));
        assertEquals("four", lyricsProcessor.getTextBeforeMark(11));
        assertEquals("hap", lyricsProcessor.getTextBeforeMark(12));
        assertEquals("_py", lyricsProcessor.getTextBeforeMark(13));
        assertEquals("five", lyricsProcessor.getTextBeforeMark(14));
        assertEquals("six", lyricsProcessor.getTextBeforeMark(15));
    }

    @Test
    void displayingLinesTestForLyrics1() {
        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600, 700, 800));

        lyricsProcessor.setLyrics(lyricsSample1);
        saveLoadManager.setProp("dotsPeriod", 50);

        // We don't care about the values right before ready dots start, because they are not displayed

        lyricsProcessor.setTime(50); // before mark 0, after ready dots start
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(150); // mark 0 ~ 1
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(250); // mark 1 ~ 2
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(350); // mark 2 ~ 3
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());

        // skip mark 3 ~ 4 because we don't care the displaying lines of a blank line.

        lyricsProcessor.setTime(550); // mark 4 ~ 5
        assertArrayEquals(new int[]{3, 2}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(650); // mark 5 ~ 6
        assertArrayEquals(new int[]{3, 2}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(750); // mark 6 ~ 7
        assertArrayEquals(new int[]{3, 2}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(850); // after mark 7
        assertArrayEquals(new int[]{3, 2}, lyricsProcessor.getDisplayingLines());
    }

    @Test
    void displayingLinesTestForLyrics2() {
        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600, 700, 800));

        lyricsProcessor.setLyrics(lyricsSample2);
        saveLoadManager.setProp("dotsPeriod", 50);

        // We don't care about the values right before ready dots start, because they are not displayed

        lyricsProcessor.setTime(50); // before mark 0
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(150); // mark 0 ~ 1
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(250); // mark 1 ~ 2
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(350); // mark 2 ~ 3
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());

        // skip mark 3 ~ 4 because we don't care the displaying lines of a blank line.

        lyricsProcessor.setTime(550); // mark 4 ~ 5
        assertArrayEquals(new int[]{3, 4}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(650); // mark 5 ~ 6
        assertArrayEquals(new int[]{3, 4}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(750); // mark 6 ~ 7
        assertArrayEquals(new int[]{3, 4}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(850); // after mark 7
        assertArrayEquals(new int[]{3, 4}, lyricsProcessor.getDisplayingLines());
    }

    @Test
    void displayingLinesTestForLyrics3() {
        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(
                100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700
        ));

        lyricsProcessor.setLyrics(lyricsSample3);
        saveLoadManager.setProp("dotsPeriod", 50);

        // We don't care about the values right before ready dots start, because they are not displayed

        lyricsProcessor.setTime(50); // before mark 0
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(150); // mark 0 ~ 1
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(250); // mark 1 ~ 2
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(350); // mark 2 ~ 3
        assertArrayEquals(new int[]{0, 1}, lyricsProcessor.getDisplayingLines());

        lyricsProcessor.setTime(450); // mark 3 ~ 4
        assertArrayEquals(new int[]{2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(550); // mark 4 ~ 5
        assertArrayEquals(new int[]{2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(650); // mark 5 ~ 6
        assertArrayEquals(new int[]{2, 1}, lyricsProcessor.getDisplayingLines());

        lyricsProcessor.setTime(750); // mark 6 ~ 7
        assertArrayEquals(new int[]{2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(850); // mark 7 ~ 8
        assertArrayEquals(new int[]{2, 1}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(950); // mark 8 ~ 9
        assertArrayEquals(new int[]{2, 1}, lyricsProcessor.getDisplayingLines());

        // skip mark 9 ~ 10 because we don't care the displaying lines of a blank line.

        lyricsProcessor.setTime(1150); // mark 10 ~ 11
        assertArrayEquals(new int[]{4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1250); // mark 11 ~ 12
        assertArrayEquals(new int[]{4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1350); // mark 12 ~ 13
        assertArrayEquals(new int[]{4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1450); // mark 13 ~ 14
        assertArrayEquals(new int[]{4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1550); // mark 14 ~ 15
        assertArrayEquals(new int[]{4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1650); // mark 15 ~ 16
        assertArrayEquals(new int[]{4, 5}, lyricsProcessor.getDisplayingLines());
        lyricsProcessor.setTime(1750); // after mark 16
        assertArrayEquals(new int[]{4, 5}, lyricsProcessor.getDisplayingLines());
    }

    @Test
    void shouldDisplayTextTest() {
        lyricsProcessor.setLyrics("""
                abc
                def
                
                ghi
                jkl""");

        saveLoadManager.setProp("textDisappearTime", 50);
        saveLoadManager.setProp("dotsPeriod", 0);

        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600));

        lyricsProcessor.setTime(349);
        assertTrue(lyricsProcessor.shouldDisplayText());
        lyricsProcessor.setTime(351);
        assertFalse(lyricsProcessor.shouldDisplayText());
        lyricsProcessor.setTime(401);
        assertTrue(lyricsProcessor.shouldDisplayText());

        lyricsProcessor.setTime(599);
        assertTrue(lyricsProcessor.shouldDisplayText());
        lyricsProcessor.setTime(649);
        assertTrue(lyricsProcessor.shouldDisplayText());
        lyricsProcessor.setTime(651);
        assertFalse(lyricsProcessor.shouldDisplayText());
    }

    @Test
    void readyDotsPercentageUniversalTest() {
        lyricsProcessor.setLyrics("""
                abc
                def
                
                ghi
                jkl""");

        saveLoadManager.setProp("dotsPeriod", 50);

        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400));

        lyricsProcessor.setTime(0);
        assertTrue(lyricsProcessor.getReadyDotsPercentage() < 0);
        lyricsProcessor.setTime(60);
        assertEquals(0.2f, lyricsProcessor.getReadyDotsPercentage());
        lyricsProcessor.setTime(80);
        assertEquals(0.6f, lyricsProcessor.getReadyDotsPercentage());
        lyricsProcessor.setTime(99);
        assertEquals(0.98f, lyricsProcessor.getReadyDotsPercentage());
        lyricsProcessor.setTime(370);
        assertEquals(0.4f, lyricsProcessor.getReadyDotsPercentage());

        // Make sure percentage is 0 at the 2nd paragraph's end mark to its next.
        lyricsProcessor.setTime(660);
        assertEquals(0f, lyricsProcessor.getReadyDotsPercentage());
    }

    @Test
    void readyDotsPercentageAtFirstMarkTest() {
        lyricsProcessor.setLyrics("""
                abc
                def
                
                ghi
                jkl""");

        saveLoadManager.setProp("dotsPeriod", 50);

        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400));

        lyricsProcessor.setTime(100);
        assertEquals(0, lyricsProcessor.getReadyDotsPercentage());
        lyricsProcessor.setTime(200);
        assertEquals(0, lyricsProcessor.getReadyDotsPercentage());
    }

    @Test
    void isParagraphEndMarkTest() {
        lyricsProcessor.setLyrics("""
                abc
                def

                ghi
                jkl""");

        var marks = saveLoadManager.getMarks();
        marks.clear();
        marks.addAll(List.of(100, 200, 300, 400, 500, 600));
        assertFalse(lyricsProcessor.isParagraphEndMark(0));
        assertFalse(lyricsProcessor.isParagraphEndMark(1));
        assertTrue(lyricsProcessor.isParagraphEndMark(2));
        assertFalse(lyricsProcessor.isParagraphEndMark(3));
        assertFalse(lyricsProcessor.isParagraphEndMark(4));
        assertTrue(lyricsProcessor.isParagraphEndMark(5));
    }
}