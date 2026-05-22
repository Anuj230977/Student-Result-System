package util;

import java.sql.*;

public class DBConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/student_result_db";
    private static final String USER = "root";
    private static final String PASS = "root123"; // ← change this

    private static Connection connection = null;

    // Singleton — only one connection at a time
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}