package net.okt.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ColorChooserButton extends JButton {
    private Color current;

    public ColorChooserButton(int rgb) {
        this(new Color(rgb));
    }

    public ColorChooserButton(Color c) {
        setSelectedColor(c);
        addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Choose a color", current);
            setSelectedColor(newColor);
        });
    }

    public void setSelectedColor(Color newColor) {
        if (newColor == null) return;

        current = newColor;
        setIcon(createIcon(current, 16, 16));
        repaint();

        for (ColorChangedListener l : listeners) {
            l.colorChanged(newColor);
        }
    }

    public interface ColorChangedListener {
        void colorChanged(Color newColor);
    }

    private final List<ColorChangedListener> listeners = new ArrayList<>();

    public void addColorChangedListener(ColorChangedListener toAdd) {
        listeners.add(toAdd);
    }

    public void callListeners() {
        for (ColorChangedListener l : listeners) {
            l.colorChanged(current);
        }
    }

    public static ImageIcon createIcon(Color main, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width - 1, height - 1);
        image.flush();
        return new ImageIcon(image);
    }
}