package com.bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to manage database connections using JDBC with embedded H2 Database.
 * No external MySQL setup or password required.
 */
public class DBConnection {

    private static final String DRIVER = "org.h2.Driver";
    private static final String URL = "jdbc:h2:./database/banking_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    private static Connection connection = null;
    private static boolean initialized = false;

    private DBConnection() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER);
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                if (!initialized) {
                    initSchema(connection);
                    initialized = true;
                    System.out.println("[DB SUCCESS] Connected to Embedded Database ('banking_db') successfully!\n");
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB ERROR] H2 Database Driver not found in classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("\n[DB ERROR] Could not connect to Database: " + e.getMessage());
        }
        return connection;
    }

    private static void initSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS customers ("
                    + "customer_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL UNIQUE, "
                    + "phone VARCHAR(20) NOT NULL, "
                    + "password VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "account_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "customer_id INT NOT NULL, "
                    + "account_number BIGINT NOT NULL UNIQUE, "
                    + "account_type VARCHAR(20) NOT NULL, "
                    + "balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00, "
                    + "CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE, "
                    + "CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY')), "
                    + "CONSTRAINT chk_positive_balance CHECK (balance >= 0.00))");

            stmt.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "transaction_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "account_id INT NOT NULL, "
                    + "transaction_type VARCHAR(20) NOT NULL, "
                    + "amount DECIMAL(15, 2) NOT NULL, "
                    + "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE, "
                    + "CONSTRAINT chk_tx_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT')), "
                    + "CONSTRAINT chk_tx_amount CHECK (amount > 0.00))");

            stmt.execute("CREATE TABLE IF NOT EXISTS admins ("
                    + "admin_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL)");

            // Seed default admin if table is empty
            ResultSet rsAdmin = stmt.executeQuery("SELECT COUNT(*) FROM admins");
            if (rsAdmin.next() && rsAdmin.getInt(1) == 0) {
                stmt.execute("INSERT INTO admins (username, password) VALUES ('admin', 'admin123')");
            }

            // Seed default customer & account if empty
            ResultSet rsCust = stmt.executeQuery("SELECT COUNT(*) FROM customers");
            if (rsCust.next() && rsCust.getInt(1) == 0) {
                stmt.execute("INSERT INTO customers (customer_id, name, email, phone, password) "
                        + "VALUES (1, 'Rahul Sharma', 'rahul@example.com', '9876543210', 'pass123')");
                stmt.execute("INSERT INTO accounts (account_id, customer_id, account_number, account_type, balance) "
                        + "VALUES (1, 1, 100001, 'SAVINGS', 5000.00)");
                stmt.execute("INSERT INTO transactions (account_id, transaction_type, amount) "
                        + "VALUES (1, 'DEPOSIT', 5000.00)");
            }
        } catch (SQLException e) {
            System.err.println("[DB INIT ERROR] Failed to initialize database schema: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error closing connection: " + e.getMessage());
        }
    }
}
