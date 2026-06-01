package util;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;

/** Shared text-field styling so typing is visible and works with FlatLaf. */
public final class UiStyles {

    private static final Color FIELD_BG = new Color(49, 50, 68);
    private static final Color FIELD_FG = new Color(255, 255, 255);
    private static final Color FIELD_BORDER = new Color(88, 91, 112);
    private static final Color FIELD_BORDER_FOCUS = new Color(137, 180, 250);

    private UiStyles() {
    }

    public static void applyInputFieldStyle(JTextField field) {
        field.setEditable(true);
        field.setEnabled(true);
        field.setFocusable(true);
        field.setOpaque(true);
        field.setBackground(FIELD_BG);
        field.setForeground(FIELD_FG);
        field.setCaretColor(Color.WHITE);
        field.setDisabledTextColor(new Color(166, 173, 200));
        field.setSelectionColor(new Color(137, 180, 250));
        field.setSelectedTextColor(new Color(30, 30, 46));
        field.setBorder(createFieldBorder(false));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(createFieldBorder(true));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(createFieldBorder(false));
            }
        });
        // Avoid FlatLaf round-rect quirks on some Windows setups
        field.putClientProperty("JComponent.roundRect", false);
        field.putClientProperty("JTextField.showClearButton", false);
    }

    private static Border createFieldBorder(boolean focused) {
        Color line = focused ? FIELD_BORDER_FOCUS : FIELD_BORDER;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8));
    }
}
