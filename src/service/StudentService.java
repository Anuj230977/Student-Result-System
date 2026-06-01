package service;

import dao.StudentDAO;
import model.Student;
import java.sql.SQLException;
import java.util.List;

/**
 * Application layer between UI and database — keeps views thin.
 */
public class StudentService {

    private final StudentDAO dao = new StudentDAO();

    public void initialize() throws SQLException {
        dao.setupTables();
    }

    public boolean login(String username, String password) throws SQLException {
        return dao.login(username, password);
    }

    public void addStudent(Student student) throws SQLException {
        dao.addStudent(student);
    }

    public boolean updateStudent(Student student) throws SQLException {
        return dao.updateStudent(student);
    }

    public boolean deleteStudent(String rollNumber) throws SQLException {
        return dao.deleteStudent(rollNumber);
    }

    public List<Student> getAllStudents() throws SQLException {
        return dao.getAllStudents();
    }

    public List<Student> getPassStudents() throws SQLException {
        return dao.getPassStudents();
    }

    public List<Student> getFailStudents() throws SQLException {
        return dao.getFailStudents();
    }

    public List<Student> searchStudents(String keyword) throws SQLException {
        return dao.searchStudents(keyword);
    }

    public int[] getStats() throws SQLException {
        return dao.getStats();
    }

    public String[] getSubjectNames() throws SQLException {
        return dao.getSubjectNames();
    }

    public void saveSubjectNames(String[] names) throws SQLException {
        dao.saveSubjectNames(names);
    }

    public int[] importCSV(String filePath) throws Exception {
        return dao.importCSV(filePath);
    }

    public void exportCSV(String filePath, String[] subjectNames) throws Exception {
        dao.exportCSV(filePath, subjectNames);
    }

    /** User-friendly message for dialogs; never exposes raw SQL details. */
    public static String friendlyMessage(Exception e) {
        if (e instanceof SQLException sql) {
            switch (sql.getErrorCode()) {
                case 1062:
                    return "This roll number already exists. Use Update or choose another roll number.";
                case 1045:
                    return "Database login failed. Check db.user and db.password in config.properties.";
                case 1049:
                    return "Database not found. Create student_result_db in MySQL first.";
                case 0:
                    if (sql.getMessage() != null && sql.getMessage().contains("Communications link failure")) {
                        return "Cannot connect to MySQL. Is the server running?";
                    }
                    break;
                default:
                    break;
            }
        }
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return "An unexpected error occurred.";
        }
        if (msg.length() > 200) {
            return msg.substring(0, 197) + "...";
        }
        return msg;
    }
}
