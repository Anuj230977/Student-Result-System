package view;

import model.Student;
import service.StudentService;
import util.ReportExporter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import javax.swing.text.*;

public class MainView extends JFrame {

    private final StudentService service;

    String[] subjectNames = new String[5];

    // Input fields
    JTextField txtRoll, txtName, txtS1, txtS2, txtS3, txtS4, txtS5, txtSearch;
    JTextField[] subjectNameFields = new JTextField[5];
    JLabel lblTotal, lblPercent, lblGrade, lblRowCount;
    JLabel lblTotalStudents, lblPassPercent, lblAvgScore;
    JTable table;
    DefaultTableModel tableModel;
    JComboBox<String> filterCombo;

    public MainView(StudentService service) {
        this.service = service;
        try {
            subjectNames = service.getSubjectNames();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, StudentService.friendlyMessage(e));
        }

        setTitle("Student Result Management System");
        setSize(1150, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(new Color(30, 30, 46));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadAllStudents();
        updateStats();
        setVisible(true);
    }

    // ─── TOP PANEL ───────────────────────────────────────────────
    JPanel createTopPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(49, 50, 68));
        outer.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Student Result Management System");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(205, 214, 244));
        outer.add(title, BorderLayout.WEST);

        // Stats cards
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setBackground(new Color(49, 50, 68));

        lblTotalStudents = makeStatLabel("Total: 0");
        lblPassPercent = makeStatLabel("Pass %: 0%");
        lblAvgScore = makeStatLabel("Avg: 0%");

        statsPanel.add(makeStatCard("Total Students", lblTotalStudents, new Color(137, 180, 250)));
        statsPanel.add(makeStatCard("Pass %", lblPassPercent, new Color(166, 227, 161)));
        statsPanel.add(makeStatCard("Class Average", lblAvgScore, new Color(250, 179, 135)));

        outer.add(statsPanel, BorderLayout.EAST);
        return outer;
    }

    JLabel makeStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setForeground(new Color(30, 30, 46));
        return lbl;
    }

    JPanel makeStatCard(String title, JLabel valueLbl, Color bg) {
        JPanel card = new JPanel(new BorderLayout(3, 3));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(30, 30, 46));
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    // ─── FORM PANEL ──────────────────────────────────────────────
    JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 46));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(300, 0));

        addSectionLabel(panel, "-- Subject Names --");
        for (int i = 0; i < 5; i++) {
            subjectNameFields[i] = new JTextField(subjectNames[i]);
            styleTextField(subjectNameFields[i]);
            subjectNameFields[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            panel.add(subjectNameFields[i]);
            panel.add(Box.createVerticalStrut(3));
        }
        JButton btnSave = makeButton("Save Subject Names", new Color(148, 226, 213), e -> saveSubjectNames());
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(btnSave);
        panel.add(Box.createVerticalStrut(12));

        addSectionLabel(panel, "-- Student Details --");
        txtRoll = addField(panel, "Roll Number:");
        txtName = addField(panel, "Student Name:");
        txtS1 = addField(panel, "Marks 1:");
        txtS2 = addField(panel, "Marks 2:");
        txtS3 = addField(panel, "Marks 3:");
        txtS4 = addField(panel, "Marks 4:");
        txtS5 = addField(panel, "Marks 5:");
        applyMarksFilter(txtS1);
        applyMarksFilter(txtS2);
        applyMarksFilter(txtS3);
        applyMarksFilter(txtS4);
        applyMarksFilter(txtS5);

        panel.add(Box.createVerticalStrut(6));
        lblTotal = addResultLabel(panel, "Total:      -");
        lblPercent = addResultLabel(panel, "Percentage: -");
        lblGrade = addResultLabel(panel, "Grade:      -");
        panel.add(Box.createVerticalStrut(10));

        String[][] buttons = {
            {"Add Student", "89b4fa"},
            {"Update Student", "a6e3a1"},
            {"Delete Student", "f38ba8"},
            {"Clear Fields", "94e2d5"}
        };
        ActionListener[] actions = {
            e -> addStudent(),
            e -> updateStudent(),
            e -> deleteStudent(),
            e -> clearFields()
        };
        for (int i = 0; i < buttons.length; i++) {
            JButton btn = makeButton(buttons[i][0], hexColor(buttons[i][1]), actions[i]);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            panel.add(btn);
            panel.add(Box.createVerticalStrut(5));
        }

        return panel;
    }

    // ─── TABLE PANEL ─────────────────────────────────────────────
    JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(30, 30, 46));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        // Top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        topBar.setBackground(new Color(30, 30, 46));

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(new Color(205, 214, 244));
        txtSearch = new JTextField(15);
        styleTextField(txtSearch);

        filterCombo = new JComboBox<>(new String[]{"All", "Pass", "Fail"});
        filterCombo.setBackground(new Color(49, 50, 68));
        filterCombo.setForeground(new Color(205, 214, 244));
        filterCombo.addActionListener(e -> applyFilter());

        lblRowCount = new JLabel("Students: 0");
        lblRowCount.setForeground(new Color(166, 227, 161));
        lblRowCount.setFont(new Font("Arial", Font.BOLD, 12));

        topBar.add(lblSearch);
        topBar.add(txtSearch);
        topBar.add(makeButton("Search", hexColor("89b4fa"), e -> searchStudent()));
        topBar.add(makeButton("Show All", hexColor("a6e3a1"), e -> {
            txtSearch.setText("");
            filterCombo.setSelectedIndex(0);
            loadAllStudents();
        }));
        topBar.add(new JSeparator(SwingConstants.VERTICAL));
        topBar.add(new JLabel("Filter:") {
            {
                setForeground(new Color(205, 214, 244));
            }
        });
        topBar.add(filterCombo);
        topBar.add(new JSeparator(SwingConstants.VERTICAL));
        topBar.add(makeButton("Import CSV", hexColor("fab387"), e -> importCSV()));
        topBar.add(makeButton("Export CSV", hexColor("a6e3a1"), e -> exportCSV()));
        topBar.add(makeButton("Export Report (.txt)", hexColor("89b4fa"), e -> exportTextReport()));
        topBar.add(lblRowCount);

        panel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Roll No", "Name",
            subjectNames[0], subjectNames[1], subjectNames[2], subjectNames[3], subjectNames[4],
            "Total", "%", "Grade"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setBackground(new Color(49, 50, 68));
        table.setForeground(new Color(205, 214, 244));
        table.setGridColor(new Color(69, 71, 90));
        table.getTableHeader().setBackground(new Color(30, 30, 46));
        table.getTableHeader().setForeground(new Color(137, 180, 250));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setRowHeight(26);
        table.setSelectionBackground(new Color(137, 180, 250));
        table.setSelectionForeground(new Color(30, 30, 46));
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                fillFormFromTable();
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ─── HELPERS ─────────────────────────────────────────────────
    Color hexColor(String hex) {
        return Color.decode("#" + hex);
    }

    void addSectionLabel(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(137, 180, 250));
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
    }

    JTextField addField(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(166, 173, 200));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        JTextField field = new JTextField();
        styleTextField(field);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(field);
        panel.add(Box.createVerticalStrut(3));
        return field;
    }

    void styleTextField(JTextField field) {
        field.setBackground(new Color(49, 50, 68));
        field.setForeground(new Color(205, 214, 244));
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
    }

    JLabel addResultLabel(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(166, 227, 161));
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(2));
        return lbl;
    }

    JButton makeButton(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(new Color(30, 30, 46));
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);
        return btn;
    }

    // ─── VALIDATE MARKS ──────────────────────────────────────────
    double parseMarks(String val, String fieldName) throws Exception {
        double marks;
        try {
            marks = Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            throw new Exception(fieldName + " must be a number.");
        }
        if (marks < 0 || marks > 100) {
            throw new Exception(fieldName + " must be between 0 and 100.");
        }
        return marks;
    }

    // ─── ADD STUDENT ─────────────────────────────────────────────
    void addStudent() {
        try {
            String roll = txtRoll.getText().trim();
            String name = txtName.getText().trim();
            if (roll.isEmpty()) {
                throw new Exception("Roll Number cannot be empty.");
            }
            if (name.isEmpty()) {
                throw new Exception("Student Name cannot be empty.");
            }

            double s1 = parseMarks(txtS1.getText(), subjectNames[0]);
            double s2 = parseMarks(txtS2.getText(), subjectNames[1]);
            double s3 = parseMarks(txtS3.getText(), subjectNames[2]);
            double s4 = parseMarks(txtS4.getText(), subjectNames[3]);
            double s5 = parseMarks(txtS5.getText(), subjectNames[4]);

            Student s = new Student(roll, name, s1, s2, s3, s4, s5);
            lblTotal.setText("Total:      " + s.getTotal());
            lblPercent.setText("Percentage: " + String.format("%.2f", s.getPercentage()) + "%");
            lblGrade.setText("Grade:      " + s.getGrade());

            service.addStudent(s);
            JOptionPane.showMessageDialog(this, "Student added successfully!");
            loadAllStudents();
            updateStats();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, StudentService.friendlyMessage(e),
                    "Could not add student", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── UPDATE STUDENT ──────────────────────────────────────────
    void updateStudent() {
        try {
            String roll = txtRoll.getText().trim();
            String name = txtName.getText().trim();
            if (roll.isEmpty()) {
                throw new Exception("Roll Number cannot be empty.");
            }
            if (name.isEmpty()) {
                throw new Exception("Student Name cannot be empty.");
            }

            double s1 = parseMarks(txtS1.getText(), subjectNames[0]);
            double s2 = parseMarks(txtS2.getText(), subjectNames[1]);
            double s3 = parseMarks(txtS3.getText(), subjectNames[2]);
            double s4 = parseMarks(txtS4.getText(), subjectNames[3]);
            double s5 = parseMarks(txtS5.getText(), subjectNames[4]);

            Student s = new Student(roll, name, s1, s2, s3, s4, s5);
            if (service.updateStudent(s)) {
                JOptionPane.showMessageDialog(this, "Student updated successfully!");
                loadAllStudents();
                updateStats();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "No student found with Roll No: " + roll);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, StudentService.friendlyMessage(e),
                    "Could not update student", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── DELETE STUDENT ──────────────────────────────────────────
    void deleteStudent() {
        String roll = txtRoll.getText().trim();
        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a student from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student: " + roll + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (service.deleteStudent(roll)) {
                    JOptionPane.showMessageDialog(this, "Student deleted successfully!");
                    loadAllStudents();
                    updateStats();
                    clearFields();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, StudentService.friendlyMessage(e));
            }
        }
    }

    // ─── LOAD ALL ────────────────────────────────────────────────
    void loadAllStudents() {
        tableModel.setRowCount(0);
        try {
            List<Student> students = service.getAllStudents();
            for (Student s : students) {
                addRowToTable(s);
            }
            lblRowCount.setText("Students: " + students.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ─── SEARCH ──────────────────────────────────────────────────
    void searchStudent() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadAllStudents();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Student> students = service.searchStudents(keyword);
            for (Student s : students) {
                addRowToTable(s);
            }
            lblRowCount.setText("Results: " + students.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, StudentService.friendlyMessage(e));
        }
    }

    // ─── FILTER ──────────────────────────────────────────────────
    void applyFilter() {
        String filter = (String) filterCombo.getSelectedItem();
        tableModel.setRowCount(0);
        try {
            List<Student> students;
            if ("Pass".equals(filter)) {
                students = service.getPassStudents();
            } else if ("Fail".equals(filter)) {
                students = service.getFailStudents();
            } else {
                students = service.getAllStudents();
            }
            for (Student s : students) {
                addRowToTable(s);
            }
            lblRowCount.setText(filter + ": " + students.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ─── UPDATE STATS ────────────────────────────────────────────
    void updateStats() {
        try {
            int[] stats = service.getStats();
            int total = stats[0];
            int passed = stats[1];
            int avg = stats[3];
            double passPercent = total > 0 ? (passed * 100.0 / total) : 0;
            lblTotalStudents.setText(String.valueOf(total));
            lblPassPercent.setText(String.format("%.1f%%", passPercent));
            lblAvgScore.setText(avg + "%");
        } catch (Exception e) {
            System.out.println("Stats error: " + e.getMessage());
        }
    }

    // ─── IMPORT CSV ──────────────────────────────────────────────
    void importCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            int[] result = service.importCSV(fc.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Import Complete!\nImported: " + result[0] + "\nSkipped: " + result[1]
                    + "\n\nCSV format: roll_number, name, s1, s2, s3, s4, s5");
            loadAllStudents();
            updateStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Import error: " + e.getMessage());
        }
    }

    // ─── EXPORT CSV ──────────────────────────────────────────────
    void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("student_results.csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".csv")) {
                path += ".csv";
            }
            service.exportCSV(path, subjectNames);
            JOptionPane.showMessageDialog(this, "CSV exported to:\n" + path);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export error: " + e.getMessage());
        }
    }

    void exportTextReport() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("student_results.txt"));
        fc.setFileFilter(new FileNameExtensionFilter("Text Report (.txt)", "txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".txt")) {
                path += ".txt";
            }
            ReportExporter.exportTextReport(path, subjectNames,
                    service.getAllStudents(), service.getStats());
            JOptionPane.showMessageDialog(this,
                    "Report saved to:\n" + path + "\n\nOpen and press Ctrl+P to print!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, StudentService.friendlyMessage(e),
                    "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── SAVE SUBJECT NAMES ──────────────────────────────────────
    void saveSubjectNames() {
        try {
            for (int i = 0; i < 5; i++) {
                String n = subjectNameFields[i].getText().trim();
                subjectNames[i] = n.isEmpty() ? "Subject " + (i + 1) : n;
            }
            service.saveSubjectNames(subjectNames);
            updateTableHeaders();
            JOptionPane.showMessageDialog(this, "Subject names saved!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    void updateTableHeaders() {
        tableModel.setColumnIdentifiers(new Object[]{
            "ID", "Roll No", "Name",
            subjectNames[0], subjectNames[1], subjectNames[2],
            subjectNames[3], subjectNames[4],
            "Total", "%", "Grade"
        });
    }

    // ─── ADD ROW TO TABLE ────────────────────────────────────────
    void addRowToTable(Student s) {
        tableModel.addRow(new Object[]{
            s.getId(), s.getRollNumber(), s.getName(),
            s.getSubject1(), s.getSubject2(), s.getSubject3(),
            s.getSubject4(), s.getSubject5(),
            s.getTotal(), String.format("%.2f", s.getPercentage()), s.getGrade()
        });
    }

    // ─── FILL FORM FROM TABLE ────────────────────────────────────
    void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        txtRoll.setText(tableModel.getValueAt(row, 1).toString());
        txtName.setText(tableModel.getValueAt(row, 2).toString());
        txtS1.setText(tableModel.getValueAt(row, 3).toString());
        txtS2.setText(tableModel.getValueAt(row, 4).toString());
        txtS3.setText(tableModel.getValueAt(row, 5).toString());
        txtS4.setText(tableModel.getValueAt(row, 6).toString());
        txtS5.setText(tableModel.getValueAt(row, 7).toString());
        lblTotal.setText("Total:      " + tableModel.getValueAt(row, 8));
        lblPercent.setText("Percentage: " + tableModel.getValueAt(row, 9) + "%");
        lblGrade.setText("Grade:      " + tableModel.getValueAt(row, 10));
    }

    // ─── DOCUMENT FILTER FOR MARKS FIELDS ───────────────────────
    void applyMarksFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (isValid(fb.getDocument().getText(0, fb.getDocument().getLength()) + string)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr)
                    throws BadLocationException {
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String result = current.substring(0, offset) + string + current.substring(offset + length);
                if (isValid(result)) {
                    super.replace(fb, offset, length, string, attr);
                }
            }

            boolean isValid(String text) {
                if (text.isEmpty()) {
                    return true;
                }
                try {
                    double val = Double.parseDouble(text);
                    return val >= 0 && val <= 100;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
    }

    // ─── CLEAR FIELDS ────────────────────────────────────────────
    void clearFields() {
        txtRoll.setText("");
        txtName.setText("");
        txtS1.setText("");
        txtS2.setText("");
        txtS3.setText("");
        txtS4.setText("");
        txtS5.setText("");
        lblTotal.setText("Total:      -");
        lblPercent.setText("Percentage: -");
        lblGrade.setText("Grade:      -");
        table.clearSelection();
    }
}
