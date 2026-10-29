package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean logTransaction(Connection conn, int accountId, String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, account_id, transaction_type, amount, transaction_date " +
                     "FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("account_id"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("transaction_date")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to fetch transactions: " + e.getMessage());
        }
        return list;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, account_id, transaction_type, amount, transaction_date " +
                     "FROM transactions ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("transaction_id"),
                    rs.getInt("account_id"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("transaction_date")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to fetch all transactions: " + e.getMessage());
        }
        return list;
    }
}
