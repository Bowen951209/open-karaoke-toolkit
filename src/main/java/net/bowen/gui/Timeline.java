package net.bowen.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static java.lang.System.currentTimeMillis;

public class Timeline extends JPanel {
    // TODO: 2023/11/15 Implement this class.

    private static final ImageIcon PLAY_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/play.png")));
    private static final ImageIcon PAUSE_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/pause.png")));
    private static final ImageIcon STOP_BUTTON_ICON = new ImageIcon(Objects.requireNonNull(Timeline.class.getResource("/icons/stop.png")));
    private static final Dimension ICON_SIZE = new Dimension(PLAY_BUTTON_ICON.getIconHeight(), PLAY_BUTTON_ICON.getIconWidth());
    /**
     * The width between separation lines in pixel.
     */
    private static final int SEP_LINE_INTERVAL = 30;
    /**
     * The time interval between separation lines in milliseconds.
     */
    private static final int SEP_LINE_INTERVAL_MS = 500;
    private static final float PIXEL_TIME_RATIO = (float) SEP_LINE_INTERVAL / (float) SEP_LINE_INTERVAL_MS;
    /**
     * The delay time of {@link Timeline#timer}
     */
    private static final int TIMER_DELAY = 10;

    private final Canvas canvas = new Canvas();

    /**
     * The time the pointer is at in millisecond.
     */
    private float time;

    private final Timer timer = new Timer(TIMER_DELAY);

    private JScrollPane scrollPane;

    private boolean isPlaying;


    public Timeline() {
        super();
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridLayout(0, 1)); // make child components full size

        add(new ControlPanel());
        add(getCanvasScrollPane());
    }

    public JScrollPane getCanvasScrollPane() {
        if (this.scrollPane == null) { // if scrollPane == null, init it.
            this.scrollPane = new JScrollPane(canvas);
            this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        }
        return this.scrollPane;
    }

    private class ControlPanel extends JPanel {
        JButton playPauseButton = new JButton(PLAY_BUTTON_ICON);

        public ControlPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT));

            playPauseButton.addActionListener(e -> {
                isPlaying = !isPlaying;
                if (isPlaying)
                    timePlay();
                else
                    timePause();
            });
            playPauseButton.setPreferredSize(ICON_SIZE);

            JButton restartButton = new JButton(STOP_BUTTON_ICON);
            restartButton.addActionListener(e -> {
                timeRestart();
                canvas.repaint();
            });
            restartButton.setPreferredSize(ICON_SIZE);

            add(playPauseButton);
            add(restartButton);
        }

        private void timePlay() {
            timer.start();
            playPauseButton.setIcon(PAUSE_BUTTON_ICON);
        }

        private void timePause() {
            timer.stop();
            playPauseButton.setIcon(PLAY_BUTTON_ICON);
        }

        private void timeRestart() {
            isPlaying = false;
            timePause();
            time = 0;

            getCanvasScrollPane().getHorizontalScrollBar().setValue(0);
        }
    }

    private class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight()); // background color

            drawSeparationLines(g2d);
            drawPointer(g2d, time);
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

        private void drawPointer(Graphics2D g2d, float time) {
            g2d.setColor(Color.RED);
            int x = (int) (time * PIXEL_TIME_RATIO);
            g2d.drawLine(x, 0, x, getHeight());
        }
    }

    private class Timer extends javax.swing.Timer {
        private static final int TIME_NuLL = 0;
        private long previousSystemTime = TIME_NuLL;

        public Timer(int delay) {
            super(delay, null);
            addActionListener((e) -> {
                // Correct time
                if (previousSystemTime == TIME_NuLL) previousSystemTime = currentTimeMillis();
                time += currentTimeMillis() - previousSystemTime;

                int reasonableWidth = (int) (time * PIXEL_TIME_RATIO);
                if (reasonableWidth > canvas.getWidth()) {
                    canvas.setPreferredSize(new Dimension(reasonableWidth, canvas.getHeight()));
                    canvas.revalidate();

                    JScrollBar scrollBar = getCanvasScrollPane().getHorizontalScrollBar();
                    scrollBar.setValue(scrollBar.getMaximum());
                }

                canvas.repaint();

                previousSystemTime = currentTimeMillis();
            });
        }

        @Override
        public void stop() {
            super.stop();
            previousSystemTime = TIME_NuLL;
        }
    }
}
