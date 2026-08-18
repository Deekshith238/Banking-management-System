package banking.service;

import banking.dao.AccountDAO;
import banking.dao.TransactionDAO;
import banking.model.Account;
import banking.model.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer managing Transaction reporting and account statements.
 */
public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
    }

    public TransactionService(TransactionDAO transactionDAO, AccountDAO accountDAO) {
        this.transactionDAO = transactionDAO;
        this.accountDAO = accountDAO;
    }

    /**
     * Gets transaction statement history for a given account number.
     */
    public List<Transaction> getAccountStatement(String accountNumber) throws IllegalArgumentException, SQLException {
        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account number '" + accountNumber + "' does not exist.");
        }
        return transactionDAO.getTransactionsByAccountNumber(accountNumber);
    }

    /**
     * Gets all transactions in system.
     */
    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.getAllTransactions();
    }
}
