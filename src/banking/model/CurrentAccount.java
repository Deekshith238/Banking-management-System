package banking.model;

import java.sql.Timestamp;

/**
 * Model representing a Current Account with overdraft protection features.
 */
public class CurrentAccount extends Account {
    public static final double OVERDRAFT_LIMIT = 1000.0;

    public CurrentAccount() {
        super();
        setAccountType("CURRENT");
    }

    public CurrentAccount(String accountNumber, int customerId, double balance) {
        super(accountNumber, customerId, "CURRENT", balance);
    }

    public CurrentAccount(String accountNumber, int customerId, double balance, Timestamp createdAt) {
        super(accountNumber, customerId, "CURRENT", balance, createdAt);
    }

    @Override
    public boolean canWithdraw(double amount) {
        if (amount <= 0) return false;
        return (getBalance() - amount) >= -OVERDRAFT_LIMIT;
    }

    @Override
    public String getAccountTypeName() {
        return "Current Account";
    }

    public double getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }
}
