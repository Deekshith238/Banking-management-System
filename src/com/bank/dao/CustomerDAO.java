package com.bank.dao;

import com.bank.model.Admin;
import com.bank.model.Customer;
import com.bank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public boolean registerCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, email, phone, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getPassword());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Customer registration failed: " + e.getMessage());
            return false;
        }
    }

    public Customer loginCustomer(String email, String password) {
        String sql = "SELECT customer_id, name, email, phone, password FROM customers WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("password")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Customer login query failed: " + e.getMessage());
        }
        return null;
    }

    public Admin loginAdmin(String username, String password) {
        String sql = "SELECT admin_id, username, password FROM admins WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("admin_id"),
                        rs.getString("username"),
                        rs.getString("password")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Admin login query failed: " + e.getMessage());
        }
        return null;
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT customer_id, name, email, phone, password FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("password")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to fetch customer by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, password FROM customers ORDER BY customer_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[DAO ERROR] Failed to fetch all customers: " + e.getMessage());
        }
        return list;
    }
}
