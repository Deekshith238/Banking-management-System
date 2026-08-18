package banking.service;

import banking.dao.AccountDAO;
import banking.dao.CustomerDAO;
import banking.dao.TransactionDAO;
import banking.model.*;
import banking.util.DBConnection;
import banking.util.InputValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Service layer managing Account business logic and atomic financial operations.
 */
public class AccountService {
    private final AccountDAO accountDAO;
    private final CustomerDAO customerDAO;
    private final TransactionDAO transactionDAO;

    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.customerDAO = new CustomerDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public AccountService(AccountDAO accountDAO, CustomerDAO customerDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.customerDAO = customerDAO;
        this.transactionDAO = transactionDAO;
    }

    /**
     * Opens a new account (SAVINGS or CURRENT) for an existing customer.
     */
    public Account openAccount(int customerId, String accountType, double initialDeposit) throws IllegalArgumentException, SQLException {
        if (customerDAO.getCustomerById(customerId) == null) {
            throw new IllegalArgumentException("Customer ID " + customerId + " does not exist.");
        }

        if (!InputValidator.isValidAccountType(accountType)) {
            throw new IllegalArgumentException("Invalid account type. Must be 'SAVINGS' or 'CURRENT'.");
        }

        String typeUpper = accountType.trim().toUpperCase();
        if ("SAVINGS".equals(typeUpper) && initialDeposit < SavingsAccount.MINIMUM_BALANCE) {
            throw new IllegalArgumentException(String.format("Initial deposit for Savings Account must be at least $%.2f", SavingsAccount.MINIMUM_BALANCE));
        } else if ("CURRENT".equals(typeUpper) && initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative.");
        }

        String accNumber = generateUniqueAccountNumber(typeUpper);
        Account newAccount;
        if ("SAVINGS".equals(typeUpper)) {
            newAccount = new SavingsAccount(accNumber, customerId, initialDeposit);
        } else {
            newAccount = new CurrentAccount(accNumber, customerId, initialDeposit);
        }

        boolean created = accountDAO.createAccount(newAccount);
        if (created) {
            // Record initial deposit transaction if > 0
            if (initialDeposit > 0) {
                Transaction initTx = new Transaction(accNumber, "DEPOSIT", initialDeposit, initialDeposit, null, "Initial Account Opening Deposit");
                transactionDAO.recordTransaction(initTx);
            }
            return accountDAO.getAccountByNumber(accNumber);
        }
        return null;
    }

    /**
     * Deposits funds into an account.
     */
    public boolean deposit(String accountNumber, double amount, String remarks) throws IllegalArgumentException, SQLException {
        if (!InputValidator.isPositiveAmount(amount)) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account account = accountDAO.getAccountByNumber(accountNumber, conn);
                if (account == null) {
                    throw new IllegalArgumentException("Account number '" + accountNumber + "' not found.");
                }

                account.deposit(amount);
                boolean balanceUpdated = accountDAO.updateBalance(accountNumber, account.getBalance(), conn);

                Transaction tx = new Transaction(
                        accountNumber, "DEPOSIT", amount, account.getBalance(), null,
                        remarks != null && !remarks.trim().isEmpty() ? remarks : "Deposit"
                );
                boolean txRecorded = transactionDAO.recordTransaction(tx, conn);

                if (balanceUpdated && txRecorded) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Withdraws funds from an account according to model constraints.
     */
    public boolean withdraw(String accountNumber, double amount, String remarks) throws IllegalArgumentException, SQLException {
        if (!InputValidator.isPositiveAmount(amount)) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than 0.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account account = accountDAO.getAccountByNumber(accountNumber, conn);
                if (account == null) {
                    throw new IllegalArgumentException("Account number '" + accountNumber + "' not found.");
                }

                if (!account.canWithdraw(amount)) {
                    if (account instanceof SavingsAccount) {
                        throw new IllegalArgumentException(String.format(
                                "Insufficient funds! Savings account requires maintaining minimum balance of $%.2f. Current balance: $%.2f",
                                SavingsAccount.MINIMUM_BALANCE, account.getBalance()));
                    } else if (account instanceof CurrentAccount) {
                        throw new IllegalArgumentException(String.format(
                                "Overdraft limit exceeded! Maximum overdraft limit is $%.2f. Current balance: $%.2f",
                                CurrentAccount.OVERDRAFT_LIMIT, account.getBalance()));
                    }
                }

                account.withdraw(amount);
                boolean balanceUpdated = accountDAO.updateBalance(accountNumber, account.getBalance(), conn);

                Transaction tx = new Transaction(
                        accountNumber, "WITHDRAWAL", amount, account.getBalance(), null,
                        remarks != null && !remarks.trim().isEmpty() ? remarks : "Withdrawal"
                );
                boolean txRecorded = transactionDAO.recordTransaction(tx, conn);

                if (balanceUpdated && txRecorded) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Performs an atomic fund transfer between two accounts.
     */
    public boolean transferFunds(String fromAccNumber, String toAccNumber, double amount, String remarks) throws IllegalArgumentException, SQLException {
        if (fromAccNumber == null || toAccNumber == null || fromAccNumber.equalsIgnoreCase(toAccNumber)) {
            throw new IllegalArgumentException("Source and target account numbers must be distinct.");
        }
        if (!InputValidator.isPositiveAmount(amount)) {
            throw new IllegalArgumentException("Transfer amount must be greater than 0.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account sourceAcc = accountDAO.getAccountByNumber(fromAccNumber, conn);
                Account targetAcc = accountDAO.getAccountByNumber(toAccNumber, conn);

                if (sourceAcc == null) {
                    throw new IllegalArgumentException("Source account '" + fromAccNumber + "' not found.");
                }
                if (targetAcc == null) {
                    throw new IllegalArgumentException("Target account '" + toAccNumber + "' not found.");
                }

                if (!sourceAcc.canWithdraw(amount)) {
                    throw new IllegalArgumentException("Transfer failed: Insufficient balance/limit in source account.");
                }

                // Execute transfer in memory model
                sourceAcc.withdraw(amount);
                targetAcc.deposit(amount);

                // Update balances in DB
                boolean sourceUpdated = accountDAO.updateBalance(fromAccNumber, sourceAcc.getBalance(), conn);
                boolean targetUpdated = accountDAO.updateBalance(toAccNumber, targetAcc.getBalance(), conn);

                String transferRemark = remarks != null && !remarks.trim().isEmpty() ? remarks : "Fund Transfer";

                // Record transaction for source account (TRANSFER_OUT)
                Transaction sourceTx = new Transaction(
                        fromAccNumber, "TRANSFER_OUT", amount, sourceAcc.getBalance(), toAccNumber, transferRemark
                );
                boolean sourceTxRecorded = transactionDAO.recordTransaction(sourceTx, conn);

                // Record transaction for target account (TRANSFER_IN)
                Transaction targetTx = new Transaction(
                        toAccNumber, "TRANSFER_IN", amount, targetAcc.getBalance(), fromAccNumber, transferRemark
                );
                boolean targetTxRecorded = transactionDAO.recordTransaction(targetTx, conn);

                if (sourceUpdated && targetUpdated && sourceTxRecorded && targetTxRecorded) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Account getAccount(String accountNumber) throws SQLException {
        return accountDAO.getAccountByNumber(accountNumber);
    }

    public List<Account> getAccountsByCustomer(int customerId) throws SQLException {
        return accountDAO.getAccountsByCustomerId(customerId);
    }

    public List<Account> getAllAccounts() throws SQLException {
        return accountDAO.getAllAccounts();
    }

    // Utility generator for unique account numbers
    private String generateUniqueAccountNumber(String type) {
        String prefix = "SAV".equalsIgnoreCase(type) ? "SAV" : "CUR";
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000);
        return prefix + (System.currentTimeMillis() % 10000) + randomNumber;
    }
}
