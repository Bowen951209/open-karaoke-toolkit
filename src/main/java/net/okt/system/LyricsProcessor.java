package net.okt.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LyricsProcessor {
    private final SaveLoadManager saveLoadManager;
    private final List<Integer> marks;
    /**
     * The texts before each mark.
     */
    private final List<String> markTextList = new ArrayList<>();
    /**
     * The marks that are the end of a paragraph.
     */
    private final List<Integer> paragraphEndMarks = new ArrayList<>();
    /**
     * The marks that are the start of a line.
     */
    private final List<Integer> lineStartMarks = new ArrayList<>();
    /**
     * The lines that should be currently displayed.
     */
    private final int[] displayingLines = new int[2];

    private float readyDotsPercentage;
    private boolean shouldDisplayText;
    private String lyrics;
    private List<String> lyricsLines;

    public LyricsProcessor(SaveLoadManager saveLoadManager) {
        this.saveLoadManager = saveLoadManager;
        this.marks = saveLoadManager.getMarks();
    }

    public static boolean isEasternChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub == Character.UnicodeBlock.HIRAGANA ||
                ub == Character.UnicodeBlock.KATAKANA ||
                ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS ||
                ub == Character.UnicodeBlock.HANGUL_SYLLABLES ||
                ub == Character.UnicodeBlock.HANGUL_JAMO ||
                ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO;
    }

    public int getStartMarkAtLine(int line) {
        if (line < 0) return 0;
        int index = line - 1 - getParagraphAtLine(line);
        if (index < 0) return 0;
        return lineStartMarks.get(index);
    }

    public List<String> getLyricsLines() {
        return lyricsLines;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
        lyricsLines = Arrays.asList(lyrics.split("\n"));

        updateMarkLists();
    }

    public String getTextBeforeMark(int markIdx) {
        if (markIdx == 0 || markTextList.isEmpty()) return null;
        return markTextList.get(markIdx - 1);
    }

    /**
     * Get the lines that should be displayed at the set time.
     *
     * @see #setTime(int)
     */
    public int[] getDisplayingLines() {
        return displayingLines;
    }

    /**
     * Get if to display lyrics at the set time.
     *
     * @see #setTime(int)
     */
    public boolean shouldDisplayText() {
        return shouldDisplayText;
    }

    /**
     * Get the progress of the ready dots at the set time.
     *
     * @return A percentage value in float.
     * @see #setTime(int)
     */
    public float getReadyDotsPercentage() {
        return readyDotsPercentage;
    }

    public boolean isMaxMarkNumber() {
        return marks.size() == markTextList.size() + 1;
    }

    /**
     * Get the redundant marks number. The max mark number should be decided by the {@link #lyrics}. Any mark more than
     * that is considered a redundant mark.
     */
    public int getRedundantMarkNumber() {
        return marks.size() - markTextList.size() - 1;
    }

    /**
     * @return If mark is the end of a paragraph.
     */
    public boolean isParagraphEndMark(int mark) {
        return paragraphEndMarks.contains(mark);
    }

    /**
     * Change the values that is bond with time position.
     * <p></p>
     * <p>
     * 1. Calculate which 2 lines should be displayed at the given time and update to {@link #displayingLines}.
     * With the rules below:
     * (* means the line is currently being sung)
     * <pre>
     *     *01 -> 2*1 -> *23 -> 4*3 -> ...
     * </pre>
     * After a line is finished, skip to the next 2 line.
     * <p></p>
     * <p>When meeting the end of a paragraph, if next line is blank, don't let it only display single line:</p>
     * <pre>
     *     *01 -> 2*1 -> *21(paragraphEnd) -> *45(paragraphStart) -> 6*5 -> ...
     * </pre>
     * <p></p>
     * <p>
     * 2. Calculate if the lyrics should be displayed at the given time and update to {@link #shouldDisplayText}.
     * Lyrics should only not be displayed between different paragraphs(after disappear time and before ready dots time).
     * <p></p>
     * <p>
     * 3. Calculate the progress of the ready dots at the given time and update to {@link #readyDotsPercentage}.
     *
     * @param time The time the player is currently at.
     */
    public void setTime(int time) {
        if (marks.isEmpty()) {
            shouldDisplayText = false;
            readyDotsPercentage = 0;
            return;
        }

        int readyDotsPeriod = saveLoadManager.getPropInt("dotsPeriod");
        int textDisappearTime = saveLoadManager.getPropInt("textDisappearTime");

        // Find the paragraph it's at and find the start line of it.
        int paragraph = getParagraphAtTime(time, readyDotsPeriod);
        int paragraphStartLine = getParagraphStartLine(paragraph);

        // Find the line it's at.
        int line = getLine(time, paragraph, readyDotsPeriod);
        int nextLine = line + 1;

        // Set the displayingLines.
        // If nextLine is blank, switch it back because we don't want to display blank lines.
        if ((line - paragraphStartLine) % 2 == 0) {
            displayingLines[0] = line;
            if (isBlankLine(nextLine))
                displayingLines[1] = line - 1;
            else
                displayingLines[1] = nextLine;
        } else {
            displayingLines[1] = line;
            if (isBlankLine(nextLine))
                displayingLines[0] = line - 1;
            else
                displayingLines[0] = nextLine;
        }

        int nextMark = getNextMark(time);
        int lastMark = Math.max(nextMark - 1, 0); // If nextMark is 0, lastMark will be -1, just take 0 for the case.

        // Decide if to display text and the percentage of ready dots.
        if (isParagraphEndMark(lastMark) || time < marks.get(0)) {
            int disappearStart = marks.get(lastMark) + textDisappearTime;
            int disappearEnd = nextMark >= marks.size() ? Integer.MAX_VALUE : marks.get(nextMark) - readyDotsPeriod;
            boolean shouldDisappear = time > disappearStart && time < disappearEnd;

            shouldDisplayText = !shouldDisappear;

            readyDotsPercentage = (float) (time - disappearEnd) / readyDotsPeriod;
        } else {
            shouldDisplayText = true;
            readyDotsPercentage = 0;
        }
    }

    // TODO: Update the docs with English/Chinese differences.

    /**
     * Process the set lyrics and updates 3 lists:
     * <pre>
     * 1. {@link #paragraphEndMarks}
     * 2. {@link #lineStartMarks}
     * 3. {@link #markTextList}
     * </pre>
     * The {@link #markTextList} is calculated based on the rules below:
     *
     * <pre>
     * 1. In general, 2 marks hold a word.
     * 2. When meeting the symbol “'“, it is the link word case, take the chars right before and after “'”.
     * 3. When meeting a single “\n” symbol, it is the end of a line, ignore it and skip to the next line’s start word
     *    (the word after “\n”).
     * 4. When meeting double “\n”s, it is the end of a paragraph, also skip that, but make sure the new
     *    paragraph’s start mark holds “null”, so that we know it’s not connected to the last mark.
     * </pre>
     *
     * <p>
     * Then, store the texts corresponding to the marks in a list, where index 0 is the text held by mark0 and mark1,
     * index 1 is the text held by mark1 and mark2, and so on.
     * </p>
     * <p>
     * Here’s some examples:
     * <pre>
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
    private void updateMarkLists() {
        markTextList.clear();
        paragraphEndMarks.clear();
        lineStartMarks.clear();

        int charIdx = 0;
        for (int markIdx = 1; charIdx <= lyrics.length(); markIdx++) {
            if (charIdx + 1 >= lyrics.length()) {
                // If meet the end of the lyrics, add the last mark as the end of the last paragraph.
                // And if there's still a char left, add it as the text before the last mark.
                // p.s. Chinese and English characters will end up in below cases respectively.
                if (charIdx == lyrics.length() - 1) {
                    paragraphEndMarks.add(markIdx);
                    markTextList.add(String.valueOf(lyrics.charAt(charIdx)));
                } else paragraphEndMarks.add(markIdx - 1);

                break;
            }

            char currentChar = lyrics.charAt(charIdx);
            char nextChar = lyrics.charAt(charIdx + 1);
            int correctCharIdx;

            if (currentChar == '\n') {
                if (nextChar == '\n') {
                    // It's the end of a paragraph.
                    paragraphEndMarks.add(markIdx - 1);
                    lineStartMarks.add(markIdx);
                    markTextList.add(null);// no correct char for this mark.

                    charIdx += 2;
                    continue;
                } else {
                    // It's the end of a line.
                    correctCharIdx = charIdx + 1;
                    lineStartMarks.add(markIdx - 1);
                }

                charIdx += 2;
            } else {
                correctCharIdx = charIdx;
                charIdx++;
            }

            // The text before the mark.
            char correctChar = lyrics.charAt(correctCharIdx);
            String textBeforeMark;

            // Eastern chars and western chars are handled differently.
            if (isEasternChar(correctChar)) {
                // If the next char is ', handle the link word case.
                if (correctCharIdx + 1 < lyrics.length() && lyrics.charAt(correctCharIdx + 1) == '\'') {
                    char secondChar = lyrics.charAt(correctCharIdx + 2);
                    textBeforeMark = String.valueOf(new char[]{correctChar, secondChar});

                    charIdx += 2;
                } else textBeforeMark = String.valueOf(correctChar); // normal case.
            } else { // western chars.
                int nextSpaceIdx = getNextSpaceIdx(charIdx);
                int nextLineBreakIdx = getNextLineBreakIdx(charIdx);
                int wordEndIdx = getWordEndIdx(nextSpaceIdx, nextLineBreakIdx);

                textBeforeMark = lyrics.substring(correctCharIdx, wordEndIdx);
                charIdx = wordEndIdx;

                // If the word ends with a space, skip the space for next word.
                if (wordEndIdx == nextSpaceIdx) charIdx++;
            }

            markTextList.add(textBeforeMark);
        }
    }

    private int getWordEndIdx(int nextSpaceIdx, int nextLineBreakIdx) {
        int wordEndIdx;

        if (nextSpaceIdx == -1 && nextLineBreakIdx == -1) {
            // If there's no space or line break beyond, the word should end at the end of the lyrics.
            wordEndIdx = lyrics.length();
        } else if (nextSpaceIdx == -1) {
            // if there's no space beyond, cut word at next line break.
            wordEndIdx = nextLineBreakIdx;
        } else if (nextLineBreakIdx == -1) {
            // if there's no line break beyond, cut word at next space.
            wordEndIdx = nextSpaceIdx;
        } else {
            // Cut word at the nearest space or line break.
            wordEndIdx = Math.min(nextSpaceIdx, nextLineBreakIdx);
        }
        return wordEndIdx;
    }

    /**
     * @return If the mark is the start mark of a paragraph.
     */
    private boolean isParagraphStartMark(int mark) {
        if (mark >= marks.size()) return false;
        return mark == 0 || paragraphEndMarks.contains(mark - 1);
    }

    /***
     * @return The mark that is after and nearest to the given time.
     */
    private int getNextMark(int time) {
        return Math.abs(Collections.binarySearch(marks, time)) - 1;
    }

    private int getNextSpaceIdx(int fromIdx) {
        return lyrics.indexOf(' ', fromIdx);
    }

    private int getNextLineBreakIdx(int fromIdx) {
        return lyrics.indexOf('\n', fromIdx);
    }

    /**
     * @return The paragraph it is at the given time.
     */
    private int getParagraphAtTime(int time, int readyDotsPeriod) {
        int index = Collections.binarySearch(paragraphEndMarks, time, (paragraphEndMark, t) -> {
            int nextMark = paragraphEndMark + 1;
            if (nextMark >= marks.size()) return 1;
            Integer paragraphStartTime = marks.get(nextMark) - readyDotsPeriod;
            return paragraphStartTime.compareTo(t);
        });

        return Math.abs(index) - 1;
    }

    /**
     * @return The paragraph it is at the given line.
     */
    private int getParagraphAtLine(int line) {
        return Collections.frequency(lyricsLines.subList(0, line), "");
    }

    private int getParagraphStartLine(int paragraph) {
        if (paragraph == 0) return 0;
        int paragraphStartMark = paragraphEndMarks.get(paragraph - 1) + 1;
        // +1 because lineStartMarks doesn't include mark 0.
        return lineStartMarks.indexOf(paragraphStartMark) + paragraph + 1;
    }

    /**
     * @return The line that should be sung at the given time.
     */
    private int getLine(int time, int paragraph, int readyDotsPeriod) {
        int index = Math.abs(Collections.binarySearch(lineStartMarks, time, (lineStartMark, t) -> {
            if (lineStartMark >= marks.size()) return 1;

            int lineStartTime = Math.toIntExact(marks.get(lineStartMark));
            if (isParagraphStartMark(lineStartMark)) {
                return Integer.compare(lineStartTime - readyDotsPeriod, t);
            } else {
                return Integer.compare(lineStartTime, t);
            }
        }));

        return index - 1 + paragraph;
    }

    /**
     * @return If the given line is a blank line. (paragraph gap)
     */
    private boolean isBlankLine(int line) {
        if (line >= lyricsLines.size()) return true;
        return lyricsLines.get(line).isEmpty();
    }
}
