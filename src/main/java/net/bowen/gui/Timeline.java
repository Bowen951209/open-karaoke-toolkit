package net.bowen.gui;

import net.bowen.audioUtils.Audio;
import net.bowen.system.SaveLoadManager;
import net.bowen.system.command.CommandManager;
import net.bowen.system.command.marks.MarkAddCommand;
import net.bowen.system.command.marks.MarkPopQuantityCommand;
import net.bowen.system.command.marks.MarkRemoveCommand;
import net.bowen.system.command.marks.MarkSetCommand;

import javax.swing.*;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Objects;

public class Timeline extends JPanel {
    private static final ImageIcon PLAY_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/play.png")));
    private static final ImageIcon PAUSE_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/pause.png")));
    private static final ImageIcon STOP_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/stop.png")));
    private static final ImageIcon MARK_NORM_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_norm.png")));
    private static final ImageIcon MARK_END_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_end.png")));
    private static final ImageIcon MARK_SELECTED_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_selected.png")));
    private static final ImageIcon MARK_FLOAT_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_float.png")));
    private static final Dimension ICON_SIZE = new Dimension(PLAY_BUTTON_ICON.getIconHeight(), PLAY_BUTTON_ICON.getIconWidth());
    /**
     * The width between separation lines in pixel.
     */
    private static final int SEP_LINE_INTERVAL = 30;
    /**
     * The time interval between separation lines in milliseconds.
     */
    private static final int SEP_LINE_INTERVAL_MS = 1000;
    public static final float PIXEL_TIME_RATIO = (float) SEP_LINE_INTERVAL / (float) SEP_LINE_INTERVAL_MS;
    /**
     * The delay time of {@link Timeline#timer}
     */
    private static final int TIMER_DELAY = 10;
    public static final int SLIDER_MAX_VAL = 500;

    private final CommandManager markCmdMgr = new CommandManager(15);
    private final Canvas canvas;
    private final ControlPanel controlPanel;
    private final SaveLoadManager saveLoadManager;
    private final Timer timer;
    private final Viewport viewport;

    private BufferedImage waveImg;
    private JScrollPane scrollPane;

    private boolean isPlaying;
    private int pointerX;

    public Timeline(SaveLoadManager saveLoadManager, Viewport viewport) {
        super();
        this.saveLoadManager = saveLoadManager;
        this.viewport = viewport;
        this.canvas = new Canvas();
        this.controlPanel = new ControlPanel();
        this.timer = new Timer(TIMER_DELAY, (e) -> {
            pointerX = toX(saveLoadManager.getLoadedAudio().getTimePosition());

            JScrollBar scrollBar = getCanvasScrollPane().getHorizontalScrollBar();
            int scrollX = scrollBar.getValue();
            int distance = getWidth() - pointerX + scrollX;

            // if less than 50 pixels from the end border || if we are viewing the further timeline and the pointer is not in view
            if (distance < 50 || distance > getWidth())
                scrollBar.setValue(pointerX - getWidth() + 50); // set to 50 pixels from the end border

            canvas.repaint();

            viewport.repaint();
        });

        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(controlPanel);
        add(getCanvasScrollPane());
    }

    public void setWaveImg(BufferedImage waveImg) {
        this.waveImg = waveImg;
    }

    public void setDisplayFileName(String name) {
        Audio audio = saveLoadManager.getLoadedAudio();
        String totalTime = toMinutesAndSecond((int) audio.getTotalTime(), 0);
        name += "(" + totalTime + ")";
        controlPanel.displayFileName = name;

        controlPanel.textPanel.setPreferredSize(new Dimension(6 * name.length(), ICON_SIZE.height));
        controlPanel.revalidate();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public JScrollPane getCanvasScrollPane() {
        if (scrollPane == null) { // if scrollPane == null, init it.
            scrollPane = new JScrollPane(canvas);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.setMinimumSize(new Dimension(0, 100));
            scrollPane.getHorizontalScrollBar().setUnitIncrement(15);

            scrollPane.setFocusable(true);
            scrollPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollPane.requestFocus();
                    int x = (e.getX() + scrollPane.getHorizontalScrollBar().getValue());
                    int ms = toTime(x);

                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1 -> {
                            // If you left-click, Jump the time.
                            saveLoadManager.getLoadedAudio().setTimeTo(ms);
                            pointerX = toX(saveLoadManager.getLoadedAudio().getTimePosition());
                        }

                        case MouseEvent.BUTTON2 -> {
                            // If you middle-click, delete selected mark.
                            if (canvas.selectedMark != -1) {
                                java.util.List<Long> marks = saveLoadManager.getMarks();
                                markCmdMgr.execute(new MarkRemoveCommand(marks, canvas.selectedMark));
                            }
                        }
                    }

                    canvas.repaint();
                    viewport.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    canvas.isMouseDragging = false;
                    if (canvas.draggingMark != -1) {
                        int x = (e.getX() + scrollPane.getHorizontalScrollBar().getValue());
                        markCmdMgr.execute(new MarkSetCommand(saveLoadManager.getMarks(), canvas.draggingMark, toTime(x)));
                        canvas.draggingMark = -1;
                        canvas.repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    controlPanel.timeLabel.setText("");
                }
            });
            scrollPane.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    canvas.repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    canvas.isMouseDragging = true;
                    int time = toTime(e.getX() + scrollPane.getHorizontalScrollBar().getValue());
                    controlPanel.timeLabel.setText(toMinutesAndSecond(time, 2));

                    canvas.repaint();
                }
            });
            scrollPane.addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    scrollPane.setWheelScrollingEnabled(true);

                    if (e.isControlDown()) {
                        scrollPane.setWheelScrollingEnabled(false);

                        JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
                        controlPanel.sliderScale(-e.getWheelRotation(), e.getX() + scrollBar.getValue());
                    }
                }
            });

            scrollPane.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    scrollPane.setBorder(BorderFactory.createLineBorder(Color.GREEN));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                }
            });

            // Disable the IME of the pane. This way, the key event can be handled properly.
            scrollPane.enableInputMethods(false);

            scrollPane.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    // Space: play/pause
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) controlPanel.playPauseButton.doClick();
                    else if (e.isControlDown()) {
                        // Ctrl + Z: Undo
                        if (e.getKeyCode() == KeyEvent.VK_Z) {
                            markUndo();
                        }
                        // Ctrl + Y: Redo
                        else if (e.getKeyCode() == KeyEvent.VK_Y) {
                            markRedo();
                        }
                    }
                }
            });
        }
        return scrollPane;
    }

    private int toX(long time) {
        return (int) (time * PIXEL_TIME_RATIO * canvas.scale);
    }

    private int toTime(int x) {
        return (int) ((float) x / (PIXEL_TIME_RATIO * canvas.scale));
    }

    private void timePlay() {
        timer.start();
        controlPanel.playPauseButton.setIcon(PAUSE_BUTTON_ICON);
        saveLoadManager.getLoadedAudio().play();
    }

    private void timePause() {
        timer.stop();
        controlPanel.playPauseButton.setIcon(PLAY_BUTTON_ICON);
        saveLoadManager.getLoadedAudio().pause();
    }

    public void timeStop() {
        isPlaying = false;
        timePause();
        getCanvasScrollPane().getHorizontalScrollBar().setValue(0);
        saveLoadManager.getLoadedAudio().setTimeTo(0);

        pointerX = 0;
        canvas.repaint();
    }

    public void markUndo() {
        markCmdMgr.undo();
        canvas.repaint();
    }

    public void markRedo() {
        markCmdMgr.redo();
        canvas.repaint();
    }

    /**
     * @param d Number of digits after decimal point.
     */
    private static String toMinutesAndSecond(int millis, int d) {
        float seconds = millis * .001f;
        int minutes = (int) (seconds / 60);
        seconds -= minutes * 60;
        String format = d == 0 ? "%02.0f" : "%02." + d + "f";

        return minutes + ":" + String.format(format, seconds);
    }

    private class ControlPanel extends JPanel {
        private final JButton playPauseButton = new JButton(PLAY_BUTTON_ICON);
        private final JPanel textPanel;
        private final JSlider slider = getSlider();
        private final JLabel timeLabel = new JLabel();

        private String displayFileName = "";

        public ControlPanel() {
            super(new BorderLayout(0, 0));
            Dimension size = new Dimension(Integer.MAX_VALUE, ICON_SIZE.height);
            setMaximumSize(size);

            JPanel componentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

            // I need a separate panel to stand on east to display playing audio.
            // Simply adding a JLabel would have border problems, and thus the font size should be very tiny.
            // I got a solution by drawing the string on ourselves, and it can be full space.
            textPanel = new JPanel() {
                @Override
                public void paint(Graphics g) {
                    super.paint(g); // This is necessary.(To clear)

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.drawString(displayFileName, 0, 10);
                }
            };

            playPauseButton.addActionListener(e -> {
                scrollPane.requestFocus(); // we want to keep the timeline focused.

                isPlaying = !isPlaying;
                if (isPlaying)
                    timePlay();
                else
                    timePause();
            });
            playPauseButton.setPreferredSize(ICON_SIZE);

            componentsPanel.add(playPauseButton);
            componentsPanel.add(getStopButton());
            componentsPanel.add(getMarkButton());
            componentsPanel.add(slider);
            componentsPanel.add(timeLabel);
            add(componentsPanel, BorderLayout.WEST);
            add(textPanel, BorderLayout.EAST);
        }

        private JButton getStopButton() {
            JButton stopButton = new JButton(STOP_BUTTON_ICON);
            stopButton.addActionListener(e -> {
                scrollPane.requestFocus(); // we want to keep the timeline focused.

                timeStop();
            });
            stopButton.setPreferredSize(ICON_SIZE);
            return stopButton;
        }

        private JButton getMarkButton() {
            JButton btn = new JButton(MARK_NORM_BUTTON_ICON);
            btn.addActionListener(e -> {
                scrollPane.requestFocus(); // we want to keep the timeline focused.

                // don't know why when getting the audio play time would have precise error,
                // so use another method to replace.
                // long time = saveLoadManager.getLoadedAudio().getTimePosition();
                ArrayList<Long> marks = saveLoadManager.getMarks();
                long pointerTime = toTime(pointerX);
                long lastMarkTime = marks.size() - 1 > 0 ? marks.get(marks.size() - 1) : 0;
                if (lastMarkTime < pointerTime) // It is only available to put a mark after the last one.
                    markCmdMgr.execute(new MarkAddCommand(marks, pointerTime));
            });
            btn.setPreferredSize(ICON_SIZE);

            return btn;
        }

        private JSlider getSlider() {
            JSlider slider = new JSlider(100, SLIDER_MAX_VAL, 100);
            slider.setPreferredSize(new Dimension(100, ICON_SIZE.height));
            SliderUI sliderUI = new BasicSliderUI() {
                @Override
                public void paintThumb(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(thumbRect.x, thumbRect.y + 4, 5, 10);
                }
            };
            slider.setUI(sliderUI);

            slider.addChangeListener(e -> canvas.setSize());

            slider.addMouseWheelListener(e -> sliderScale(e.getWheelRotation()));
            slider.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int valRange = slider.getMaximum() - slider.getMinimum();
                    int val = (int) ((float) e.getX() / slider.getWidth() * valRange + slider.getMinimum());
                    slider.setValue(val);
                }
            });

            return slider;
        }

        private void sliderScale(int orientation) {
            int sliderSpeed = 10;
            slider.setValue(slider.getValue() + orientation * sliderSpeed);
        }

        /**
         * This method will ensure that we are focus on the time of the original specified x pos.
         * @param x x position of the whole length of canvas.
         * */
        private void sliderScale(int orientation, int x) {
            int time = toTime(x);
            sliderScale(orientation);
            int newX = toX(time);

            JScrollBar scrollBar = getCanvasScrollPane().getHorizontalScrollBar();
            scrollBar.setValue(scrollBar.getValue() + (newX - x));
        }
    }

    public class Canvas extends JPanel {
        private float scale = 1;
        private int selectedMark = -1;
        private int draggingMark = -1;
        private boolean isMouseDragging;

        public Canvas() {
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    setSize();
                    Timeline.this.revalidate();
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight()); // background color

            g2d.drawImage(waveImg, 0, 0, getWidth(), getHeight(), null);
            drawSeparationLines(g2d);

            // Draw the marks
            drawMarks(g2d);

            // The cursor pointer & update label
            Point mousePosition = getMousePosition();
            if (mousePosition != null) {
                int time = toTime(mousePosition.x);
                controlPanel.timeLabel.setText(toMinutesAndSecond(time, 2));

                drawPointer(g2d, Color.DARK_GRAY, mousePosition.x);
            }

            // The current playing time pointer
            drawPointer(g2d, Color.RED, pointerX);
        }

        private void drawSeparationLines(Graphics2D g2d) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

            int pointingPixel = 0; // the pixel we are current at
            int millisecond = 0; // the time we are current at in millisecond.
            while (pointingPixel < getWidth()) {
                pointingPixel += (int) (SEP_LINE_INTERVAL * scale);
                millisecond += SEP_LINE_INTERVAL_MS;

                g2d.setColor(Color.GRAY);
                g2d.drawLine(pointingPixel, 0, pointingPixel, getHeight());
                g2d.setColor(Color.BLACK);
                g2d.drawString(toMinutesAndSecond(millisecond, 0), pointingPixel, 10);
            }
        }

        private void drawPointer(Graphics2D g2d, Color color, int x) {
            g2d.setColor(color);
            g2d.drawLine(x, 0, x, getHeight());
        }

        private void drawMarks(Graphics2D g2d) {
            g2d.setColor(Color.YELLOW);
            var marks = saveLoadManager.getMarks();
            var textList = saveLoadManager.getTextList();

            // Only if the mouse is not dragging to set to -1. This is for dragging control stability.
            if (!isMouseDragging) {
                selectedMark = -1;

                // Restore mouse appearance.
                Cursor dCursor = Cursor.getDefaultCursor();
                setCursor(dCursor);
            }

            Point mousePos = getMousePosition();
            final int iconSize = 10;
            for (int i = 0, wordIndex = -1; i < marks.size(); i++, wordIndex++) {
                long time = marks.get(i);

                int x = toX(time);

                // -----Draw the gaps:-----
                // First to reallocate marks size if the number of marks is too many.
                // (This will happen if the user delete words and influenced the exist marks.)
                if (saveLoadManager.getRedundantMarkQuantity() != 0) {
                    int rq = saveLoadManager.getRedundantMarkQuantity();
                    int q = textList.isEmpty() ? marks.size() : rq;
                    markCmdMgr.execute(new MarkPopQuantityCommand(marks, q));
                    canvas.repaint();
                }

                // Then draw the rects and strings.
                boolean isParagraphEnd = false;
                boolean shouldDrawGap = true;
                if (i > 0) {
                    // This is for telling if it is the end of the paragraph.
                    if (wordIndex + 2 < textList.size()) {
                        String nextS = textList.get(wordIndex + 1);
                        String nextnextS = textList.get(wordIndex + 2);
                        // The \n\n case.
                        if (nextS.equals("\n") && nextnextS.equals("\n"))
                            isParagraphEnd = true;
                    }

                    String s = textList.get(wordIndex);
                    // Handle the \n case.
                    if (s.equals("\n")) {
                        wordIndex++;
                        s = textList.get(wordIndex);

                        // For this gap should not draw. (meeting double \n)
                        if (s.equals("\n")) shouldDrawGap = false;
                    }

                    // x is applied to some adjusts to avoid covering the marks.
                    int lastX = toX(marks.get(i - 1)) + iconSize / 2 - 1;
                    int width = x - lastX - iconSize / 2 + 1;
                    int height = 15;
                    Font f = new Font(Font.SANS_SERIF, Font.BOLD, Math.min(height - 2, width));

                    // Draw the rectangle gaps.
                    if (shouldDrawGap) {
                        g2d.setColor(Color.WHITE);
                        g2d.fillRect(lastX, 0, width, height);
                    }

                    // Draw the words in the marks.
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(f);
                    g2d.drawString(s, lastX + width / 2 - f.getSize() * s.length() / 2, 10); // x is at the middle.
                }

                // Make sure the icon draw position is on the very middle.
                x -= iconSize / 2;

                // If icon selected, draw the selected icon.
                if (mousePos != null) {
                    // The cursor should cover on the icon.
                    boolean isCovered =
                            !isMouseDragging && mousePos.x >= x && mousePos.x <= x + iconSize && mousePos.y <= iconSize;

                    if (isCovered || selectedMark == i) { // selectedMark == i for dragging control stability.
                        selectedMark = i;

                        // Set mouse appearance.
                        Cursor hMoveCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                        setCursor(hMoveCursor);

                        // Handle dragging.
                        if (isMouseDragging) {
                            // Make sure user's not dragging out of available position.
                            long t = toTime(mousePos.x);
                            long lastT = i == 0 ? 0 : marks.get(i - 1);
                            long nextT = i == marks.size() - 1 ? Integer.MAX_VALUE : marks.get(i + 1);
                            if (t > lastT && t < nextT) { // only in the range available.
                                draggingMark = i;
                            } else {
                                draggingMark = -1;
                            }
                        }

                        g2d.drawImage(MARK_SELECTED_ICON.getImage(), x, 0, iconSize, iconSize, null);
                        continue;
                    }
                }

                // Draw the mark image. If the mark is the end mark, draw the special end icon.
                // p.s. End marks are the last one of all the marks or the last mark of the paragraph.
                if (i == marks.size() - 1 || isParagraphEnd) {
                    // End mark case.
                    g2d.drawImage(MARK_END_ICON.getImage(), x, 0, iconSize, iconSize, null);
                } else {
                    // General mark case.
                    g2d.drawImage(MARK_NORM_BUTTON_ICON.getImage(), x, 0, iconSize, iconSize, null);
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));

                    g2d.setColor(Color.BLACK);
                    g2d.drawString(Integer.toString(i), x + 3, 8);
                }
            }

            // Draw float mark(dragging mark)
            if (draggingMark != -1 && mousePos != null) {
                g2d.drawImage(MARK_FLOAT_ICON.getImage(), mousePos.x - iconSize / 2, 0, iconSize, iconSize, null);
            }
        }

        public void setSize() {
            if (saveLoadManager.getLoadedAudio() == null) return;

            canvas.scale = (float) controlPanel.slider.getValue() * 0.01f;
            long audioTime = saveLoadManager.getLoadedAudio().getTotalTime();
            // I don't know why it's ICON_SIZE.height * 2, but it works.
            canvas.setPreferredSize(new Dimension(toX(audioTime), getCanvasScrollPane().getHeight() - ICON_SIZE.height * 2));
            canvas.revalidate();
            scrollPane.requestFocus();
        }
    }
}