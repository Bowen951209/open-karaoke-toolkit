package net.okt.gui;

import javax.swing.*;
import java.awt.*;

public class TitledComboBox<E> extends JPanel {
    private final JComboBox<E> comboBox;
    public TitledComboBox(String title, E[] elements) {
        super(new FlowLayout(FlowLayout.LEFT));

        // Set the size.
        setPreferredSize(new Dimension(150, 30));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());

        this.comboBox = new JComboBox<>(elements);
        add(new JLabel(title));
        add(comboBox);
    }

    public Object getSelectedElement() {
        return comboBox.getSelectedItem();
    }

    public void setSelectedItem(Object o) {
        comboBox.setSelectedItem(o);
    }

    public void addActionListener(Runnable r) {
        comboBox.addActionListener(e -> r.run());
    }
}
