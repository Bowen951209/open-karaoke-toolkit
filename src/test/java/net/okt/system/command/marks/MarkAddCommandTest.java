package net.okt.system.command.marks;

import net.okt.Main;
import net.okt.system.LyricsProcessor;
import net.okt.system.SaveLoadManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarkAddCommandTest {
    Main mainFrame = new Main("MarkAddCmd Test", null);
    SaveLoadManager saveLoadManager = new SaveLoadManager(mainFrame);
    LyricsProcessor lyricsProcessor = new LyricsProcessor(saveLoadManager);

    /**
     * Make sure no exception will be thrown when adding the first mark.
     */
    @Test
    void addFirstMarkTest() {
        var markList = saveLoadManager.getMarks();
        assertDoesNotThrow(() -> new MarkAddCommand(markList, 10).execute());
    }

    /**
     * Make sure no exception will be thrown when adding the end mark of the first paragraph.
     */
    @Test
    void addFirstParagraphEndMarkTest() {
        lyricsProcessor.setLyrics("""
                aaa bbb ccc
                ddd eee fff
                hhh iii jjj
                
                kkk lll mmm
                nnn ooo ppp
                """);

        var markList = saveLoadManager.getMarks();
        assertTrue(lyricsProcessor.isParagraphEndMark(9));

        for (int i = 0; i < 9; i++) {
            int time = i * 10;
            assertDoesNotThrow(() -> new MarkAddCommand(markList, time).execute());
        }
    }
}