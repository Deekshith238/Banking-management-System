package dao;

import model.Account;
import model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountDAO Class
 * Data Access Object for 'accounts' table operations.
 * Demonstrates Key Concepts:
 * 1. JDBC Transaction Management (setAutoCommit(false), commit(), rollback())
 * 2. PreparedStatement & ResultSet
 * 3. Business rule validation (sufficiency of funds, valid amounts)
 */
public class AccountDAO {

    private TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * Create a new Bank Account for a customer.
     * @param account Account object
     * @return true if created successfully, false otherwise
     */
    public boolean createAccount(Account account) {
        String sql = "INSERT INTO accounts (customer_id, account_number, account_type, balance, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, account.getCustomerId());
            stmt.setString(2, account.getAccountNumber());
            stmt.setString(3, account.getAccountType());
            stmt.setDouble(4, account.getBalance());
            stmt.setString(5, account.getStatus());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                // If initial balance > 0, log initial deposit transaction
                if (account.getBalance() > 0) {
                    Account createdAcc = getAccountByNumber(account.getAccountNumber());
                    if (createdAcc != null) {
                        transactionDAO.addTransaction(new Transaction(createdAcc.getAccountId(), "INITIAL_DEPOSIT", account.getBalance()));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error creating Account: " + e.getMessage());
        }

        return false;
    }

    /**
     * Retrieve Account entity by Account Number.
     * @param accountNumber Unique account number
     * @return Account object if found, null otherwise
     */
    public Account getAccountByNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("account_id"),
                            rs.getInt("customer_id"),
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            rs.getDouble("balance"),
                            rs.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error fetching Account: " + e.getMessage());
        }

        return null;
    }

    /**
     * Retrieve all accounts owned by a specific Customer ID.
     * @param customerId Customer ID
     * @return List of Account objects
     */
    public List<Account> getAccountsByCustomerId(int customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Account acc = new Account(
                            rs.getInt("account_id"),
                            rs.getInt("customer_id"),
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            rs.getDouble("balance"),
                            rs.getString("status")
                    );
                    accounts.add(acc);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error fetching Customer Accounts: " + e.getMessage());
        }

        return accounts;
    }

    /**
     * Get current balance for an account.
     * @param accountNumber Account Number
     * @return double balance value (-1 if account not found)
     */
    public double getBalance(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        if (account != null) {
            return account.getBalance();
        }
        return -1;
    }

    /**
     * Deposit money into account.
     * @param accountNumber Account number
     * @param amount Deposit amount (> 0)
     * @return true if deposit succeeded, false otherwise
     */
    public boolean deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Deposit amount must be greater than zero!");
            return false;
        }

        Account account = getAccountByNumber(accountNumber);
        if (account == null) {
            System.out.println("❌ Account number not found!");
            return false;
        }

        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                // Record transaction
                transactionDAO.addTransaction(new Transaction(account.getAccountId(), "DEPOSIT", amount));
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error during Deposit: " + e.getMessage());
        }

        return false;
    }

    /**
     * Withdraw money from account.
     * @param accountNumber Account number
     * @param amount Withdrawal amount (> 0)
     * @return true if withdrawal succeeded, false otherwise
     */
    public boolean withdraw(String accountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be greater than zero!");
            return false;
        }

        Account account = getAccountByNumber(accountNumber);
        if (account == null) {
            System.out.println("❌ Account number not found!");
            return false;
        }

        if (account.getBalance() < amount) {
            System.out.println("❌ Insufficient funds! Current Balance: $" + String.format("%.2f", account.getBalance()));
            return false;
        }

        String sql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                // Record transaction
                transactionDAO.addTransaction(new Transaction(account.getAccountId(), "WITHDRAWAL", amount));
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error during Withdrawal: " + e.getMessage());
        }

        return false;
    }

    /**
     * Transfer money between sender and receiver accounts using explicit JDBC Transaction Management.
     * Demonstrates ACID Compliance:
     * - Auto-commit is set to false
     * - Money is deducted from sender and added to receiver atomically
     * - Transaction logs are recorded for both accounts
     * - If any step fails, conn.rollback() is triggered to undo changes
     *
     * @param senderAccNum Sender's Account Number
     * @param receiverAccNum Receiver's Account Number
     * @param amount Transfer amount (> 0)
     * @return true if transfer completed successfully, false otherwise
     */
    public boolean transfer(String senderAccNum, String receiverAccNum, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Transfer amount must be greater than zero!");
            return false;
        }

        if (senderAccNum.equalsIgnoreCase(receiverAccNum)) {
            System.out.println("❌ Cannot transfer money to the same account!");
            return false;
        }

        Account sender = getAccountByNumber(senderAccNum);
        Account receiver = getAccountByNumber(receiverAccNum);

        if (sender == null) {
            System.out.println("❌ Sender Account not found!");
            return false;
        }

        if (receiver == null) {
            System.out.println("❌ Receiver Account not found!");
            return false;
        }

        if (sender.getBalance() < amount) {
            System.out.println("❌ Insufficient balance in sender account! Available: $" + String.format("%.2f", sender.getBalance()));
            return false;
        }

        String deductSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
        String addSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // STEP 1: Disable Auto-Commit to start explicit transaction
            conn.setAutoCommit(false);

            // STEP 2: Deduct amount from Sender
            try (PreparedStatement stmtDeduct = conn.prepareStatement(deductSql)) {
                stmtDeduct.setDouble(1, amount);
                stmtDeduct.setString(2, senderAccNum);
                int rowsDeducted = stmtDeduct.executeUpdate();
                if (rowsDeducted == 0) {
                    throw new SQLException("Failed to deduct amount from sender.");
                }
            }

            // STEP 3: Add amount to Receiver
            try (PreparedStatement stmtAdd = conn.prepareStatement(addSql)) {
                stmtAdd.setDouble(1, amount);
                stmtAdd.setString(2, receiverAccNum);
                int rowsAdded = stmtAdd.executeUpdate();
                if (rowsAdded == 0) {
                    throw new SQLException("Failed to credit amount to receiver.");
                }
            }

            // STEP 4: Record Transaction logs within the same connection context
            transactionDAO.addTransaction(conn, new Transaction(sender.getAccountId(), "TRANSFER_DEBIT -> " + receiverAccNum, amount));
            transactionDAO.addTransaction(conn, new Transaction(receiver.getAccountId(), "TRANSFER_CREDIT <- " + senderAccNum, amount));

            // STEP 5: Commit Transaction (atomic success)
            conn.commit();
            System.out.println("✅ Transfer of $" + String.format("%.2f", amount) + " completed successfully!");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Transaction Failed! Rolling back changes... Error: " + e.getMessage());
            if (conn != null) {
                try {
                    // Rollback all changes if any error occurs
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("❌ Rollback Error: " + ex.getMessage());
                }
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    // Restore default auto-commit state and close connection
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("❌ Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}
