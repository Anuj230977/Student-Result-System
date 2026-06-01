package dao;

import model.Student;
import util.DBConnection;
import util.PasswordUtil;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private static final String DEFAULT_ADMIN_USER = "admin";
    private static final String DEFAULT_ADMIN_PASS = "admin123";

    public void setupTables() throws SQLException {
        Connection con = DBConnection.getConnection();

        try (Statement st = con.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS students ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "roll_number VARCHAR(20) UNIQUE NOT NULL,"
                    + "name VARCHAR(100) NOT NULL,"
                    + "subject1 FLOAT, subject2 FLOAT, subject3 FLOAT, subject4 FLOAT, subject5 FLOAT,"
                    + "total FLOAT, percentage FLOAT, grade VARCHAR(5),"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS subject_config ("
                    + "id INT PRIMARY KEY,"
                    + "subject_name VARCHAR(100) NOT NULL)");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "username VARCHAR(50) UNIQUE NOT NULL,"
                    + "password VARCHAR(255) NOT NULL)");
        }

        widenPasswordColumn(con);
        seedSubjectNames(con);
        seedAdminUser(con);
        migratePlaintextPasswords(con);
    }

    private void widenPasswordColumn(Connection con) {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE users MODIFY password VARCHAR(255) NOT NULL");
        } catch (SQLException ignored) {
            // Column may already be wide enough
        }
    }

    private void seedSubjectNames(Connection con) throws SQLException {
        int count;
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM subject_config")) {
            rs.next();
            count = rs.getInt(1);
        }
        if (count > 0) {
            return;
        }
        for (int i = 1; i <= 5; i++) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO subject_config (id, subject_name) VALUES (?, ?)")) {
                ps.setInt(1, i);
                ps.setString(2, "Subject " + i);
                ps.executeUpdate();
            }
        }
    }

    private void seedAdminUser(Connection con) throws SQLException {
        int count;
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            count = rs.getInt(1);
        }
        if (count > 0) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, DEFAULT_ADMIN_USER);
            ps.setString(2, PasswordUtil.hashPassword(DEFAULT_ADMIN_PASS));
            ps.executeUpdate();
        }
    }

    private void migratePlaintextPasswords(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT id, username, password FROM users")) {
            while (rs.next()) {
                String stored = rs.getString("password");
                if (PasswordUtil.isHashed(stored)) {
                    continue;
                }
                int id = rs.getInt("id");
                String hashed = PasswordUtil.hashPassword(stored);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE users SET password=? WHERE id=?")) {
                    ps.setString(1, hashed);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    public boolean login(String username, String password) throws SQLException {
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT password FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String stored = rs.getString("password");
                if (PasswordUtil.verify(password, stored)) {
                    return true;
                }
                // Legacy plain-text row (before migration) — verify and upgrade
                if (!PasswordUtil.isHashed(stored) && stored.equals(password)) {
                    updatePasswordHash(con, username, password);
                    return true;
                }
                return false;
            }
        }
    }

    private void updatePasswordHash(Connection con, String username, String plainPassword)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE users SET password=? WHERE username=?")) {
            ps.setString(1, PasswordUtil.hashPassword(plainPassword));
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    public void addStudent(Student s) throws SQLException {
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO students (roll_number, name, subject1, subject2, subject3, "
                + "subject4, subject5, total, percentage, grade) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, s.getRollNumber());
            ps.setString(2, s.getName());
            ps.setDouble(3, s.getSubject1());
            ps.setDouble(4, s.getSubject2());
            ps.setDouble(5, s.getSubject3());
            ps.setDouble(6, s.getSubject4());
            ps.setDouble(7, s.getSubject5());
            ps.setDouble(8, s.getTotal());
            ps.setDouble(9, s.getPercentage());
            ps.setString(10, s.getGrade());
            ps.executeUpdate();
        }
    }

    public boolean updateStudent(Student s) throws SQLException {
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE students SET name=?, subject1=?, subject2=?, subject3=?, "
                + "subject4=?, subject5=?, total=?, percentage=?, grade=? WHERE roll_number=?")) {
            ps.setString(1, s.getName());
            ps.setDouble(2, s.getSubject1());
            ps.setDouble(3, s.getSubject2());
            ps.setDouble(4, s.getSubject3());
            ps.setDouble(5, s.getSubject4());
            ps.setDouble(6, s.getSubject5());
            ps.setDouble(7, s.getTotal());
            ps.setDouble(8, s.getPercentage());
            ps.setString(9, s.getGrade());
            ps.setString(10, s.getRollNumber());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(String rollNumber) throws SQLException {
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM students WHERE roll_number=?")) {
            ps.setString(1, rollNumber);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        return getStudentsByQuery("SELECT * FROM students ORDER BY id ASC");
    }

    public List<Student> getPassStudents() throws SQLException {
        return getStudentsByQuery("SELECT * FROM students WHERE grade != 'F' ORDER BY id ASC");
    }

    public List<Student> getFailStudents() throws SQLException {
        return getStudentsByQuery("SELECT * FROM students WHERE grade = 'F' ORDER BY id ASC");
    }

    public List<Student> searchStudents(String keyword) throws SQLException {
        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM students WHERE roll_number LIKE ? OR name LIKE ? ORDER BY id ASC")) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            return mapResultSet(ps.executeQuery());
        }
    }

    public int[] getStats() throws SQLException {
        Connection con = DBConnection.getConnection();
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) as total, "
                        + "SUM(CASE WHEN grade != 'F' THEN 1 ELSE 0 END) as passed, "
                        + "AVG(percentage) as avg_percent FROM students")) {
            rs.next();
            int total = rs.getInt("total");
            int passed = rs.getInt("passed");
            int avg = (int) rs.getDouble("avg_percent");
            return new int[]{total, passed, total - passed, avg};
        }
    }

    public String[] getSubjectNames() throws SQLException {
        Connection con = DBConnection.getConnection();
        String[] names = new String[5];
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT subject_name FROM subject_config ORDER BY id")) {
            int i = 0;
            while (rs.next() && i < 5) {
                names[i++] = rs.getString("subject_name");
            }
        }
        return names;
    }

    public void saveSubjectNames(String[] names) throws SQLException {
        Connection con = DBConnection.getConnection();
        for (int i = 0; i < 5; i++) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE subject_config SET subject_name=? WHERE id=?")) {
                ps.setString(1, names[i]);
                ps.setInt(2, i + 1);
                ps.executeUpdate();
            }
        }
    }

    public int[] importCSV(String filePath) throws Exception {
        int success = 0;
        int failed = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split(",");
                if (p.length < 7) {
                    failed++;
                    continue;
                }
                try {
                    Student s = new Student(
                            p[0].trim(), p[1].trim(),
                            Double.parseDouble(p[2].trim()),
                            Double.parseDouble(p[3].trim()),
                            Double.parseDouble(p[4].trim()),
                            Double.parseDouble(p[5].trim()),
                            Double.parseDouble(p[6].trim()));
                    Connection con = DBConnection.getConnection();
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT IGNORE INTO students (roll_number, name, subject1, subject2, "
                            + "subject3, subject4, subject5, total, percentage, grade) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                        ps.setString(1, s.getRollNumber());
                        ps.setString(2, s.getName());
                        ps.setDouble(3, s.getSubject1());
                        ps.setDouble(4, s.getSubject2());
                        ps.setDouble(5, s.getSubject3());
                        ps.setDouble(6, s.getSubject4());
                        ps.setDouble(7, s.getSubject5());
                        ps.setDouble(8, s.getTotal());
                        ps.setDouble(9, s.getPercentage());
                        ps.setString(10, s.getGrade());
                        if (ps.executeUpdate() > 0) {
                            success++;
                        } else {
                            failed++;
                        }
                    }
                } catch (Exception e) {
                    failed++;
                }
            }
        }
        return new int[]{success, failed};
    }

    public void exportCSV(String filePath, String[] subjectNames) throws Exception {
        Connection con = DBConnection.getConnection();
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM students ORDER BY id ASC");
                PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("Roll Number,Name," + String.join(",", subjectNames)
                    + ",Total,Percentage,Grade");
            while (rs.next()) {
                pw.println(
                        rs.getString("roll_number") + "," + rs.getString("name") + ","
                        + rs.getDouble("subject1") + "," + rs.getDouble("subject2") + ","
                        + rs.getDouble("subject3") + "," + rs.getDouble("subject4") + ","
                        + rs.getDouble("subject5") + "," + rs.getDouble("total") + ","
                        + String.format("%.2f", rs.getDouble("percentage")) + ","
                        + rs.getString("grade"));
            }
        }
    }

    private List<Student> getStudentsByQuery(String query) throws SQLException {
        Connection con = DBConnection.getConnection();
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            return mapResultSet(rs);
        }
    }

    private List<Student> mapResultSet(ResultSet rs) throws SQLException {
        List<Student> list = new ArrayList<>();
        while (rs.next()) {
            Student s = new Student(
                    rs.getString("roll_number"), rs.getString("name"),
                    rs.getDouble("subject1"), rs.getDouble("subject2"),
                    rs.getDouble("subject3"), rs.getDouble("subject4"),
                    rs.getDouble("subject5"));
            s.setId(rs.getInt("id"));
            list.add(s);
        }
        return list;
    }
}
