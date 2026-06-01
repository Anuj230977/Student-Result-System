package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        AppConfig.load();
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    AppConfig.getDbUrl(),
                    AppConfig.getDbUser(),
                    AppConfig.getDbPassword());
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
