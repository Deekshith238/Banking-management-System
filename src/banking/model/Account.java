package banking.model;

import java.sql.Timestamp;

/**
 * Abstract Base Model representing a Bank Account in the Banking Management System.
 */
public abstract class Account {
    private String accountNumber;
    private int customerId;
    private String accountType; // "SAVINGS" or "CURRENT"
    private double balance;
    private Timestamp createdAt;

    // Constructors
    public Account() {}

    public Account(String accountNumber, int customerId, String accountType, double balance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
    }

    public Account(String accountNumber, int customerId, String accountType, double balance, Timestamp createdAt) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // Abstract methods to enforce polymorphic business rules
    public abstract boolean canWithdraw(double amount);
    public abstract String getAccountTypeName();

    // Common Business Operations
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (canWithdraw(amount)) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Account [No: %s, Customer ID: %d, Type: %s, Balance: $%.2f]",
                accountNumber, customerId, accountType, balance);
    }
}
