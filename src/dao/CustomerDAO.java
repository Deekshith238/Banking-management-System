package dao;

import model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CustomerDAO Class
 * Data Access Object for performing database operations on 'customers' table.
 * Demonstrates JDBC Concepts:
 * 1. PreparedStatement (prevents SQL Injection)
 * 2. Try-With-Resources (automatic closing of Connection, Statement, ResultSet)
 * 3. ResultSet navigation
 */
public class CustomerDAO {

    /**
     * Register a new Customer in the database.
     * @param customer Customer entity containing registration details
     * @return true if registration succeeded, false otherwise
     */
    public boolean registerCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, email, phone, address, password) VALUES (?, ?, ?, ?, ?)";

        // Try-with-resources automatically closes Connection and PreparedStatement
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getPassword());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("❌ Database Error during Customer Registration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate customer login credentials.
     * @param email Customer email
     * @param password Customer password
     * @return Customer object if credentials are valid, null otherwise
     */
    public Customer loginCustomer(String email, String password) {
        String sql = "SELECT * FROM customers WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("password")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error during Customer Login: " + e.getMessage());
        }

        return null; // Return null if authentication fails
    }

    /**
     * Fetch customer details by Customer ID.
     * @param customerId Customer ID
     * @return Customer object if found, null otherwise
     */
    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("password")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error fetching Customer Profile: " + e.getMessage());
        }

        return null;
    }
}
