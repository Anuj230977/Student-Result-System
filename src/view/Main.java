package view;

import com.formdev.flatlaf.FlatDarkLaf;
import service.StudentService;
import java.awt.Color;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Keep typed text visible in custom-styled fields
            Color fieldBg = new Color(49, 50, 68);
            Color fieldFg = Color.WHITE;
            UIManager.put("TextField.background", fieldBg);
            UIManager.put("TextField.foreground", fieldFg);
            UIManager.put("TextField.caretForeground", fieldFg);
            UIManager.put("PasswordField.background", fieldBg);
            UIManager.put("PasswordField.foreground", fieldFg);
            UIManager.put("PasswordField.caretForeground", fieldFg);
        } catch (Exception e) {
            System.out.println("FlatLaf error: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            StudentService service = new StudentService();
            try {
                service.initialize();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the database.\n\n"
                        + StudentService.friendlyMessage(e)
                        + "\n\nCopy config.properties.example to config.properties "
                        + "and set your MySQL password.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            new LoginView(service);
        });
    }
}
