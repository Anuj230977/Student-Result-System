package view;

import dao.StudentDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginView extends JFrame {

    JTextField txtUsername;
    JPasswordField txtPassword;
    StudentDAO dao = new StudentDAO();

    public LoginView() {
        setTitle("Login — Student Result System");
        setSize(380, 280);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 46));
        setLayout(new BorderLayout());

        add(createTopPanel(),  BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createBotPanel(),  BorderLayout.SOUTH);

        setVisible(true);
    }

    JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(49, 50, 68));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lbl = new JLabel("Student Result Management System");
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        lbl.setForeground(new Color(205, 214, 244));
        panel.add(lbl);
        return panel;
    }

    JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 46));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setForeground(new Color(166, 173, 200));
        panel.add(lblUser, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        styleField(txtUsername);
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setForeground(new Color(166, 173, 200));
        panel.add(lblPass, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        styleField(txtPassword);
        // Login on Enter key
        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) login();
            }
        });
        panel.add(txtPassword, gbc);

        return panel;
    }

    JPanel createBotPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 46));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(137, 180, 250));
        btnLogin.setForeground(new Color(30, 30, 46));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 13));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setPreferredSize(new Dimension(120, 35));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> login());
        panel.add(btnLogin);

        JLabel hint = new JLabel("Default: admin / admin123");
        hint.setForeground(new Color(88, 91, 112));
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        panel.add(hint);

        return panel;
    }

    void styleField(JTextField field) {
        field.setBackground(new Color(49, 50, 68));
        field.setForeground(new Color(205, 214, 244));
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
    }

    void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try {
            if (dao.login(username, password)) {
                dispose(); // close login window
                new MainView(); // open main app
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password!", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }
}