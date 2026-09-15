package model;

/**
 * Customer Model Class
 * Represents a customer entity in the Banking System.
 * Demonstrates OOP Concept: Encapsulation (Private fields with public getters and setters).
 */
public class Customer {

    // Private member variables (Encapsulation)
    private int customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;

    // Default Constructor
    public Customer() {
    }

    // Parameterized Constructor (used for creating new customer without ID before DB insertion)
    public Customer(String name, String email, String phone, String address, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
    }

    // Full Parameterized Constructor (used when fetching existing customer from Database)
    public Customer(int customerId, String name, String email, String phone, String address, String password) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Overriding toString() for clean debugging/display
    @Override
    public String toString() {
        return "Customer [" +
                "ID=" + customerId +
                ", Name='" + name + '\'' +
                ", Email='" + email + '\'' +
                ", Phone='" + phone + '\'' +
                ", Address='" + address + '\'' +
                ']';
    }
}
