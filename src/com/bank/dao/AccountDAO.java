package com.bank.dao;

import com.bank.model.Account;
import com.bank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public boolean createAccount(Account account) {
        String sql = "INSERT INTO accounts (customer_id, account_number, account_type, balance) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, account.getCustomerId());
            pstmt.setLong(2, account.getAccountNumber());
            pstmt.setString(3, account.getAccountType());
            pstmt.setDouble(4, account.getBalance());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to create account: " + e.getMessage());
            return false;
        }
    }

    public Account getAccountByNumber(long accountNumber) {
        String sql = "SELECT account_id, customer_id, account_number, account_type, balance FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, accountNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                        rs.getInt("account_id"),
                        rs.getInt("customer_id"),
                        rs.getLong("account_number"),
                        rs.getString("account_type"),
                        rs.getDouble("balance")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to fetch account: " + e.getMessage());
        }
        return null;
    }

    public List<Account> getAccountsByCustomerId(int customerId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT account_id, customer_id, account_number, account_type, balance FROM accounts WHERE customer_id = ? ORDER BY account_number ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Account(
                        rs.getInt("account_id"),
                        rs.getInt("customer_id"),
                        rs.getLong("account_number"),
                        rs.getString("account_type"),
                        rs.getDouble("balance")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to fetch customer accounts: " + e.getMessage());
        }
        return list;
    }

    public boolean updateBalance(Connection conn, long accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setLong(2, accountNumber);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT account_id, customer_id, account_number, account_type, balance FROM accounts ORDER BY account_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Account(
                    rs.getInt("account_id"),
                    rs.getInt("customer_id"),
                    rs.getLong("account_number"),
                    rs.getString("account_type"),
                    rs.getDouble("balance")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to fetch all accounts: " + e.getMessage());
        }
        return list;
    }
}
