package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection Utility Class (in dao package)
 * Manages JDBC Database Connection to MySQL.
 * Configurable DB_URL, DB_USER, and DB_PASSWORD constants.
 */
public class DBConnection {

    // Database Configuration Constants
    private static final String DB_URL = "jdbc:mysql://localhost:3306/banking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    // MySQL JDBC Driver Class Name
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Reusable static method to obtain a MySQL Database Connection.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found! Ensure mysql-connector-j.jar is in classpath.");
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
