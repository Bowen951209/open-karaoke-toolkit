package net.bowen.gui;

import net.bowen.audioUtils.BoxWaveform;
import net.bowen.system.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Timeline extends JPanel {
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
    public static final float PIXEL_TIME_RATIO = (float) SEP_LINE_INTERVAL / (float) SEP_LINE_INTERVAL_MS;
    /**
     * The delay time of {@link Timeline#timer}
     */
    private static final int TIMER_DELAY = 10;

    private final Canvas canvas;
    private final ControlPanel controlPanel = new ControlPanel();
    private final SaveLoadManager saveLoadManager;
    private final Timer timer;

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

    public Timeline(SaveLoadManager saveLoadManager) {
        super();
        this.saveLoadManager = saveLoadManager;
        this.canvas = new Canvas();
        this.timer = new Timer(TIMER_DELAY, (e) -> {
            pointerX = (int) (saveLoadManager.getLoadedAudio().getTimePosition() * PIXEL_TIME_RATIO);
            canvas.repaint(pointerX - 10, 0, pointerX, canvas.getHeight()); // we need bigger clear area to clear properly.
        });
        this.waveImg = BoxWaveform.loadImage(saveLoadManager.getLoadedAudio().getUrl(),
                new Dimension(canvas.getPreferredSize().width, 50), 1, new Color(5, 80, 20));

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
        saveLoadManager.getLoadedAudio().zero();

        pointerX = 0;
        canvas.repaint();
    }

    private class ControlPanel extends JPanel{
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

            JButton stopButton = getStopButton();

            add(playPauseButton);
            add(stopButton);
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
    }

    public class Canvas extends JPanel {
        public Canvas() {
            setPreferredSize(new Dimension((int) (saveLoadManager.getLoadedAudio().getTotalTime() * PIXEL_TIME_RATIO), 0));
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight()); // background color

            g2d.drawImage(waveImg, 0, 0, getWidth(), getHeight(), null);
            drawSeparationLines(g2d);
            drawPointer(g2d);
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

        private void drawPointer(Graphics2D g2d) {
            g2d.setColor(Color.RED);
            g2d.drawLine(pointerX, 0, pointerX, getHeight());
        }
    }
}
