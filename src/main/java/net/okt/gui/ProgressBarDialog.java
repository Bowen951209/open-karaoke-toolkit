package net.okt.gui;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressBarDialog extends JDialog {
    public final JProgressBar progressBar;

    private Runnable manualCloseOperation;

    public void setManualCloseOperation(Runnable manualCloseOperation) {
        this.manualCloseOperation = manualCloseOperation;
    }

    public ProgressBarDialog(String title, String closingMessage) {
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(true);

        setTitle(title);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        getContentPane().add(progressBar);
        setLocationRelativeTo(null); // This sets the dialog position to the middle of the screen.
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(ProgressBarDialog.this, closingMessage, "Warning",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    ProgressBarDialog.this.dispose();

                    if (manualCloseOperation != null)
                        manualCloseOperation.run();
                }
            }
        });
    }

    public void showFinish(String msg) {
        dispose();
        JOptionPane.showMessageDialog(null, msg);
    }
}
