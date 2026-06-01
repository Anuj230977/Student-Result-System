package view;

import com.formdev.flatlaf.FlatDarkLaf;
import service.StudentService;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
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
