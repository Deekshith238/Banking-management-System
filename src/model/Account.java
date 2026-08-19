package model;

/**
 * Account Model Class
 * Represents a bank account entity associated with a customer.
 * Demonstrates OOP Concept: Encapsulation.
 */
public class Account {

    // Private fields
    private int accountId;
    private int customerId;
    private String accountNumber;
    private String accountType; // Savings or Current
    private double balance;
    private String status;      // ACTIVE, INACTIVE, etc.

    // Default Constructor
    public Account() {
    }

    // Constructor for creating a new account (without accountId)
    public Account(int customerId, String accountNumber, String accountType, double balance, String status) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    // Full Constructor (retrieved from Database)
    public Account(int accountId, int customerId, String accountNumber, String accountType, double balance, String status) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    // Getters and Setters
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account [" +
                "Account No='" + accountNumber + '\'' +
                ", Type='" + accountType + '\'' +
                ", Balance=$" + String.format("%.2f", balance) +
                ", Status='" + status + '\'' +
                ']';
    }
}
