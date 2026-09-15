package model;

import java.sql.Timestamp;

/**
 * Transaction Model Class
 * Represents a deposit, withdrawal, or transfer transaction logged for an account.
 * Demonstrates OOP Concept: Encapsulation.
 */
public class Transaction {

    private int transactionId;
    private int accountId;
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT
    private double amount;
    private Timestamp transactionDate;

    // Default Constructor
    public Transaction() {
    }

    // Constructor for creating a new transaction log (without ID and date - set automatically by DB)
    public Transaction(int accountId, String transactionType, double amount) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    // Full Constructor
    public Transaction(int transactionId, int accountId, String transactionType, double amount, Timestamp transactionDate) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "Transaction [" +
                "ID=" + transactionId +
                ", Type='" + transactionType + '\'' +
                ", Amount=$" + String.format("%.2f", amount) +
                ", Date=" + transactionDate +
                ']';
    }
}
