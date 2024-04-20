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

    // TODO: write a test

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
