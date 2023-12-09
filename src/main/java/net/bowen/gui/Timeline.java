package net.bowen.gui;

import net.bowen.system.SaveLoadManager;

import javax.swing.*;
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
    private static final int SEP_LINE_INTERVAL_MS = 500;
    public static final float PIXEL_TIME_RATIO = (float) SEP_LINE_INTERVAL / (float) SEP_LINE_INTERVAL_MS;
    /**
     * The delay time of {@link Timeline#timer}
     */
    private static final int TIMER_DELAY = 10;

    private final Canvas canvas;
    private final ControlPanel controlPanel = new ControlPanel();
    private final SaveLoadManager saveLoadManager;
    private final Timer timer;
    private final Viewport viewport;

    private BufferedImage waveImg;
    private JScrollPane scrollPane;

    private boolean isPlaying;
    private int pointerX;

    public void setWaveImg(BufferedImage waveImg) {
        this.waveImg = waveImg;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Timeline(SaveLoadManager saveLoadManager, Viewport viewport) {
        super();
        this.saveLoadManager = saveLoadManager;
        this.viewport = viewport;
        this.canvas = new Canvas();
        this.timer = new Timer(TIMER_DELAY, (e) -> {
            pointerX = (int) (saveLoadManager.getLoadedAudio().getTimePosition() * PIXEL_TIME_RATIO);

            JScrollBar scrollBar = getCanvasScrollPane().getHorizontalScrollBar();
            int scrollX = scrollBar.getValue();
            int distance = getWidth() - pointerX + scrollX;

            // if less than 50 pixels from the end border || if we are viewing the further timeline and pointer is not in view
            if (distance < 50 || distance > getWidth())
                scrollBar.setValue(pointerX - getWidth() + 50); // set to 50 pixels from the end border

            canvas.repaint(pointerX - 10, 0, pointerX, canvas.getHeight()); // we need bigger clear area to clear properly.

            viewport.repaint();
        });

        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(controlPanel);
        add(getCanvasScrollPane());
    }

    public JScrollPane getCanvasScrollPane() {
        if (scrollPane == null) { // if scrollPane == null, init it.
            scrollPane = new JScrollPane(canvas);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);


            // Key listener
            scrollPane.setFocusable(true);
            scrollPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollPane.requestFocus();
                    float x = e.getX() + scrollPane.getHorizontalScrollBar().getValue();
                    int ms = (int) (x / PIXEL_TIME_RATIO);

                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1 -> {
                            // If left click, Jump the time.
                            saveLoadManager.getLoadedAudio().setTimeTo(ms);
                            pointerX = (int) (saveLoadManager.getLoadedAudio().getTimePosition() * PIXEL_TIME_RATIO);
                        }

                        case MouseEvent.BUTTON3 -> {
                            // If right click, delete selected mark.
                            if (canvas.selectedMark != -1) {
                                saveLoadManager.getMarks().remove(canvas.selectedMark);
                            }
                        }
                    }

                    canvas.repaint();
                    viewport.repaint();
                }
            });
            scrollPane.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    canvas.repaint();
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

    private class ControlPanel extends JPanel {
        private final JButton playPauseButton = new JButton(PLAY_BUTTON_ICON);

        public ControlPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, ICON_SIZE.height));

            playPauseButton.addActionListener(e -> {
                scrollPane.requestFocus(); // we want to keep the timeline focused.

                isPlaying = !isPlaying;
                if (isPlaying)
                    timePlay();
                else
                    timePause();
            });
            playPauseButton.setPreferredSize(ICON_SIZE);

            add(playPauseButton);
            add(getStopButton());
            add(getMarkButton());
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

                // don't know why when getting the audio play time would have precise error. So use another method to replace.
                // long time = saveLoadManager.getLoadedAudio().getTimePosition();
                long time = (long) ((float) pointerX / PIXEL_TIME_RATIO);
                saveLoadManager.getMarks().add(time);
            });
            btn.setPreferredSize(ICON_SIZE);

            return btn;
        }
    }

    public class Canvas extends JPanel {
        private int selectedMark = -1;

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
            float second = 0; // the time we are current at in second.
            while (pointingPixel < getWidth()) {
                pointingPixel += SEP_LINE_INTERVAL;
                second += SEP_LINE_INTERVAL_MS * 0.001f;

                g2d.setColor(Color.GRAY);
                g2d.drawLine(pointingPixel, 0, pointingPixel, getHeight());
                g2d.setColor(Color.BLACK);
                g2d.drawString(Float.toString(second), pointingPixel, 10);
            }
        }

        private void drawPointer(Graphics2D g2d, Color color, int x) {
            g2d.setColor(color);
            g2d.drawLine(x, 0, x, getHeight());
        }

        private void drawMarks(Graphics2D g2d) {
            g2d.setColor(Color.YELLOW);
            ArrayList<Long> marks = saveLoadManager.getMarks();

            selectedMark = -1;
            for (int i = 0; i < marks.size(); i++) {
                long time = marks.get(i);

                int iconSize = 10;
                // Make sure the icon draw position is on the very middle.
                int x = (int) (time * PIXEL_TIME_RATIO) - iconSize / 2;

                // If mouse on icon, draw the selected icon.
                Point mousePos = getMousePosition();
                if (mousePos != null) {
                    if (mousePos.x >= x && mousePos.x <= x + iconSize && mousePos.y <= iconSize) {
                        selectedMark = i;
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
    }
}
