package banking.model;

import java.sql.Timestamp;

/**
 * Model representing a financial Transaction entity.
 */
public class Transaction {
    private int transactionId;
    private String accountNumber;
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    private double amount;
    private double balanceAfter;
    private String targetAccountNumber;
    private String remarks;
    private Timestamp timestamp;

    public Transaction() {}

    public Transaction(String accountNumber, String transactionType, double amount, double balanceAfter,
                       String targetAccountNumber, String remarks) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.targetAccountNumber = targetAccountNumber;
        this.remarks = remarks;
    }

    public Transaction(int transactionId, String accountNumber, String transactionType, double amount,
                       double balanceAfter, String targetAccountNumber, String remarks, Timestamp timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.targetAccountNumber = targetAccountNumber;
        this.remarks = remarks;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public void setTargetAccountNumber(String targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("Transaction [ID: %d, Account: %s, Type: %s, Amount: $%.2f, Balance After: $%.2f, Time: %s]",
                transactionId, accountNumber, transactionType, amount, balanceAfter, timestamp);
    }
}
