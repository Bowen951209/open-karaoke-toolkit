package net.okt.gui;

import net.okt.system.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class VolumeSliderPanel extends JPanel {
    private static final BasicStroke OUTLINE_STROKE = new BasicStroke(2);
    private GradientPaint gradientPaint;

    private final int max;
    private final int min;
    private final int range;
    private final List<Runnable> valueChangeListeners = new ArrayList<>();

    private int value;

    public VolumeSliderPanel(int min, int max, int value) {
        this.max = max;
        this.min = min;
        this.range = max - min;
        this.value = value;
        this.valueChangeListeners.add(this::repaint);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setValueToMouseX(e.getX());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setValueToMouseX(e.getX());
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                setValue(getValue() - e.getWheelRotation());
            }
        });
    }

    public void addValueChangeListener(Runnable l) {
        valueChangeListeners.add(l);
    }

    public void setValue(int value) {
        this.value = MathUtils.clamp(value, min, max);
        valueChangeListeners.forEach(Runnable::run);
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int triangleHeight = getHeight() * value / max;
        int triangleWidth = getPointerX();
        Path2D triangleFill = getTriangle(triangleWidth, triangleHeight);
        g2d.setPaint(getGradientPaint());
        g2d.fill(triangleFill);

        Path2D triangleOutline = getTriangle(getWidth(), getHeight());
        g2d.setColor(Color.GRAY);
        g2d.setStroke(OUTLINE_STROKE);
        g2d.draw(triangleOutline);

        g2d.drawString(value + "%", 15, 12);
    }

    private void setValueToMouseX(int mouseX) {
        setValue(mouseX * range / getWidth());
    }

    private GradientPaint getGradientPaint() {
        if (gradientPaint == null)
            gradientPaint = new GradientPaint(0, 0, Color.GREEN, getWidth(), 0, Color.ORANGE);

        return gradientPaint;
    }

    private Path2D getTriangle(int w, int h) {
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(0, getHeight());
        triangle.lineTo(w, getHeight());
        triangle.lineTo(w, getHeight() - h);
        triangle.closePath();

        return triangle;
    }

    private int getPointerX() {
        return getWidth() * value / range;
    }
}
