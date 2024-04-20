package net.okt.system;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LyricsProcessor {
    private final List<String> markTextList = new ArrayList<>();
    private final Set<Integer> paragraphEndMarks = new HashSet<>();
    private String lyrics;

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public boolean isParagraphEnd(int markIdx) {
        return paragraphEndMarks.contains(markIdx);
    }

    public String getTextBeforeMark(int markIdx) {
        if (markIdx == 0) return null;
        else if (markIdx - 1 > markTextList.size()) return null;
        return markTextList.get(markIdx - 1);
    }

    /**
     * This method processes the set lyrics and updates the mark-text data to #markTextList according to the karaoke
     * lyrics rules below:
     *
     * <pre>
     * 1. In general, 2 marks hold a word.
     * 2. When meeting the symbol “'“, that means it is the link word case, take the chars right before and after “'”.
     * 3. When meeting a single “\n” symbol, that means it is the end of a line, we want to ignore it and skip to the
     * next line’s start word - the word after “\n”.
     * 4. When meeting double “\n”s, that means it is the end of a paragraph, also skip that, but make sure the new
     * paragraph’s start mark holds “null”, and so that we know it’s not connected to the last mark.
     *</pre>
     *
     * <p>
     * Then, store the texts corresponding to the marks in a list, where index 0 is the text held by mark0 and mark1,
     * index 1 is the text held by mark1 and mark2, and so on.
     * </p>
     *
     * Here’s some examples:
     *<pre>
     * 1)
     * Given lyrics: “ab\nc'd\n\nefg”
     * Marks distribution would be:
     *
     * m0 a m1 b m2 cd m3 m4 e m5 f m6 g m7
     *
     * The list would be:
     * {a, b, cd, null, e, f, g}
     *
     * 2)
     *
     * Given lyrics:  “a\nbc\n\nd\nef'g”
     *
     * Marks distribution would be:
     * m0 a m1 b m2 c m3 m4 d m5 e m6 fg m7
     *
     * The list would be:
     * {a, b, c, null, d, e, fg}
     * <pre>
     */
    public void genTextToMarksList() {
        markTextList.clear();
        int charIdx = 0;
        for (int markIdx = 1; charIdx < lyrics.length(); markIdx++) {
            if (charIdx + 1 >= lyrics.length()) {
                markTextList.add(String.valueOf(lyrics.charAt(charIdx)));
                break;
            }

            char currentChar = lyrics.charAt(charIdx);
            char nextChar = lyrics.charAt(charIdx + 1);
            int correctCharIdx;

            if (currentChar == '\n') {
                if (nextChar == '\n') {
                    correctCharIdx = -1;
                    paragraphEndMarks.add(markIdx - 1);
                } else {
                    correctCharIdx = charIdx + 1;
                }

                charIdx += 2;
            } else {
                correctCharIdx = charIdx;
                charIdx++;
            }

            String textBeforeMark;
            if (lyrics.charAt(correctCharIdx + 1) == '\'') {
                char firstChar = lyrics.charAt(correctCharIdx);
                char secondChar = lyrics.charAt(correctCharIdx + 2);
                textBeforeMark = String.valueOf(new char[]{firstChar, secondChar});

                charIdx += 2;
            } else {
                textBeforeMark = correctCharIdx == -1 ? null : String.valueOf(lyrics.charAt(correctCharIdx));
            }

            markTextList.add(textBeforeMark);
        }
    }
}
