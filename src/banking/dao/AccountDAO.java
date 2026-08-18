package banking.dao;

import banking.model.Account;
import banking.model.CurrentAccount;
import banking.model.SavingsAccount;
import banking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Account database operations.
 */
public class AccountDAO {

    /**
     * Creates a new Account record in the database.
     */
    public boolean createAccount(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountNumber());
            pstmt.setInt(2, account.getCustomerId());
            pstmt.setString(3, account.getAccountType());
            pstmt.setDouble(4, account.getBalance());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves an Account by Account Number.
     */
    public Account getAccountByNumber(String accountNumber) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return getAccountByNumber(accountNumber, conn);
        }
    }

    /**
     * Overloaded method to retrieve Account using an existing DB Connection (for multi-step SQL transactions).
     */
    public Account getAccountByNumber(String accountNumber, Connection conn) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractAccountFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all accounts associated with a specific customer ID.
     */
    public List<Account> getAccountsByCustomerId(int customerId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(extractAccountFromResultSet(rs));
                }
            }
        }
        return accounts;
    }

    /**
     * Retrieves all accounts in the database.
     */
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(extractAccountFromResultSet(rs));
            }
        }
        return accounts;
    }

    /**
     * Updates balance of an account within a database transaction.
     */
    public boolean updateBalance(String accountNumber, double newBalance, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountNumber);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Helper method to instantiate SavingsAccount or CurrentAccount based on account_type column
    private Account extractAccountFromResultSet(ResultSet rs) throws SQLException {
        String accNo = rs.getString("account_number");
        int custId = rs.getInt("customer_id");
        String type = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        Timestamp createdAt = rs.getTimestamp("created_at");

        if ("SAVINGS".equalsIgnoreCase(type)) {
            return new SavingsAccount(accNo, custId, balance, createdAt);
        } else {
            return new CurrentAccount(accNo, custId, balance, createdAt);
        }
    }
}
