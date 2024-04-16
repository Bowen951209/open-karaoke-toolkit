package net.okt.gui;

import net.okt.system.SaveLoadManager;

import javax.swing.*;

public class DoubleTextBarPanel extends JPanel {
    private final SlidableNumberBar bar1;
    private final SlidableNumberBar bar2;

    public DoubleTextBarPanel(String title, int limitDigit, String name1, String name2, String propKey1,
                              String propKey2, SaveLoadManager saveLoadManager, Viewport viewport) {
        add(new JLabel(title));

        this.bar1 = new SlidableNumberBar(name1, limitDigit, propKey1, saveLoadManager);
        this.bar1.addDocumentListener(() -> {
            saveLoadManager.setProp(propKey1, bar1.getVal());
            viewport.repaint();
        });

        this.bar2 = new SlidableNumberBar(name2, limitDigit, propKey2, saveLoadManager);
        this.bar2.addDocumentListener(() -> {
            saveLoadManager.setProp(propKey2, bar2.getVal());
            viewport.repaint();
        });

        add(bar1);
        add(bar2);
    }

    public void addDocumentListener(Runnable e){
        bar1.addDocumentListener(e);
        bar2.addDocumentListener(e);
    }
}
