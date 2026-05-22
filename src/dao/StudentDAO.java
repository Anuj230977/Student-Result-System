package dao;

import model.Student;
import util.DBConnection;
import java.sql.*;
import java.util.*;

public class StudentDAO {

    // ─── SETUP TABLES ────────────────────────────────────────────
    public void setupTables() throws SQLException {
        Connection con = DBConnection.getConnection();

        con.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS students (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "roll_number VARCHAR(20) UNIQUE NOT NULL," +
            "name VARCHAR(100) NOT NULL," +
            "subject1 FLOAT, subject2 FLOAT, subject3 FLOAT, subject4 FLOAT, subject5 FLOAT," +
            "total FLOAT, percentage FLOAT, grade VARCHAR(5)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );

        con.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS subject_config (" +
            "id INT PRIMARY KEY," +
            "subject_name VARCHAR(100) NOT NULL)"
        );

        con.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50) UNIQUE NOT NULL," +
            "password VARCHAR(50) NOT NULL)"
        );

        // Default subject names
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT COUNT(*) FROM subject_config");
        rs.next();
        if (rs.getInt(1) == 0) {
            for (int i = 1; i <= 5; i++) {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO subject_config (id, subject_name) VALUES (?, ?)");
                ps.setInt(1, i);
                ps.setString(2, "Subject " + i);
                ps.executeUpdate();
            }
        }

        // Default admin user
        ResultSet rs2 = con.createStatement().executeQuery(
            "SELECT COUNT(*) FROM users");
        rs2.next();
        if (rs2.getInt(1) == 0) {
            con.createStatement().executeUpdate(
                "INSERT INTO users (username, password) VALUES ('admin', 'admin123')");
        }
    }

    // ─── LOGIN ───────────────────────────────────────────────────
    public boolean login(String username, String password) throws SQLException {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM users WHERE username=? AND password=?");
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    // ─── ADD STUDENT ─────────────────────────────────────────────
    public boolean addStudent(Student s) throws SQLException {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO students (roll_number, name, subject1, subject2, subject3, " +
            "subject4, subject5, total, percentage, grade) VALUES (?,?,?,?,?,?,?,?,?,?)");
        ps.setString(1, s.getRollNumber());
        ps.setString(2, s.getName());
        ps.setDouble(3, s.getSubject1()); ps.setDouble(4, s.getSubject2());
        ps.setDouble(5, s.getSubject3()); ps.setDouble(6, s.getSubject4());
        ps.setDouble(7, s.getSubject5()); ps.setDouble(8, s.getTotal());
        ps.setDouble(9, s.getPercentage()); ps.setString(10, s.getGrade());
        return ps.executeUpdate() > 0;
    }

    // ─── UPDATE STUDENT ──────────────────────────────────────────
    public boolean updateStudent(Student s) throws SQLException {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "UPDATE students SET name=?, subject1=?, subject2=?, subject3=?, " +
            "subject4=?, subject5=?, total=?, percentage=?, grade=? WHERE roll_number=?");
        ps.setString(1, s.getName());
        ps.setDouble(2, s.getSubject1()); ps.setDouble(3, s.getSubject2());
        ps.setDouble(4, s.getSubject3()); ps.setDouble(5, s.getSubject4());
        ps.setDouble(6, s.getSubject5()); ps.setDouble(7, s.getTotal());
        ps.setDouble(8, s.getPercentage()); ps.setString(9, s.getGrade());
        ps.setString(10, s.getRollNumber());
        return ps.executeUpdate() > 0;
    }

    // ─── DELETE STUDENT ──────────────────────────────────────────
    public boolean deleteStudent(String rollNumber) throws SQLException {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM students WHERE roll_number=?");
        ps.setString(1, rollNumber);
        return ps.executeUpdate() > 0;
    }

    // ─── GET ALL STUDENTS ────────────────────────────────────────
    public List<Student> getAllStudents() throws SQLException {
        return getStudentsByQuery("SELECT * FROM students ORDER BY id ASC");
    }

    // ─── GET PASS STUDENTS ───────────────────────────────────────
    public List<Student> getPassStudents() throws SQLException {
        return getStudentsByQuery("SELECT * FROM students WHERE grade != 'F' ORDER BY id ASC");
    }

    // ─── GET FAIL STUDENTS ───────────────────────────────────────
    public List<Student> getFailStudents() throws SQLException {
        return getStudentsByQuery("SELECT * FROM students WHERE grade = 'F' ORDER BY id ASC");
    }

    // ─── SEARCH STUDENTS ─────────────────────────────────────────
    public List<Student> searchStudents(String keyword) throws SQLException {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM students WHERE roll_number LIKE ? OR name LIKE ? ORDER BY id ASC");
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");
        return mapResultSet(ps.executeQuery());
    }

    // ─── GET STATS ───────────────────────────────────────────────
    public int[] getStats() throws SQLException {
        Connection con = DBConnection.getConnection();
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT COUNT(*) as total, " +
            "SUM(CASE WHEN grade != 'F' THEN 1 ELSE 0 END) as passed, " +
            "AVG(percentage) as avg_percent FROM students");
        rs.next();
        int total   = rs.getInt("total");
        int passed  = rs.getInt("passed");
        int avg     = (int) rs.getDouble("avg_percent");
        return new int[]{total, passed, total - passed, avg};
    }

    // ─── SUBJECT NAMES ───────────────────────────────────────────
    public String[] getSubjectNames() throws SQLException {
        Connection con = DBConnection.getConnection();
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT subject_name FROM subject_config ORDER BY id");
        String[] names = new String[5];
        int i = 0;
        while (rs.next() && i < 5) names[i++] = rs.getString("subject_name");
        return names;
    }

    public void saveSubjectNames(String[] names) throws SQLException {
        Connection con = DBConnection.getConnection();
        for (int i = 0; i < 5; i++) {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE subject_config SET subject_name=? WHERE id=?");
            ps.setString(1, names[i]);
            ps.setInt(2, i + 1);
            ps.executeUpdate();
        }
    }

    // ─── IMPORT CSV ──────────────────────────────────────────────
    public int[] importCSV(String filePath) throws Exception {
        int success = 0, failed = 0;
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filePath));
        br.readLine(); // skip header
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split(",");
            if (p.length < 7) { failed++; continue; }
            try {
                Student s = new Student(
                    p[0].trim(), p[1].trim(),
                    Double.parseDouble(p[2].trim()), Double.parseDouble(p[3].trim()),
                    Double.parseDouble(p[4].trim()), Double.parseDouble(p[5].trim()),
                    Double.parseDouble(p[6].trim())
                );
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "INSERT IGNORE INTO students (roll_number, name, subject1, subject2, " +
                    "subject3, subject4, subject5, total, percentage, grade) VALUES (?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1, s.getRollNumber()); ps.setString(2, s.getName());
                ps.setDouble(3, s.getSubject1());   ps.setDouble(4, s.getSubject2());
                ps.setDouble(5, s.getSubject3());   ps.setDouble(6, s.getSubject4());
                ps.setDouble(7, s.getSubject5());   ps.setDouble(8, s.getTotal());
                ps.setDouble(9, s.getPercentage()); ps.setString(10, s.getGrade());
                int result = ps.executeUpdate();
                if (result > 0) success++; else failed++;
            } catch (Exception e) { failed++; }
        }
        br.close();
        return new int[]{success, failed};
    }

    // ─── EXPORT CSV ──────────────────────────────────────────────
    public void exportCSV(String filePath, String[] subjectNames) throws Exception {
        Connection con = DBConnection.getConnection();
        ResultSet rs = con.createStatement().executeQuery(
            "SELECT * FROM students ORDER BY id ASC");
        java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath));
        pw.println("Roll Number,Name," + String.join(",", subjectNames) + ",Total,Percentage,Grade");
        while (rs.next()) {
            pw.println(
                rs.getString("roll_number") + "," + rs.getString("name") + "," +
                rs.getDouble("subject1") + "," + rs.getDouble("subject2") + "," +
                rs.getDouble("subject3") + "," + rs.getDouble("subject4") + "," +
                rs.getDouble("subject5") + "," + rs.getDouble("total") + "," +
                String.format("%.2f", rs.getDouble("percentage")) + "," +
                rs.getString("grade"));
        }
        pw.close();
    }

    // ─── HELPERS ─────────────────────────────────────────────────
    private List<Student> getStudentsByQuery(String query) throws SQLException {
        return mapResultSet(DBConnection.getConnection().createStatement().executeQuery(query));
    }

    private List<Student> mapResultSet(ResultSet rs) throws SQLException {
        List<Student> list = new ArrayList<>();
        while (rs.next()) {
            Student s = new Student(
                rs.getString("roll_number"), rs.getString("name"),
                rs.getDouble("subject1"), rs.getDouble("subject2"),
                rs.getDouble("subject3"), rs.getDouble("subject4"), rs.getDouble("subject5")
            );
            s.setId(rs.getInt("id"));
            list.add(s);
        }
        return list;
    }
}