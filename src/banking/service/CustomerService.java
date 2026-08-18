package banking.service;

import banking.dao.CustomerDAO;
import banking.model.Customer;
import banking.util.InputValidator;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer managing Customer business logic.
 */
public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    // Constructor injection for testing
    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    /**
     * Registers a new customer with validation.
     */
    public Customer registerCustomer(String name, String email, String phone, String address) throws IllegalArgumentException, SQLException {
        if (!InputValidator.isNonEmptyString(name)) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address format.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone number format. Must be 7-15 digits.");
        }

        // Check if email already registered
        if (customerDAO.getCustomerByEmail(email) != null) {
            throw new IllegalArgumentException("Customer with email '" + email + "' already exists.");
        }

        Customer newCustomer = new Customer(name.trim(), email.trim(), phone.trim(), address != null ? address.trim() : "");
        int generatedId = customerDAO.createCustomer(newCustomer);
        if (generatedId > 0) {
            return customerDAO.getCustomerById(generatedId);
        }
        return null;
    }

    /**
     * Finds customer by ID.
     */
    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.getCustomerById(customerId);
    }

    /**
     * Retrieves all customers.
     */
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }
}
