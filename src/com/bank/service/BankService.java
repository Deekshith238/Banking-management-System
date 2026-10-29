package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.TransactionDAO;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.model.Account;
import com.bank.model.Admin;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class BankService {

    private final CustomerDAO customerDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final Random random;

    public BankService() {
        this.customerDAO = new CustomerDAO();
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.random = new Random();
    }

    public boolean registerCustomer(String name, String email, String phone, String password) {
        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.err.println("[VALIDATION ERROR] Name, email, and password cannot be empty.");
            return false;
        }
        Customer customer = new Customer(name.trim(), email.trim(), phone.trim(), password);
        return customerDAO.registerCustomer(customer);
    }

    public Customer loginCustomer(String email, String password) {
        if (email == null || password == null) return null;
        return customerDAO.loginCustomer(email.trim(), password.trim());
    }

    public Account openAccount(int customerId, String accountType, double initialDeposit) {
        if (initialDeposit < 0) {
            System.err.println("[VALIDATION ERROR] Initial deposit cannot be negative.");
            return null;
        }

        long accountNumber = 1000000000L + (long)(random.nextDouble() * 899999999L);
        Account account = new Account(customerId, accountNumber, accountType.toUpperCase(), initialDeposit);

        boolean created = accountDAO.createAccount(account);
        if (!created) {
            return null;
        }

        Account fetched = accountDAO.getAccountByNumber(accountNumber);
        if (fetched != null && initialDeposit > 0) {
            try (Connection conn = DBConnection.getConnection()) {
                transactionDAO.logTransaction(conn, fetched.getAccountId(), "DEPOSIT", initialDeposit);
            } catch (SQLException e) {
                System.err.println("[LOG ERROR] Failed to log initial deposit: " + e.getMessage());
            }
        }
        return fetched;
    }

    public List<Account> getCustomerAccounts(int customerId) {
        return accountDAO.getAccountsByCustomerId(customerId);
    }

    public double checkBalance(long accountNumber) throws AccountNotFoundException {
        Account acc = accountDAO.getAccountByNumber(accountNumber);
        if (acc == null) {
            throw new AccountNotFoundException("Account number " + accountNumber + " does not exist.");
        }
        return acc.getBalance();
    }

    public boolean deposit(long accountNumber, double amount) throws AccountNotFoundException {
        if (amount <= 0) {
            System.err.println("[VALIDATION ERROR] Deposit amount must be greater than zero.");
            return false;
        }

        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " not found.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            double newBalance = account.getBalance() + amount;
            boolean updated = accountDAO.updateBalance(conn, accountNumber, newBalance);
            boolean logged = transactionDAO.logTransaction(conn, account.getAccountId(), "DEPOSIT", amount);

            if (updated && logged) {
                conn.commit();
                System.out.printf("[SUCCESS] Deposited ₹%.2f. New Balance: ₹%.2f\n", amount, newBalance);
                return true;
            } else {
                conn.rollback();
                System.err.println("[ERROR] Deposit failed during database update.");
                return false;
            }
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("[TRANSACTION ERROR] Deposit rolled back: " + e.getMessage());
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    public boolean withdraw(long accountNumber, double amount) throws AccountNotFoundException, InsufficientBalanceException {
        if (amount <= 0) {
            System.err.println("[VALIDATION ERROR] Withdrawal amount must be greater than zero.");
            return false;
        }

        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " not found.");
        }

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException(
                String.format("Insufficient funds! Available: ₹%.2f, Requested: ₹%.2f", account.getBalance(), amount)
            );
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            double newBalance = account.getBalance() - amount;
            boolean updated = accountDAO.updateBalance(conn, accountNumber, newBalance);
            boolean logged = transactionDAO.logTransaction(conn, account.getAccountId(), "WITHDRAWAL", amount);

            if (updated && logged) {
                conn.commit();
                System.out.printf("[SUCCESS] Withdrawn ₹%.2f. New Balance: ₹%.2f\n", amount, newBalance);
                return true;
            } else {
                conn.rollback();
                System.err.println("[ERROR] Withdrawal failed. Rolled back.");
                return false;
            }
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("[TRANSACTION ERROR] Withdrawal rolled back: " + e.getMessage());
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    public boolean transferMoney(long fromAccountNumber, long toAccountNumber, double amount) 
            throws AccountNotFoundException, InsufficientBalanceException {

        if (amount <= 0) {
            System.err.println("[VALIDATION ERROR] Transfer amount must be greater than zero.");
            return false;
        }

        if (fromAccountNumber == toAccountNumber) {
            System.err.println("[VALIDATION ERROR] Cannot transfer funds to the same account.");
            return false;
        }

        Account sender = accountDAO.getAccountByNumber(fromAccountNumber);
        if (sender == null) {
            throw new AccountNotFoundException("Sender account " + fromAccountNumber + " not found.");
        }

        Account receiver = accountDAO.getAccountByNumber(toAccountNumber);
        if (receiver == null) {
            throw new AccountNotFoundException("Receiver account " + toAccountNumber + " not found.");
        }

        if (sender.getBalance() < amount) {
            throw new InsufficientBalanceException(
                String.format("Transfer failed! Insufficient balance in A/C %d. Available: ₹%.2f, Required: ₹%.2f",
                        fromAccountNumber, sender.getBalance(), amount)
            );
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            double senderNewBalance = sender.getBalance() - amount;
            double receiverNewBalance = receiver.getBalance() + amount;

            boolean senderDeducted = accountDAO.updateBalance(conn, fromAccountNumber, senderNewBalance);
            boolean receiverCredited = accountDAO.updateBalance(conn, toAccountNumber, receiverNewBalance);
            boolean senderLogged = transactionDAO.logTransaction(conn, sender.getAccountId(), "TRANSFER_OUT", amount);
            boolean receiverLogged = transactionDAO.logTransaction(conn, receiver.getAccountId(), "TRANSFER_IN", amount);

            if (senderDeducted && receiverCredited && senderLogged && receiverLogged) {
                conn.commit();
                System.out.printf("[SUCCESS] Transferred ₹%.2f from A/C %d to A/C %d successfully!\n",
                        amount, fromAccountNumber, toAccountNumber);
                return true;
            } else {
                conn.rollback();
                System.err.println("[ERROR] Transfer failed during database update. Changes rolled back.");
                return false;
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("[TRANSACTION ERROR] Exception occurred during transfer. Changes safely rolled back.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<Transaction> getTransactionHistory(long accountNumber) throws AccountNotFoundException {
        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " not found.");
        }
        return transactionDAO.getTransactionsByAccountId(account.getAccountId());
    }

    public Admin loginAdmin(String username, String password) {
        if (username == null || password == null) return null;
        return customerDAO.loginAdmin(username.trim(), password.trim());
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    public Account searchAccount(long accountNumber) {
        return accountDAO.getAccountByNumber(accountNumber);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }
}
