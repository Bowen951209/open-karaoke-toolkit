package net.okt.gui;

import net.okt.audioUtils.Audio;
import net.okt.system.LyricsProcessor;
import net.okt.system.SaveLoadManager;
import net.okt.system.command.CommandManager;
import net.okt.system.command.marks.MarkAddCommand;
import net.okt.system.command.marks.MarkRemoveCommand;
import net.okt.system.command.marks.MarkSetCommand;

import javax.swing.*;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Timeline extends JPanel {
    private static final ImageIcon PLAY_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/play.png")));
    private static final ImageIcon PAUSE_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/pause.png")));
    private static final ImageIcon STOP_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/stop.png")));
    private static final ImageIcon MARK_NORM_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_norm.png")));
    private static final ImageIcon MARK_END_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_end.png")));
    private static final ImageIcon MARK_SELECTED_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_selected.png")));
    private static final ImageIcon MARK_FLOAT_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_float.png")));
    private static final ImageIcon MARK_GRAY_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/mark_gray.png")));
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

    private final LyricsProcessor lyricsProcessor;
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

    public Timeline(SaveLoadManager saveLoadManager, LyricsProcessor lyricsProcessor, Viewport viewport) {
        super();
        this.saveLoadManager = saveLoadManager;
        this.lyricsProcessor = lyricsProcessor;
        this.viewport = viewport;
        this.canvas = new Canvas();
        this.controlPanel = new ControlPanel();
        this.scrollPane = getCanvasScrollPane();
        this.timer = new Timer(TIMER_DELAY, (e) -> {
            resetPointerX();

            JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
            int scrollX = scrollBar.getValue();
            int distance = getWidth() - pointerX + scrollX;

            // If less than 50 pixels from the end border || if we are viewing the further timeline and the pointer is not in view
            if (distance < 50 || distance > getWidth())
                scrollBar.setValue(pointerX - getWidth() + 50); // set to 50 pixels from the end border

            // If the audio is finished, stop the play.
            if (saveLoadManager.getLoadedAudio().isFinished())
                timeStop();

            canvas.repaint();

            viewport.repaint();
        });

        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(controlPanel);
        add(scrollPane);
    }

    public void setWaveImg(BufferedImage waveImg) {
        this.waveImg = waveImg;
    }

    public void setDisplayFileName(String name) {
        Audio audio = saveLoadManager.getLoadedAudio();
        String totalTime = toMinutesAndSecond(audio.getTotalTime(), 0);
        name += "(" + totalTime + ")";
        controlPanel.filenameLabel.setText(name);

        controlPanel.revalidate();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void timePlay() {
        timer.start();
        controlPanel.playPauseButton.setIcon(PAUSE_BUTTON_ICON);
        saveLoadManager.getLoadedAudio().play();
    }

    public void timePause() {
        timer.stop();
        controlPanel.playPauseButton.setIcon(PLAY_BUTTON_ICON);
        saveLoadManager.getLoadedAudio().pause();

        // Reset the pointer or else it will stop at a wrong position.
        resetPointerX();
        canvas.repaint();
    }

    public void timeStop() {
        isPlaying = false;
        timePause();
        scrollPane.getHorizontalScrollBar().setValue(0);
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

    private JScrollPane getCanvasScrollPane() {
        scrollPane = new JScrollPane(canvas);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setMinimumSize(new Dimension(0, 100));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(15);

        scrollPane.setFocusable(true);
        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (saveLoadManager.getLoadedAudio() == null) return;

                scrollPane.requestFocus();
                int x = (e.getX() + scrollPane.getHorizontalScrollBar().getValue());
                int ms = toTime(x);

                switch (e.getButton()) {
                    case MouseEvent.BUTTON1 -> {
                        // If you left-click, Jump the time.
                        saveLoadManager.getLoadedAudio().setTimeTo(ms);
                        resetPointerX();
                    }

                    case MouseEvent.BUTTON2 -> {
                        // If you middle-click, delete selected mark.
                        if (canvas.coveredMark != -1) {
                            var marks = saveLoadManager.getMarks();
                            markCmdMgr.execute(new MarkRemoveCommand(marks, canvas.coveredMark));
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
                controlPanel.timeLabel.setText(null);
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

        // Remove the default mouse wheel listener and add back later because the one we want to add disables the
        // scroll when ctrl is down. Thus, the default one should be performed after it.
        var defaultMouseListener = scrollPane.getMouseWheelListeners()[0];
        scrollPane.removeMouseWheelListener(defaultMouseListener);

        scrollPane.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    scrollPane.setWheelScrollingEnabled(false);

                    int speed = 10;
                    int orientation = -e.getWheelRotation();
                    JSlider slider = controlPanel.slider;
                    slider.setValue(slider.getValue() + orientation * speed);
                } else {
                    scrollPane.setWheelScrollingEnabled(true);
                }
            }
        });
        scrollPane.addMouseWheelListener(defaultMouseListener);

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

        return scrollPane;
    }

    /**
     * Make the {@link #scrollPane} focus on the given time in the middle.
     *
     * @param time          The time in the middle of the view.
     * @param timelineWidth The width of the timeline.(Not the canvas width.)
     */
    private void scrollPaneSetTime(int time, int timelineWidth) {
        int val = toX(time) - timelineWidth / 2;
        scrollPane.getHorizontalScrollBar().setValue(val);
    }

    private int toX(int time) {
        return (int) (time * PIXEL_TIME_RATIO * canvas.scale);
    }

    private int toTime(int x) {
        return (int) ((float) x / (PIXEL_TIME_RATIO * canvas.scale));
    }

    private void resetPointerX() {
        pointerX = toX(saveLoadManager.getLoadedAudio().getTimePosition());
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
        private final JSlider slider = getSlider();
        private final JLabel filenameLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();

        public ControlPanel() {
            super(new BorderLayout(0, 0));
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 10);
            timeLabel.setFont(font);
            filenameLabel.setFont(font);

            Dimension size = new Dimension(Integer.MAX_VALUE, ICON_SIZE.height);
            setMaximumSize(size);

            JPanel componentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

            playPauseButton.addActionListener(e -> {
                if (saveLoadManager.getLoadedAudio() == null) return;

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
            add(filenameLabel, BorderLayout.EAST);
        }

        private JButton getStopButton() {
            JButton stopButton = new JButton(STOP_BUTTON_ICON);
            stopButton.addActionListener(e -> {
                if (saveLoadManager.getLoadedAudio() == null) return;

                scrollPane.requestFocus(); // we want to keep the timeline focused.
                timeStop();
            });
            stopButton.setPreferredSize(ICON_SIZE);
            return stopButton;
        }

        private JButton getMarkButton() {
            JButton btn = new JButton(MARK_NORM_BUTTON_ICON);
            btn.addActionListener(e -> {
                if (saveLoadManager.getLoadedAudio() == null) return;

                scrollPane.requestFocus(); // we want to keep the timeline focused.

                var marks = saveLoadManager.getMarks();
                int pointerTime = saveLoadManager.getLoadedAudio().getTimePosition();
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

            slider.addChangeListener(e -> {
                if (saveLoadManager.getLoadedAudio() == null) return;
                int time = toTime(scrollPane.getHorizontalScrollBar().getValue() + getWidth() / 2);
                canvas.setSize();
                resetPointerX();
                scrollPaneSetTime(time, getWidth());
            });

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
    }

    public class Canvas extends JPanel {
        private static final FontRenderContext FRC = new FontRenderContext(null, false, true);
        private final Font FONT_PLAIN_10 = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        private final Font FONT_BOLD_8 = new Font(Font.SANS_SERIF, Font.BOLD, 8);
        private final int MARK_ICON_SIZE = 10;

        private float scale = 1;
        private int coveredMark = -1;
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

            Point mousePos = getMousePosition();
            handleMouseEvent(mousePos);

            // Draw the gaps.
            drawGaps(g2d);

            // Draw the marks
            drawMarks(g2d, mousePos);
            drawDraggingMark(g2d, mousePos);

            // The cursor pointer & update label
            if (mousePos != null) {
                int time = toTime(mousePos.x);
                controlPanel.timeLabel.setText(toMinutesAndSecond(time, 2));

                drawPointer(g2d, Color.DARK_GRAY, mousePos.x);
            }

            // The current playing time pointer
            drawPointer(g2d, Color.RED, pointerX);
        }

        public void setSize() {
            if (saveLoadManager.getLoadedAudio() == null) return;

            canvas.scale = (float) controlPanel.slider.getValue() * 0.01f;
            int audioTime = saveLoadManager.getLoadedAudio().getTotalTime();

            int height = (int) canvas.getPreferredSize().getHeight();
            canvas.setPreferredSize(new Dimension(toX(audioTime), height));
            canvas.revalidate();
            scrollPane.requestFocus();
        }

        private void drawSeparationLines(Graphics2D g2d) {
            g2d.setFont(FONT_PLAIN_10);

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

        private void drawMarks(Graphics2D g2d, Point mousePos) {
            var marks = saveLoadManager.getMarks();

            if (!isMouseDragging) coveredMark = -1;

            for (int i = 0; i < marks.size(); i++) {
                // If icon is covered, draw the covered style icon.
                int markX = toX(marks.get(i)) - MARK_ICON_SIZE / 2;
                boolean isCovered = isMouseCoverMark(markX, mousePos);

                if (isCovered || coveredMark == i) { // selectedMark == i for dragging control stability.
                    coveredMark = i;
                    g2d.drawImage(MARK_SELECTED_ICON.getImage(), markX, 0, MARK_ICON_SIZE, MARK_ICON_SIZE, null);
                } else {
                    // Draw the mark image. If the mark is the end mark, draw the special end icon.
                    // p.s. End marks are the last one of all the marks or the last mark of the paragraph.
                    drawMark(i, markX, g2d);
                }
            }
        }

        /**
         * Draw the gaps between marks, including text gaps, ready dot gaps, and disappear time gaps.
         */
        private void drawGaps(Graphics2D g2d) {
            var marks = saveLoadManager.getMarks();

            for (int i = 0; i < marks.size(); i++) {
                if (i >= lyricsProcessor.getMaxMarkNumber()) break;

                String gapText = lyricsProcessor.getTextBeforeMark(i);
                int currentMarkX = toX(marks.get(i));

                if (gapText == null) {
                    // If it is the head of a paragraph draw ready dot rect.
                    drawReadyDotsRect(currentMarkX, g2d);
                } else {
                    // If it is the body of a paragraph, draw gaps and strings between last mark and current mark.
                    drawTextGap(i, gapText, g2d);
                }

                if (lyricsProcessor.isParagraphEndMark(i))
                    drawDisappearHintGap(currentMarkX, g2d);
            }
        }

        private void drawMark(int markIdx, int x, Graphics2D g2d) {
            if (lyricsProcessor.isParagraphEndMark(markIdx))
                g2d.drawImage(MARK_END_ICON.getImage(), x, 0, MARK_ICON_SIZE, MARK_ICON_SIZE, null);
            else if (lyricsProcessor.isRedundantMark(markIdx))
                g2d.drawImage(MARK_GRAY_ICON.getImage(), x, 0, MARK_ICON_SIZE, MARK_ICON_SIZE, null);
            else
                g2d.drawImage(MARK_NORM_BUTTON_ICON.getImage(), x, 0, MARK_ICON_SIZE, MARK_ICON_SIZE, null);

            // Draw the first digit of the mark index on the mark.
            String number = String.valueOf(markIdx % 10);
            g2d.setColor(Color.BLACK);
            g2d.setFont(FONT_BOLD_8);
            g2d.drawString(number, x + 3, 8);
        }

        /**
         * Draw the float mark that appears under the cursor if a mark is dragged.
         */
        private void drawDraggingMark(Graphics2D g2d, Point mousePos) {
            if (draggingMark != -1 && mousePos != null) {
                g2d.drawImage(
                        MARK_FLOAT_ICON.getImage()
                        , mousePos.x - MARK_ICON_SIZE / 2, 0, MARK_ICON_SIZE, MARK_ICON_SIZE, null
                );
            }
        }

        private void drawTextGap(int markIdx, String string, Graphics2D g2d) {
            var marks = saveLoadManager.getMarks();
            int thisX = toX(marks.get(markIdx));
            // lastX and gapWidth variables are applied to some adjusts to avoid covering the marks.
            int lastX = toX(marks.get(markIdx - 1)) + MARK_ICON_SIZE / 2 - 1;
            int gapWidth = thisX - lastX - MARK_ICON_SIZE / 2 + 1;
            int height = 15;
            Font f = new Font(Font.SANS_SERIF, Font.BOLD, Math.min(height - 2, gapWidth));
            int stringWidth = (int) f.getStringBounds(string, FRC).getWidth();
            int stringX = lastX + (gapWidth - stringWidth) / 2;

            // Draw the rectangle.
            g2d.setColor(Color.WHITE);
            g2d.fillRect(lastX, 0, gapWidth, height);

            // Draw the words in the gaps.
            g2d.setColor(Color.BLACK);
            g2d.setFont(f);
            g2d.drawString(string, stringX, 10); // x is at the middle.
        }

        private void drawReadyDotsRect(int markX, Graphics2D g2d) {
            int period = saveLoadManager.getPropInt("dotsPeriod");
            int dotsNum = saveLoadManager.getPropInt("dotsNum");
            int width = toX(period);

            // Draw the rectangle.
            g2d.setColor(Color.GRAY);
            g2d.fillRect(markX - width, 0, width, 15);

            // Draw dots inside the rectangle.
            int arcSize = 10;
            int widthPerBlock = width / dotsNum;
            int dotX = markX - width + widthPerBlock / 2 - arcSize / 2;
            g2d.setColor(Color.BLUE);
            for (int j = 0; j < dotsNum; j++) {
                g2d.fillArc(dotX, 2, arcSize, arcSize, 0, 360);
                dotX += widthPerBlock;
            }
        }

        private void drawDisappearHintGap(int startX, Graphics2D g2d) {
            int period = saveLoadManager.getPropInt("textDisappearTime");

            g2d.setColor(Color.GREEN);
            g2d.fillRect(startX, 0, toX(period), 15);
        }


        private void handleMouseEvent(Point mousePos) {
            var marks = saveLoadManager.getMarks();
            if (mousePos == null) return;

            // Handle if the mark is being dragged.
            if (isMouseDragging && coveredMark != -1) {
                // Make sure user's not dragging out of available position.
                int t = toTime(mousePos.x);
                int lastT = coveredMark == 0 ? 0 : marks.get(coveredMark - 1);
                int nextT = coveredMark == marks.size() - 1 ? Integer.MAX_VALUE : marks.get(coveredMark + 1);
                if (t > lastT && t < nextT) { // only in the range available.
                    draggingMark = coveredMark;
                } else {
                    draggingMark = -1;
                }
            }

            // Mouse appearance.
            if (coveredMark == -1) {
                Cursor dCursor = Cursor.getDefaultCursor();
                setCursor(dCursor);
            } else {
                Cursor hMoveCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                setCursor(hMoveCursor);
            }
        }

        private boolean isMouseCoverMark(int markX, Point mousePos) {
            if (mousePos == null) return false;

            return !isMouseDragging &&
                    mousePos.x >= markX && mousePos.x <= markX + MARK_ICON_SIZE && mousePos.y <= MARK_ICON_SIZE;
        }
    }
}