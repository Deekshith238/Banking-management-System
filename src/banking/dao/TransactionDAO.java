package banking.dao;

import banking.model.Transaction;
import banking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Transaction log persistence.
 */
public class TransactionDAO {

    /**
     * Records a new transaction into the database using an active DB Connection.
     */
    public boolean recordTransaction(Transaction tx, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount, balance_after, target_account_number, remarks) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tx.getAccountNumber());
            pstmt.setString(2, tx.getTransactionType());
            pstmt.setDouble(3, tx.getAmount());
            pstmt.setDouble(4, tx.getBalanceAfter());
            if (tx.getTargetAccountNumber() != null && !tx.getTargetAccountNumber().isEmpty()) {
                pstmt.setString(5, tx.getTargetAccountNumber());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }
            pstmt.setString(6, tx.getRemarks());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        tx.setTransactionId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Records a transaction standalone.
     */
    public boolean recordTransaction(Transaction tx) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return recordTransaction(tx, conn);
        }
    }

    /**
     * Retrieves statement / transaction history for a specific account.
     */
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransactionFromResultSet(rs));
                }
            }
        }
        return transactions;
    }

    /**
     * Retrieves all system transactions.
     */
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        }
        return transactions;
    }

    // Helper method to extract Transaction entity from ResultSet
    private Transaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getString("account_number"),
                rs.getString("transaction_type"),
                rs.getDouble("amount"),
                rs.getDouble("balance_after"),
                rs.getString("target_account_number"),
                rs.getString("remarks"),
                rs.getTimestamp("timestamp")
        );
    }
}
