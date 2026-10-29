package com.bank.model;

import java.sql.Timestamp;

public class Transaction {
    private int transactionId;
    private int accountId;
    private String transactionType;
    private double amount;
    private Timestamp transactionDate;

    public Transaction() {
    }

    public Transaction(int accountId, String transactionType, double amount) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = new Timestamp(System.currentTimeMillis());
    }

    public Transaction(int transactionId, int accountId, String transactionType, double amount, Timestamp transactionDate) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

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
        return String.format("TxID: %-5d | Date: %-19s | Type: %-13s | Amount: ₹%-10.2f",
                transactionId, 
                transactionDate != null ? transactionDate.toString().substring(0, 19) : "N/A", 
                transactionType, 
                amount);
    }
}
