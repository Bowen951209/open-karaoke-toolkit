package net.okt.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

public class TextFieldFileChooser extends JPanel {
    private final JTextField textField;
    private final JFileChooser fileChooser;

    private FileNameExtensionFilter extensionFilter;

    public void setExtensionFilter(String description, String extension) {
        String previousFilePath = fileChooser.getSelectedFile().getPath();
        // Modify the file path with the new extension.
        File newFile = new File(previousFilePath.split("\\.")[0] + "." + extension);
        this.fileChooser.setSelectedFile(newFile);
        this.textField.setText(newFile.getAbsolutePath());

        this.extensionFilter = new FileNameExtensionFilter(description, extension);
        this.fileChooser.setFileFilter(this.extensionFilter);
    }

    public TextFieldFileChooser(String defaultFile) {
        this.textField = new JTextField(defaultFile);
        this.textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fileChooser.setSelectedFile(new File(textField.getText()));
            }
        });

        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
