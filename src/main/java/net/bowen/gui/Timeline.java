package net.bowen.gui;

import net.bowen.system.SaveLoadManager;

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
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setMinimumSize(new Dimension(0, 100));


            // Key listener
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

                        case MouseEvent.BUTTON3 -> {
                            // If you right-click, delete selected mark.
                            if (canvas.selectedMark != -1) {
                                saveLoadManager.getMarks().remove(canvas.selectedMark);
                            }
                        }
                    }

                    canvas.repaint();
                    viewport.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    canvas.isMouseDragging = false;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    controlPanel.timeLabel.setText("");
                }
            });
            scrollPane.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int time = toTime(e.getX() + scrollPane.getHorizontalScrollBar().getValue());
                    controlPanel.timeLabel.setText(toMinutesAndSecond(time, 2));

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
                    if (e.isControlDown()) {
                        controlPanel.slider.setValue(controlPanel.slider.getValue() - e.getWheelRotation() * 5);
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
            scrollPane.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) controlPanel.playPauseButton.doClick();
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
            Dimension size = new Dimension(Integer.MAX_VALUE, ICON_SIZE.height);
            setLayout(new BorderLayout(0, 0));
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
                long time = toTime(pointerX);
                saveLoadManager.getMarks().add(time);
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

            slider.addMouseWheelListener(e -> slider.setValue(slider.getValue() + e.getWheelRotation()));
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
        private float scale = 1;
        private int selectedMark = -1;
        private boolean isMouseDragging;

        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight()); // background color

            g2d.drawImage(waveImg, 0, 0, getWidth(), getHeight(), null);
            drawSeparationLines(g2d);

            // Draw the marks
            drawMarks(g2d);

            // The current playing time pointer
            drawPointer(g2d, Color.RED, pointerX);

            // The cursor pointer
            Point mousePosition = getMousePosition();
            if (mousePosition != null)
                drawPointer(g2d, Color.DARK_GRAY, mousePosition.x);
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
            ArrayList<Long> marks = saveLoadManager.getMarks();

            if (!isMouseDragging) // Only if the mouse is not dragging to set to 0. This is for dragging control stability.
                selectedMark = -1;

            for (int i = 0; i < marks.size(); i++) {
                long time = marks.get(i);

                int iconSize = 10;
                // Make sure the icon draw position is on the very middle.
                int x = toX(time) - iconSize / 2;

                // If icon selected, draw the selected icon.
                Point mousePos = getMousePosition();
                if (mousePos != null) {
                    // The cursor should cover on the icon.
                    boolean isCovered =
                            !isMouseDragging && mousePos.x >= x && mousePos.x <= x + iconSize && mousePos.y <= iconSize;

                    if (isCovered || selectedMark == i) { // selectedMark == i for dragging control stability.
                        selectedMark = i;
                        if (isMouseDragging)
                            marks.set(selectedMark, (long) toTime(mousePos.x));

                        g2d.drawImage(MARK_SELECTED_ICON.getImage(), x, 0, iconSize, iconSize, null);
                        continue;
                    }
                }

                // If the mark is the last mark, draw the end icon.
                if (i != marks.size() - 1) {
                    g2d.drawImage(MARK_NORM_BUTTON_ICON.getImage(), x, 0, iconSize, iconSize, null);
                } else {
                    g2d.drawImage(MARK_END_ICON.getImage(), x, 0, iconSize, iconSize, null);
                }
            }
        }

        public void setSize() {
            canvas.scale = (float) controlPanel.slider.getValue() * 0.01f;
            long audioTime = saveLoadManager.getLoadedAudio().getTotalTime();
            canvas.setPreferredSize(new Dimension(toX(audioTime), getHeight()));
            canvas.revalidate();
            scrollPane.requestFocus();
        }
    }
}
