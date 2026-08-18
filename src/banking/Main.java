package banking;

import banking.model.Account;
import banking.model.Customer;
import banking.model.Transaction;
import banking.service.AccountService;
import banking.service.CustomerService;
import banking.service.TransactionService;
import banking.util.DBConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Main application entry point for the Java Banking Management System CLI.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static CustomerService customerService;
    private static AccountService accountService;
    private static TransactionService transactionService;

    public static void main(String[] args) {
        customerService = new CustomerService();
        accountService = new AccountService();
        transactionService = new TransactionService();

        printHeader();
        checkDatabaseConnectivity();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleCustomerMenu();
                    break;
                case "2":
                    handleAccountMenu();
                    break;
                case "3":
                    handleTransactionOperationsMenu();
                    break;
                case "4":
                    handleStatementMenu();
                    break;
                case "5":
                    handleDatabaseConfigMenu();
                    break;
                case "6":
                    System.out.println("\nThank you for using Banking Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please select a number from 1 to 6.");
            }
        }
        scanner.close();
    }

    private static void printHeader() {
        System.out.println("==========================================================");
        System.out.println("           🏦 BANKING MANAGEMENT SYSTEM 🏦               ");
        System.out.println("==========================================================");
    }

    private static void checkDatabaseConnectivity() {
        System.out.print("🔍 Checking Database Connection... ");
        if (DBConnection.testConnection()) {
            System.out.println("✅ Connected to MySQL Database successfully!");
        } else {
            System.out.println("⚠️ Warning: Cannot connect to MySQL Database at " + DBConnection.getUrl());
            System.out.println("   (Tip: Option 5 in Main Menu lets you test connection or update DB credentials)");
        }
        System.out.println();
    }

    private static void printMainMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1. Customer Management");
        System.out.println("2. Account Management");
        System.out.println("3. Financial Operations (Deposit / Withdraw / Transfer)");
        System.out.println("4. Account Statements & Transaction History");
        System.out.println("5. Database Connection Diagnostics");
        System.out.println("6. Exit");
        System.out.print("Select an option (1-6): ");
    }

    // =========================================================================
    // 1. CUSTOMER MANAGEMENT
    // =========================================================================
    private static void handleCustomerMenu() {
        System.out.println("\n--- CUSTOMER MANAGEMENT ---");
        System.out.println("1. Register New Customer");
        System.out.println("2. View Customer Details by ID");
        System.out.println("3. List All Customers");
        System.out.println("4. Back to Main Menu");
        System.out.print("Select an option (1-4): ");

        String choice = scanner.nextLine().trim();
        try {
            switch (choice) {
                case "1":
                    registerCustomer();
                    break;
                case "2":
                    viewCustomerById();
                    break;
                case "3":
                    listAllCustomers();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void registerCustomer() throws SQLException {
        System.out.println("\n--- Register New Customer ---");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();

        Customer customer = customerService.registerCustomer(name, email, phone, address);
        if (customer != null) {
            System.out.println("✅ Customer registered successfully!");
            System.out.println("   Customer ID: " + customer.getCustomerId());
            System.out.println("   Name: " + customer.getName());
            System.out.println("   Email: " + customer.getEmail());
        } else {
            System.out.println("❌ Failed to register customer.");
        }
    }

    private static void viewCustomerById() throws SQLException {
        System.out.print("Enter Customer ID: ");
        String input = scanner.nextLine();
        try {
            int customerId = Integer.parseInt(input.trim());
            Customer customer = customerService.getCustomerById(customerId);
            if (customer != null) {
                System.out.println("\n✅ Customer Profile Found:");
                System.out.println("   ID: " + customer.getCustomerId());
                System.out.println("   Name: " + customer.getName());
                System.out.println("   Email: " + customer.getEmail());
                System.out.println("   Phone: " + customer.getPhone());
                System.out.println("   Address: " + customer.getAddress());
                System.out.println("   Created At: " + customer.getCreatedAt());
            } else {
                System.out.println("❌ Customer with ID " + customerId + " not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Customer ID must be a valid integer.");
        }
    }

    private static void listAllCustomers() throws SQLException {
        List<Customer> customers = customerService.getAllCustomers();
        System.out.println("\n--- ALL REGISTERED CUSTOMERS ---");
        if (customers.isEmpty()) {
            System.out.println("No customers found in database.");
            return;
        }
        System.out.printf("%-5s | %-20s | %-25s | %-15s%n", "ID", "Name", "Email", "Phone");
        System.out.println("------------------------------------------------------------------");
        for (Customer c : customers) {
            System.out.printf("%-5d | %-20s | %-25s | %-15s%n",
                    c.getCustomerId(), c.getName(), c.getEmail(), c.getPhone());
        }
    }

    // =========================================================================
    // 2. ACCOUNT MANAGEMENT
    // =========================================================================
    private static void handleAccountMenu() {
        System.out.println("\n--- ACCOUNT MANAGEMENT ---");
        System.out.println("1. Open New Bank Account");
        System.out.println("2. View Account Details by Account Number");
        System.out.println("3. List Accounts of a Customer");
        System.out.println("4. List All Accounts");
        System.out.println("5. Back to Main Menu");
        System.out.print("Select an option (1-5): ");

        String choice = scanner.nextLine().trim();
        try {
            switch (choice) {
                case "1":
                    openAccount();
                    break;
                case "2":
                    viewAccountByNumber();
                    break;
                case "3":
                    listCustomerAccounts();
                    break;
                case "4":
                    listAllAccounts();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void openAccount() throws SQLException {
        System.out.println("\n--- Open New Bank Account ---");
        System.out.print("Enter Customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter Account Type (SAVINGS / CURRENT): ");
        String accountType = scanner.nextLine().trim();

        System.out.print("Enter Initial Deposit Amount ($): ");
        double initialDeposit = Double.parseDouble(scanner.nextLine().trim());

        Account account = accountService.openAccount(customerId, accountType, initialDeposit);
        if (account != null) {
            System.out.println("✅ Account created successfully!");
            System.out.println("   Account Number: " + account.getAccountNumber());
            System.out.println("   Account Type: " + account.getAccountTypeName());
            System.out.printf("   Current Balance: $%.2f%n", account.getBalance());
        } else {
            System.out.println("❌ Failed to create account.");
        }
    }

    private static void viewAccountByNumber() throws SQLException {
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        Account account = accountService.getAccount(accNo);
        if (account != null) {
            System.out.println("\n✅ Account Details:");
            System.out.println("   Account Number: " + account.getAccountNumber());
            System.out.println("   Customer ID: " + account.getCustomerId());
            System.out.println("   Account Type: " + account.getAccountTypeName());
            System.out.printf("   Current Balance: $%.2f%n", account.getBalance());
        } else {
            System.out.println("❌ Account number '" + accNo + "' not found.");
        }
    }

    private static void listCustomerAccounts() throws SQLException {
        System.out.print("Enter Customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine().trim());

        List<Account> accounts = accountService.getAccountsByCustomer(customerId);
        System.out.println("\n--- ACCOUNTS FOR CUSTOMER ID " + customerId + " ---");
        if (accounts.isEmpty()) {
            System.out.println("No accounts found for customer ID " + customerId);
            return;
        }
        System.out.printf("%-15s | %-15s | %-12s%n", "Account No", "Type", "Balance");
        System.out.println("---------------------------------------------");
        for (Account acc : accounts) {
            System.out.printf("%-15s | %-15s | $%-11.2f%n", acc.getAccountNumber(), acc.getAccountTypeName(), acc.getBalance());
        }
    }

    private static void listAllAccounts() throws SQLException {
        List<Account> accounts = accountService.getAllAccounts();
        System.out.println("\n--- ALL BANK ACCOUNTS ---");
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        System.out.printf("%-15s | %-12s | %-15s | %-12s%n", "Account No", "Cust ID", "Type", "Balance");
        System.out.println("---------------------------------------------------------");
        for (Account acc : accounts) {
            System.out.printf("%-15s | %-12d | %-15s | $%-11.2f%n",
                    acc.getAccountNumber(), acc.getCustomerId(), acc.getAccountTypeName(), acc.getBalance());
        }
    }

    // =========================================================================
    // 3. FINANCIAL OPERATIONS
    // =========================================================================
    private static void handleTransactionOperationsMenu() {
        System.out.println("\n--- FINANCIAL OPERATIONS ---");
        System.out.println("1. Deposit Funds");
        System.out.println("2. Withdraw Funds");
        System.out.println("3. Transfer Funds Between Accounts");
        System.out.println("4. Back to Main Menu");
        System.out.print("Select an option (1-4): ");

        String choice = scanner.nextLine().trim();
        try {
            switch (choice) {
                case "1":
                    performDeposit();
                    break;
                case "2":
                    performWithdrawal();
                    break;
                case "3":
                    performTransfer();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void performDeposit() throws SQLException {
        System.out.println("\n--- Deposit Funds ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();
        System.out.print("Enter Amount to Deposit ($): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Remarks (optional): ");
        String remarks = scanner.nextLine().trim();

        boolean success = accountService.deposit(accNo, amount, remarks);
        if (success) {
            Account acc = accountService.getAccount(accNo);
            System.out.println("✅ Deposit successful!");
            System.out.printf("   New Balance for %s: $%.2f%n", accNo, acc.getBalance());
        } else {
            System.out.println("❌ Deposit operation failed.");
        }
    }

    private static void performWithdrawal() throws SQLException {
        System.out.println("\n--- Withdraw Funds ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();
        System.out.print("Enter Amount to Withdraw ($): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Remarks (optional): ");
        String remarks = scanner.nextLine().trim();

        boolean success = accountService.withdraw(accNo, amount, remarks);
        if (success) {
            Account acc = accountService.getAccount(accNo);
            System.out.println("✅ Withdrawal successful!");
            System.out.printf("   New Balance for %s: $%.2f%n", accNo, acc.getBalance());
        } else {
            System.out.println("❌ Withdrawal operation failed.");
        }
    }

    private static void performTransfer() throws SQLException {
        System.out.println("\n--- Transfer Funds ---");
        System.out.print("Enter Source Account Number: ");
        String fromAcc = scanner.nextLine().trim();
        System.out.print("Enter Target Account Number: ");
        String toAcc = scanner.nextLine().trim();
        System.out.print("Enter Transfer Amount ($): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Transfer Remarks (optional): ");
        String remarks = scanner.nextLine().trim();

        boolean success = accountService.transferFunds(fromAcc, toAcc, amount, remarks);
        if (success) {
            Account source = accountService.getAccount(fromAcc);
            Account target = accountService.getAccount(toAcc);
            System.out.println("✅ Fund Transfer Completed Successfully!");
            System.out.printf("   Source Account (%s) Balance: $%.2f%n", fromAcc, source.getBalance());
            System.out.printf("   Target Account (%s) Balance: $%.2f%n", toAcc, target.getBalance());
        } else {
            System.out.println("❌ Fund transfer failed.");
        }
    }

    // =========================================================================
    // 4. STATEMENTS & HISTORY
    // =========================================================================
    private static void handleStatementMenu() {
        System.out.println("\n--- ACCOUNT STATEMENTS & TRANSACTION HISTORY ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        try {
            List<Transaction> statement = transactionService.getAccountStatement(accNo);
            System.out.println("\n=========================================================================================");
            System.out.println("                     ACCOUNT STATEMENT FOR: " + accNo);
            System.out.println("=========================================================================================");
            if (statement.isEmpty()) {
                System.out.println("No transactions found for this account.");
                return;
            }
            System.out.printf("%-8s | %-13s | %-10s | %-12s | %-12s | %-20s%n",
                    "Tx ID", "Type", "Amount", "Balance After", "Target Acc", "Timestamp");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (Transaction tx : statement) {
                System.out.printf("%-8d | %-13s | $%-9.2f | $%-11.2f | %-12s | %-20s%n",
                        tx.getTransactionId(),
                        tx.getTransactionType(),
                        tx.getAmount(),
                        tx.getBalanceAfter(),
                        tx.getTargetAccountNumber() != null ? tx.getTargetAccountNumber() : "-",
                        tx.getTimestamp());
            }
            System.out.println("=========================================================================================");
        } catch (Exception e) {
            System.out.println("❌ Error fetching statement: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. DATABASE DIAGNOSTICS & CONFIG
    // =========================================================================
    private static void handleDatabaseConfigMenu() {
        System.out.println("\n--- DATABASE DIAGNOSTICS & CONNECTION CONFIG ---");
        System.out.println("Current DB URL: " + DBConnection.getUrl());
        System.out.println("Current DB User: " + DBConnection.getUser());

        System.out.print("Testing Connection... ");
        boolean connected = DBConnection.testConnection();
        if (connected) {
            System.out.println("✅ CONNECTION OK");
        } else {
            System.out.println("❌ CONNECTION FAILED");
            System.out.println("Possible causes:");
            System.out.println("  1. MySQL Service is not running on localhost:3306");
            System.out.println("  2. 'banking_db' database has not been created yet (Run database/banking.sql script)");
            System.out.println("  3. Credentials (user/password) mismatch");
        }

        System.out.println("\nWould you like to update DB Connection details? (y/N): ");
        String opt = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(opt) || "yes".equalsIgnoreCase(opt)) {
            System.out.print("Enter JDBC URL [e.g. jdbc:mysql://localhost:3306/banking_db]: ");
            String url = scanner.nextLine().trim();
            System.out.print("Enter DB Username [e.g. root]: ");
            String user = scanner.nextLine().trim();
            System.out.print("Enter DB Password: ");
            String pass = scanner.nextLine();

            DBConnection.setCredentials(url, user, pass);
            System.out.print("Testing new credentials... ");
            if (DBConnection.testConnection()) {
                System.out.println("✅ Connected successfully!");
            } else {
                System.out.println("❌ Connection failed with new credentials.");
            }
        }
    }
}
