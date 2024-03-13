package net.okt.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

public class TextFieldFileChooser extends JPanel {
    private final JTextField textField;
    private final JFileChooser fileChooser;

    public TextFieldFileChooser(String defaultFile) {
        FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter(".mp4", "mp4");

        this.textField = new JTextField(defaultFile);
        this.textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fileChooser.setSelectedFile(new File(textField.getText()));
            }
        });

        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.fileChooser.setFileFilter(extensionFilter);
        this.fileChooser.setSelectedFile(new File(defaultFile));


        JButton button = new JButton(UIManager.getIcon("FileView.directoryIcon"));
        button.addActionListener(e -> {
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
                String extension = "." + extensionFilter.getExtensions()[0];
                if (!fileChooser.getSelectedFile().getName().endsWith(extension))
                    fileChooser.setSelectedFile(new File(getSelectedFile() + extension));

                this.textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        add(textField);
        add(button);
    }

    public File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }
}
