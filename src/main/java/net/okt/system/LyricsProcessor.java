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
     * (Doesn't include mark0.)
     */
    private final List<Integer> lineStartMarks = new ArrayList<>();
    /**
     * The lines that should be displayed at the set time.
     *
     * @see #setTime(int)
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

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
        lyricsLines = Arrays.asList(lyrics.split("\n"));

        updateMarkLists();
    }

    /**
     * @return The text before the given mark. If the lyrics is empty or there's no word before the mark(a paragraph
     * start mark or redundant mark), return null.
     */
    public String getTextBeforeMark(int markIdx) {
        if (markIdx == 0 || markTextList.isEmpty() || markIdx >= getMaxMarkNumber())
            return null;

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

    /**
     * @return The max number of mark needed.
     */
    public int getMaxMarkNumber() {
        // (markTextList's size + 1) is the maximum size for marks.
        return markTextList.size() + 1;
    }

    /**
     * @return If mark is the end of a paragraph.
     */
    public boolean isParagraphEndMark(int mark) {
        return paragraphEndMarks.contains(mark);
    }

    public boolean isRedundantMark(int mark) {
        // Make sure there is redundant mark first.
        return marks.size() > getMaxMarkNumber() && mark >= getMaxMarkNumber();
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
        if (marks.isEmpty() || lyrics == null) {
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
        int lastMark = nextMark - 1; // If nextMark is 0, lastMark will be -1.

        // TODO: Make users can put marks in the middle.

        // If last mark is a redundant mark, process it as the end of paragraph end marks. By doing so, the following
        // logic can work properly.
        if (isRedundantMark(lastMark))
            lastMark = paragraphEndMarks.get(paragraphEndMarks.size() - 1);

        // Decide if to display text and the percentage of ready dots.
        if (isParagraphEndMark(lastMark) || nextMark == 0) {
            // Calculate the start and end the text should disappear.
            boolean isEndMark = nextMark >= getMaxMarkNumber(); // If it's the end of the last paragraph.
            int disappearStart = nextMark == 0 ? 0 : marks.get(lastMark) + textDisappearTime;
            int disappearEnd = isEndMark ? Integer.MAX_VALUE : marks.get(nextMark) - readyDotsPeriod;
            boolean shouldDisappear = time >= disappearStart && time <= disappearEnd;
            shouldDisplayText = !shouldDisappear;

            // Only if next mark is not the very end mark should we calculate the readyDotsPercentage, as ready dots
            // should not display at the very end paragraph.
            readyDotsPercentage = isEndMark ? 0 : (float) (time - disappearEnd) / readyDotsPeriod;
        } else {
            shouldDisplayText = true;
            readyDotsPercentage = 0;
        }
    }

    /**
     * Processes the set lyrics and updates 3 lists:
     * <ul>
     *   <li><code>paragraphEndMarks</code></li>
     *   <li><code>lineStartMarks</code></li>
     *   <li><code>markTextList</code></li>
     * </ul>
     *
     * <p>The <code>markTextList</code> is calculated based on its language (Western/Eastern). The rules below:</p>
     *
     * <h2>For Eastern (Chinese):</h2>
     * <ol>
     *   <li>In general, 2 marks hold a single character.</li>
     *   <li>When meeting the symbol <code>‘'’</code>, it is a link word, take the characters right before and after <code>‘'’</code>.
     *       So the characters "一'二" will be "一二".</li>
     * </ol>
     *
     * <h2>For Western (English):</h2>
     * <ol>
     *   <li>2 marks hold a word. A word is separated by spaces. For example, the string "one two three" will be divided into the
     *       set {"one", "two", "three"}.</li>
     *   <li>When meeting the symbol <code>‘_’</code>, it is a separated word, separate the word into more parts. For example, the word
     *       "ma_ni_pu_la_tion" should be divided into the set {"ma", "_ni", "_pu", "_la", "_tion"}.</li>
     * </ol>
     *
     * <h2>Common Rules:</h2>
     * <ol>
     *   <li>When meeting a single <code>‘\n’</code> symbol, it is the end of a line, ignore it and skip to the next line’s start word
     *       (the word after <code>‘\n’</code>).</li>
     *   <li>When meeting double <code>‘\n’</code> symbols, it is the end of a paragraph, also skip that, but make sure the new
     *       paragraph’s start mark holds <code>null</code>, so that we know it’s not connected to the last mark.</li>
     * </ol>
     *
     * <p>Then, store the texts corresponding to the marks in a list, where index 0 is the text held by mark0 and mark1, index 1 is the text held by mark1 and mark2, and so on.</p>
     *
     * <h2>Examples:</h2>
     * <ol>
     *   <li>
     *     <p>Given lyrics: “一二\n三'四\n\n五六七”</p>
     *     <p>Marks distribution would be:</p>
     *     <pre>m0 一 m1 二 m2 三四 m3 m4 五 m5 六 m6 七 m7</pre>
     *     <p>The list would be:</p>
     *     <pre>{一, 二, 三四, null, 五, 六, 七}</pre>
     *   </li>
     *   <li>
     *     <p>Given lyrics: “one\ntwo\n\nthree\nfour five”</p>
     *     <p>Marks distribution would be:</p>
     *     <pre>m0 one m1 two m2 m3 three m4 four m5 five m6</pre>
     *     <p>The list would be:</p>
     *     <pre>{one, two, null, three, four, five}</pre>
     *   </li>
     * </ol>
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
                // If the next char is ' and the 2nd char is accessible, handle the link word case.
                int secondCharIdx = correctCharIdx + 2;
                if (secondCharIdx < lyrics.length() && lyrics.charAt(correctCharIdx + 1) == '\'') {
                    char secondChar = lyrics.charAt(secondCharIdx);
                    textBeforeMark = String.valueOf(new char[]{correctChar, secondChar});

                    charIdx += 2;
                } else textBeforeMark = String.valueOf(correctChar); // normal case.
            } else { // western chars.
                int nextSpaceIdx = lyrics.indexOf(' ', charIdx);
                int nextLineBreakIdx = lyrics.indexOf('\n', charIdx);
                int nextUnderscoreIdx = lyrics.indexOf('_', charIdx);
                int wordEndIdx = getWordEndIdx(nextSpaceIdx, nextLineBreakIdx, nextUnderscoreIdx);

                textBeforeMark = lyrics.substring(correctCharIdx, wordEndIdx);
                charIdx = wordEndIdx;

                // If the word ends with a space, skip the space for next word.
                if (wordEndIdx == nextSpaceIdx) charIdx++;
            }

            markTextList.add(textBeforeMark);
        }
    }

    /**
     * @return The index which the word should end at.
     */
    private int getWordEndIdx(int nextSpaceIdx, int nextLineBreakIdx, int nextUnderscoreIdx) {
        int wordEndIdx = lyrics.length();

        if (nextSpaceIdx != -1)
            wordEndIdx = nextSpaceIdx;
        if (nextLineBreakIdx != -1)
            wordEndIdx = Math.min(wordEndIdx, nextLineBreakIdx);
        if (nextUnderscoreIdx != -1)
            wordEndIdx = Math.min(wordEndIdx, nextUnderscoreIdx);

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

    /**
     * @return The paragraph it is at the given time.
     */
    private int getParagraphAtTime(int time, int readyDotsPeriod) {
        int index = Collections.binarySearch(paragraphEndMarks, time, (paragraphEndMark, t) -> {
            int nextMark = paragraphEndMark + 1;
            if (nextMark >= getMaxMarkNumber()) return 1;
            int paragraphStartTime = marks.get(nextMark) - readyDotsPeriod;
            return Integer.compare(paragraphStartTime, t);
        });

        return Math.max(0, Math.abs(index) - 1);
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

            int lineStartTime = marks.get(lineStartMark);
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
