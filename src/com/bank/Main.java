package com.bank;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.model.Account;
import com.bank.model.Admin;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.service.BankService;
import com.bank.util.DBConnection;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final BankService bankService = new BankService();

    private static Customer loggedInCustomer = null;
    private static Admin loggedInAdmin = null;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    WELCOME TO SIMPLE BANKING MANAGEMENT SYSTEM  ");
        System.out.println("=================================================");
        System.out.println("Connecting to database...");

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Fatal: Could not establish database connection. Exiting application.");
            return;
        }

        boolean exitApp = false;
        while (!exitApp) {
            if (loggedInCustomer != null) {
                showCustomerDashboard();
            } else if (loggedInAdmin != null) {
                showAdminDashboard();
            } else {
                exitApp = showMainMenu();
            }
        }

        System.out.println("\nThank you for banking with us. Have a great day!");
        DBConnection.closeConnection();
        scanner.close();
    }

    private static boolean showMainMenu() {
        System.out.println("\n-------------------------------------------------");
        System.out.println("                   MAIN MENU                     ");
        System.out.println("-------------------------------------------------");
        System.out.println("1. Customer Registration");
        System.out.println("2. Customer Login");
        System.out.println("3. Admin Login");
        System.out.println("4. Exit");
        System.out.print("Please enter your choice (1-4): ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                handleCustomerRegistration();
                break;
            case 2:
                handleCustomerLogin();
                break;
            case 3:
                handleAdminLogin();
                break;
            case 4:
                return true;
            default:
                System.out.println("[!] Invalid choice. Please select an option between 1 and 4.");
        }
        return false;
    }

    private static void handleCustomerRegistration() {
        System.out.println("\n--- New Customer Registration ---");
        System.out.print("Enter Full Name: ");
        String name = getStringInput();
        System.out.print("Enter Email Address: ");
        String email = getStringInput();
        System.out.print("Enter Phone Number: ");
        String phone = getStringInput();
        System.out.print("Enter Password: ");
        String password = getStringInput();

        boolean registered = bankService.registerCustomer(name, email, phone, password);
        if (registered) {
            System.out.println("\n[SUCCESS] Registration successful! You can now log in.");
        } else {
            System.out.println("\n[FAIL] Registration failed. Email may already be registered.");
        }
    }

    private static void handleCustomerLogin() {
        System.out.println("\n--- Customer Login ---");
        System.out.print("Enter Registered Email: ");
        String email = getStringInput();
        System.out.print("Enter Password: ");
        String password = getStringInput();

        Customer customer = bankService.loginCustomer(email, password);
        if (customer != null) {
            loggedInCustomer = customer;
            System.out.println("\n[SUCCESS] Welcome back, " + customer.getName() + "!");
        } else {
            System.out.println("\n[FAIL] Invalid email or password. Please try again.");
        }
    }

    private static void showCustomerDashboard() {
        System.out.println("\n=================================================");
        System.out.println("               CUSTOMER DASHBOARD                ");
        System.out.println("   Logged in as: " + loggedInCustomer.getName() + " (" + loggedInCustomer.getEmail() + ")");
        System.out.println("=================================================");
        System.out.println("1. Open New Bank Account");
        System.out.println("2. View My Accounts");
        System.out.println("3. Check Account Balance");
        System.out.println("4. Deposit Money");
        System.out.println("5. Withdraw Money");
        System.out.println("6. Transfer Funds");
        System.out.println("7. View Transaction History");
        System.out.println("8. Logout");
        System.out.print("Please enter your choice (1-8): ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                handleOpenAccount();
                break;
            case 2:
                handleViewAccounts();
                break;
            case 3:
                handleCheckBalance();
                break;
            case 4:
                handleDeposit();
                break;
            case 5:
                handleWithdraw();
                break;
            case 6:
                handleTransfer();
                break;
            case 7:
                handleTransactionHistory();
                break;
            case 8:
                loggedInCustomer = null;
                System.out.println("\n[INFO] You have logged out successfully.");
                break;
            default:
                System.out.println("[!] Invalid choice. Please choose from 1 to 8.");
        }
    }

    private static void handleOpenAccount() {
        System.out.println("\n--- Open New Bank Account ---");
        System.out.println("Select Account Type:");
        System.out.println("1. SAVINGS");
        System.out.println("2. CURRENT");
        System.out.println("3. SALARY");
        System.out.print("Enter choice (1-3): ");

        int typeChoice = getIntInput();
        String type = "SAVINGS";
        if (typeChoice == 2) type = "CURRENT";
        else if (typeChoice == 3) type = "SALARY";

        System.out.print("Enter Initial Deposit Amount (₹, minimum 0): ");
        double initialDeposit = getDoubleInput();

        Account account = bankService.openAccount(loggedInCustomer.getCustomerId(), type, initialDeposit);
        if (account != null) {
            System.out.println("\n[SUCCESS] Account opened successfully!");
            System.out.println("Generated Account Number : " + account.getAccountNumber());
            System.out.println("Account Type             : " + account.getAccountType());
            System.out.printf("Current Balance          : ₹%.2f\n", account.getBalance());
        } else {
            System.out.println("\n[FAIL] Failed to open account.");
        }
    }

    private static void handleViewAccounts() {
        System.out.println("\n--- My Bank Accounts ---");
        List<Account> accounts = bankService.getCustomerAccounts(loggedInCustomer.getCustomerId());
        if (accounts.isEmpty()) {
            System.out.println("You do not have any active bank accounts. Choose option 1 to open one.");
            return;
        }

        System.out.println("----------------------------------------------------------------------");
        System.out.printf("%-15s %-15s %-15s\n", "Account Number", "Account Type", "Balance (₹)");
        System.out.println("----------------------------------------------------------------------");
        for (Account acc : accounts) {
            System.out.printf("%-15d %-15s ₹%-15.2f\n", acc.getAccountNumber(), acc.getAccountType(), acc.getBalance());
        }
        System.out.println("----------------------------------------------------------------------");
    }

    private static void handleCheckBalance() {
        System.out.print("\nEnter Account Number: ");
        long accNo = getLongInput();

        try {
            double balance = bankService.checkBalance(accNo);
            System.out.printf("\n[INFO] Current Balance for A/C %d is: ₹%.2f\n", accNo, balance);
        } catch (AccountNotFoundException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private static void handleDeposit() {
        System.out.println("\n--- Deposit Funds ---");
        System.out.print("Enter Account Number: ");
        long accNo = getLongInput();
        System.out.print("Enter Amount to Deposit (₹): ");
        double amount = getDoubleInput();

        try {
            bankService.deposit(accNo, amount);
        } catch (AccountNotFoundException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private static void handleWithdraw() {
        System.out.println("\n--- Withdraw Funds ---");
        System.out.print("Enter Account Number: ");
        long accNo = getLongInput();
        System.out.print("Enter Amount to Withdraw (₹): ");
        double amount = getDoubleInput();

        try {
            bankService.withdraw(accNo, amount);
        } catch (AccountNotFoundException | InsufficientBalanceException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private static void handleTransfer() {
        System.out.println("\n--- Fund Transfer ---");
        System.out.print("Enter Your Account Number (From): ");
        long fromAcc = getLongInput();
        System.out.print("Enter Beneficiary Account Number (To): ");
        long toAcc = getLongInput();
        System.out.print("Enter Transfer Amount (₹): ");
        double amount = getDoubleInput();

        try {
            bankService.transferMoney(fromAcc, toAcc, amount);
        } catch (AccountNotFoundException | InsufficientBalanceException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private static void handleTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        System.out.print("Enter Account Number: ");
        long accNo = getLongInput();

        try {
            List<Transaction> list = bankService.getTransactionHistory(accNo);
            if (list.isEmpty()) {
                System.out.println("No transactions found for Account " + accNo);
                return;
            }

            System.out.println("--------------------------------------------------------------------------------");
            for (Transaction tx : list) {
                System.out.println(tx);
            }
            System.out.println("--------------------------------------------------------------------------------");
        } catch (AccountNotFoundException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private static void handleAdminLogin() {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Enter Admin Username: ");
        String username = getStringInput();
        System.out.print("Enter Admin Password: ");
        String password = getStringInput();

        Admin admin = bankService.loginAdmin(username, password);
        if (admin != null) {
            loggedInAdmin = admin;
            System.out.println("\n[SUCCESS] Admin access granted. Welcome, " + admin.getUsername() + "!");
        } else {
            System.out.println("\n[FAIL] Invalid admin credentials.");
        }
    }

    private static void showAdminDashboard() {
        System.out.println("\n=================================================");
        System.out.println("                 ADMIN DASHBOARD                 ");
        System.out.println("=================================================");
        System.out.println("1. View All Registered Customers");
        System.out.println("2. View All Bank Accounts");
        System.out.println("3. Search Account Details");
        System.out.println("4. View All Transaction Logs");
        System.out.println("5. Logout");
        System.out.print("Please enter your choice (1-5): ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                List<Customer> customers = bankService.getAllCustomers();
                System.out.println("\n--- Registered Customers (" + customers.size() + ") ---");
                for (Customer c : customers) {
                    System.out.println(c);
                }
                break;
            case 2:
                List<Account> accounts = bankService.getAllAccounts();
                System.out.println("\n--- Total Bank Accounts (" + accounts.size() + ") ---");
                for (Account a : accounts) {
                    System.out.println(a);
                }
                break;
            case 3:
                System.out.print("\nEnter Account Number to Search: ");
                long searchAcc = getLongInput();
                Account acc = bankService.searchAccount(searchAcc);
                if (acc != null) {
                    System.out.println("\n[FOUND] " + acc);
                } else {
                    System.out.println("\n[NOT FOUND] Account " + searchAcc + " does not exist.");
                }
                break;
            case 4:
                List<Transaction> txList = bankService.getAllTransactions();
                System.out.println("\n--- Global Transaction Logs (" + txList.size() + ") ---");
                for (Transaction t : txList) {
                    System.out.println(t);
                }
                break;
            case 5:
                loggedInAdmin = null;
                System.out.println("\n[INFO] Admin logged out.");
                break;
            default:
                System.out.println("[!] Invalid choice. Please choose from 1 to 5.");
        }
    }

    private static int getIntInput() {
        while (scanner.hasNextLine()) {
            try {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    return Integer.parseInt(line);
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid integer. Please enter a valid number: ");
            }
        }
        return 4;
    }

    private static long getLongInput() {
        while (scanner.hasNextLine()) {
            try {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    return Long.parseLong(line);
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid account number. Please enter digits only: ");
            }
        }
        return 0L;
    }

    private static double getDoubleInput() {
        while (scanner.hasNextLine()) {
            try {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    return Double.parseDouble(line);
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid amount. Please enter a valid decimal number: ");
            }
        }
        return 0.0;
    }

    private static String getStringInput() {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
        }
        return "";
    }
}
