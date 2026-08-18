package banking.model;

import java.sql.Timestamp;

/**
 * Model representing a Savings Account with minimum balance constraints and interest logic.
 */
public class SavingsAccount extends Account {
    public static final double MINIMUM_BALANCE = 500.0;
    public static final double INTEREST_RATE = 0.04; // 4% Annual Interest Rate

    public SavingsAccount() {
        super();
        setAccountType("SAVINGS");
    }

    public SavingsAccount(String accountNumber, int customerId, double balance) {
        super(accountNumber, customerId, "SAVINGS", balance);
    }

    public SavingsAccount(String accountNumber, int customerId, double balance, Timestamp createdAt) {
        super(accountNumber, customerId, "SAVINGS", balance, createdAt);
    }

    @Override
    public boolean canWithdraw(double amount) {
        if (amount <= 0) return false;
        return (getBalance() - amount) >= MINIMUM_BALANCE;
    }

    @Override
    public String getAccountTypeName() {
        return "Savings Account";
    }

    public double calculateAnnualInterest() {
        return getBalance() * INTEREST_RATE;
    }
}
