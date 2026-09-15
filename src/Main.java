import dao.CustomerDAO;
import dao.AccountDAO;
import dao.TransactionDAO;
import model.Customer;
import model.Account;
import model.Transaction;

import java.util.List;
import java.util.Scanner;

/**
 * Main Class - Entry Point of Banking Management System
 * Provides a clean console-based interactive UI for customers.
 * Demonstrates:
 * 1. Modular separation of UI logic and Data Access (DAO pattern)
 * 2. Exception-safe user input parsing
 * 3. User session state management
 */
public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static CustomerDAO customerDAO = new CustomerDAO();
    private static AccountDAO accountDAO = new AccountDAO();
    private static TransactionDAO transactionDAO = new TransactionDAO();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("    WELCOME TO BANKING MANAGEMENT SYSTEM");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    loginCustomer();
                    break;
                case 3:
                    running = false;
                    System.out.println("\nThank you for using Banking Management System. Goodbye!");
                    break;
                default:
                    System.out.println("❌ Invalid choice! Please select an option between 1 and 3.");
            }
        }
        scanner.close();
    }

    // ============================================================
    // MENU DISPLAY METHODS
    // ============================================================

    private static void displayMainMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.println("===============================");
    }

    private static void displayCustomerMenu(Customer loggedInCustomer) {
        System.out.println("\n========== CUSTOMER MENU ==========");
        System.out.println("Welcome, " + loggedInCustomer.getName() + "!");
        System.out.println("1. View Profile");
        System.out.println("2. Create Account");
        System.out.println("3. Check Balance");
        System.out.println("4. Deposit Money");
        System.out.println("5. Withdraw Money");
        System.out.println("6. Transfer Money");
        System.out.println("7. Transaction History");
        System.out.println("8. Logout");
        System.out.println("===================================");
    }

    // ============================================================
    // AUTHENTICATION FLOWS
    // ============================================================

    private static void registerCustomer() {
        System.out.println("\n--- Customer Registration ---");
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter Address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("❌ Name, Email, and Password cannot be empty!");
            return;
        }

        Customer newCustomer = new Customer(name, email, phone, address, password);
        boolean success = customerDAO.registerCustomer(newCustomer);

        if (success) {
            System.out.println("✅ Customer registered successfully! You can now log in.");
        } else {
            System.out.println("❌ Registration failed! Email may already be in use.");
        }
    }

    private static void loginCustomer() {
        System.out.println("\n--- Customer Login ---");
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        Customer customer = customerDAO.loginCustomer(email, password);
        if (customer != null) {
            System.out.println("✅ Login successful!");
            handleCustomerSession(customer);
        } else {
            System.out.println("❌ Invalid credentials! Please check your email and password.");
        }
    }

    // ============================================================
    // CUSTOMER SESSION HANDLER
    // ============================================================

    private static void handleCustomerSession(Customer customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            displayCustomerMenu(customer);
            int choice = readIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    viewProfile(customer);
                    break;
                case 2:
                    createBankAccount(customer);
                    break;
                case 3:
                    checkBalance(customer);
                    break;
                case 4:
                    depositMoney(customer);
                    break;
                case 5:
                    withdrawMoney(customer);
                    break;
                case 6:
                    transferMoney(customer);
                    break;
                case 7:
                    viewTransactionHistory(customer);
                    break;
                case 8:
                    loggedIn = false;
                    System.out.println("✅ You have logged out successfully.");
                    break;
                default:
                    System.out.println("❌ Invalid choice! Please select an option between 1 and 8.");
            }
        }
    }

    // ============================================================
    // CUSTOMER OPERATIONS
    // ============================================================

    private static void viewProfile(Customer customer) {
        System.out.println("\n--- My Profile ---");
        Customer currentInfo = customerDAO.getCustomerById(customer.getCustomerId());
        if (currentInfo != null) {
            System.out.println("Customer ID : " + currentInfo.getCustomerId());
            System.out.println("Name        : " + currentInfo.getName());
            System.out.println("Email       : " + currentInfo.getEmail());
            System.out.println("Phone       : " + currentInfo.getPhone());
            System.out.println("Address     : " + currentInfo.getAddress());
        } else {
            System.out.println(customer);
        }
    }

    private static void createBankAccount(Customer customer) {
        System.out.println("\n--- Create Bank Account ---");
        System.out.println("Select Account Type:");
        System.out.println("1. Savings");
        System.out.println("2. Current");
        int typeChoice = readIntInput("Choice (1 or 2): ");

        String accountType = (typeChoice == 2) ? "Current" : "Savings";
        double initialBalance = readDoubleInput("Enter Initial Balance ($): ");

        if (initialBalance < 0) {
            System.out.println("❌ Initial balance cannot be negative!");
            return;
        }

        // Generate unique 10-digit account number (e.g. ACC10293847)
        String accountNumber = "ACC" + (10000000 + (int)(Math.random() * 89999999));

        Account newAccount = new Account(
                customer.getCustomerId(),
                accountNumber,
                accountType,
                initialBalance,
                "ACTIVE"
        );

        boolean success = accountDAO.createAccount(newAccount);
        if (success) {
            System.out.println("✅ Bank Account created successfully!");
            System.out.println("   Account Number : " + accountNumber);
            System.out.println("   Account Type   : " + accountType);
            System.out.println("   Initial Balance: $" + String.format("%.2f", initialBalance));
        } else {
            System.out.println("❌ Failed to create bank account. Please try again.");
        }
    }

    private static void checkBalance(Customer customer) {
        System.out.println("\n--- Check Balance ---");
        Account acc = selectAccount(customer);
        if (acc != null) {
            double balance = accountDAO.getBalance(acc.getAccountNumber());
            System.out.println("----------------------------------------");
            System.out.println("Account Number : " + acc.getAccountNumber());
            System.out.println("Account Type   : " + acc.getAccountType());
            System.out.println("Current Balance: $" + String.format("%.2f", balance));
            System.out.println("----------------------------------------");
        }
    }

    private static void depositMoney(Customer customer) {
        System.out.println("\n--- Deposit Money ---");
        Account acc = selectAccount(customer);
        if (acc != null) {
            double amount = readDoubleInput("Enter deposit amount ($): ");
            if (amount <= 0) {
                System.out.println("❌ Deposit amount must be greater than zero!");
                return;
            }
            boolean success = accountDAO.deposit(acc.getAccountNumber(), amount);
            if (success) {
                double newBalance = accountDAO.getBalance(acc.getAccountNumber());
                System.out.println("✅ Deposit successful! Updated Balance: $" + String.format("%.2f", newBalance));
            }
        }
    }

    private static void withdrawMoney(Customer customer) {
        System.out.println("\n--- Withdraw Money ---");
        Account acc = selectAccount(customer);
        if (acc != null) {
            double amount = readDoubleInput("Enter withdrawal amount ($): ");
            if (amount <= 0) {
                System.out.println("❌ Withdrawal amount must be greater than zero!");
                return;
            }
            boolean success = accountDAO.withdraw(acc.getAccountNumber(), amount);
            if (success) {
                double newBalance = accountDAO.getBalance(acc.getAccountNumber());
                System.out.println("✅ Withdrawal successful! Updated Balance: $" + String.format("%.2f", newBalance));
            }
        }
    }

    private static void transferMoney(Customer customer) {
        System.out.println("\n--- Transfer Money ---");
        Account senderAcc = selectAccount(customer);
        if (senderAcc != null) {
            System.out.print("Enter Receiver's Account Number: ");
            String receiverAccNum = scanner.nextLine().trim();

            if (receiverAccNum.isEmpty()) {
                System.out.println("❌ Receiver account number cannot be empty!");
                return;
            }

            double amount = readDoubleInput("Enter transfer amount ($): ");
            if (amount <= 0) {
                System.out.println("❌ Transfer amount must be greater than zero!");
                return;
            }

            accountDAO.transfer(senderAcc.getAccountNumber(), receiverAccNum, amount);
        }
    }

    private static void viewTransactionHistory(Customer customer) {
        System.out.println("\n--- Transaction History ---");
        Account acc = selectAccount(customer);
        if (acc != null) {
            List<Transaction> transactions = transactionDAO.getTransactionHistory(acc.getAccountId());
            if (transactions.isEmpty()) {
                System.out.println("ℹ️ No transactions found for account " + acc.getAccountNumber());
            } else {
                System.out.println("\nTransaction History for Account: " + acc.getAccountNumber());
                System.out.printf("%-15s %-25s %-15s %-25s\n", "Txn ID", "Type", "Amount", "Date & Time");
                System.out.println("-----------------------------------------------------------------------------");
                for (Transaction t : transactions) {
                    System.out.printf("%-15d %-25s $%-14.2f %-25s\n",
                            t.getTransactionId(),
                            t.getTransactionType(),
                            t.getAmount(),
                            t.getTransactionDate().toString());
                }
                System.out.println("-----------------------------------------------------------------------------");
            }
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private static Account selectAccount(Customer customer) {
        List<Account> accounts = accountDAO.getAccountsByCustomerId(customer.getCustomerId());
        if (accounts.isEmpty()) {
            System.out.println("⚠️ You do not have any bank accounts yet. Please create an account first (Option 2).");
            return null;
        }

        if (accounts.size() == 1) {
            return accounts.get(0);
        }

        System.out.println("Select your bank account:");
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            System.out.println((i + 1) + ". " + acc.getAccountNumber() + " (" + acc.getAccountType() + ") - Balance: $" + String.format("%.2f", acc.getBalance()));
        }

        int choice = readIntInput("Enter account option: ");
        if (choice < 1 || choice > accounts.size()) {
            System.out.println("❌ Invalid account selection!");
            return null;
        }

        return accounts.get(choice - 1);
    }

    private static int readIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format! Please enter a valid integer.");
            }
        }
    }

    private static double readDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid amount format! Please enter a valid decimal number.");
            }
        }
    }
}
