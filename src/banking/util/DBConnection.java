package banking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for Database Connection handling using JDBC.
 */
public class DBConnection {
    // Default MySQL Database Configuration
    private static String URL = "jdbc:mysql://localhost:3306/banking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String USER = "root";
    private static String PASSWORD = "password";

    static {
        try {
            // Explicitly load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Warning: MySQL JDBC Driver (mysql-connector-j) not found in classpath.");
        }
    }

    /**
     * Obtains a new database connection.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Sets custom database connection credentials at runtime if needed.
     */
    public static void setCredentials(String dbUrl, String user, String password) {
        URL = dbUrl;
        USER = user;
        PASSWORD = password;
    }

    /**
     * Utility method to test database connectivity.
     * @return true if database connection is successful, false otherwise.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }
}
