package dao;

import model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO Class
 * Data Access Object for 'transactions' table operations.
 * Demonstrates:
 * 1. PreparedStatement for parameterized queries
 * 2. Overloaded method accepting existing Connection (for JDBC ACID transactions)
 * 3. List<Transaction> mapping from ResultSet
 */
public class TransactionDAO {

    /**
     * Add a transaction record using a new DB connection.
     * @param transaction Transaction model instance
     * @return true if inserted, false otherwise
     */
    public boolean addTransaction(Transaction transaction) {
        try (Connection conn = DBConnection.getConnection()) {
            return addTransaction(conn, transaction);
        } catch (SQLException e) {
            System.err.println("❌ Error adding transaction log: " + e.getMessage());
            return false;
        }
    }

    /**
     * Overloaded addTransaction method using an active Connection context.
     * Crucial for JDBC transaction management (commit/rollback during transfer).
     * @param conn Active Connection (with autoCommit false)
     * @param transaction Transaction model instance
     * @return true if inserted, false otherwise
     * @throws SQLException rethrown so caller transaction can handle rollback
     */
    public boolean addTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setDouble(3, transaction.getAmount());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        }
    }

    /**
     * Retrieve complete transaction history for a specific Account ID.
     * @param accountId Account ID
     * @return List of Transaction objects sorted by date descending
     */
    public List<Transaction> getTransactionHistory(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("account_id"),
                            rs.getString("transaction_type"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("transaction_date")
                    );
                    transactions.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error fetching Transaction History: " + e.getMessage());
        }

        return transactions;
    }
}
