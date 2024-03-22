package net.okt.gui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class TextFieldLimit extends JTextField {

    /**
     * @param acceptOnlyNumbers Accept all char or only numbers.
     * @param limit limit number of char of the text field.
     */
    public TextFieldLimit(boolean acceptOnlyNumbers, int limit, String defaultText) {
        Document document;
        if (acceptOnlyNumbers) {
            // Not number is not allowed.
            document = new PlainDocument() {
                @Override
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    if (str == null)
                        return;

                    try {
                        Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        return;
                    }

                    if ((getLength() + str.length()) <= limit) {
                        super.insertString(offs, str, a);
                    }
                }
            };
        } else {
            document = new PlainDocument() {
                @Override
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    if (str == null)
                        return;

                    if ((getLength() + str.length()) <= limit) {
                        super.insertString(offs, str, a);
                    }
                }
            };
        }

        // Set default text.
        try {
            document.insertString(0, defaultText, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

        setColumns(limit);
        setDocument(document);
    }
}