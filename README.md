# 🏦 Banking Management System

A robust, multi-tier Java CLI Banking Management System built using standard Java Object-Oriented Programming (OOP) principles, Data Access Object (DAO) design patterns, and JDBC persistence with MySQL database.

---

## 📁 Project Structure

```
Banking-Management-System/
│
├── src/
│   └── banking/
│       │
│       ├── model/
│       │   ├── Customer.java            # Customer entity
│       │   ├── Account.java             # Base abstract Account class
│       │   ├── SavingsAccount.java      # Savings account (minimum balance rules & interest)
│       │   ├── CurrentAccount.java      # Current account (overdraft limits)
│       │   └── Transaction.java         # Financial transaction log model
│       │
│       ├── dao/
│       │   ├── CustomerDAO.java         # Database CRUD operations for Customers
│       │   ├── AccountDAO.java          # Database CRUD operations for Accounts
│       │   └── TransactionDAO.java      # Database logging operations for Transactions
│       │
│       ├── service/
│       │   ├── CustomerService.java     # Customer registration & validation business logic
│       │   ├── AccountService.java      # Account opening, deposit, withdraw, transfer logic
│       │   └── TransactionService.java  # Statement generation & history reporting logic
│       │
│       ├── util/
│       │   ├── DBConnection.java        # JDBC Connection manager & connectivity diagnostics
│       │   └── InputValidator.java      # Input validation & formatting helpers
│       │
│       └── Main.java                    # Application driver & interactive CLI user interface
│
├── database/
│   └── banking.sql                      # SQL DDL & DML database setup script with seed data
│
├── lib/
│   └── mysql-connector-j.jar            # Place MySQL JDBC Connector Driver JAR here
│
├── README.md                            # Complete setup & usage guide
└── .gitignore                           # Git ignore rules for Java compilation artifacts
```

---

## ✨ Key Features

1. **Customer Management**: Register new customers with email/phone format validation, search customer profile by ID/Email, and list all registered customers.
2. **Account Management**: Open Savings Accounts (with $500 minimum balance enforcement) and Current Accounts (with $1,000 overdraft limit support).
3. **Financial Operations**:
   - **Deposit**: Secure deposit operations updating balances and logging transactions.
   - **Withdrawal**: Polymorphic withdrawal logic validating balance constraints based on account type.
   - **Fund Transfer**: Atomic multi-account transfers using SQL database transaction locks (`commit` / `rollback`) to prevent partial failures.
4. **Statements & Audit Logs**: Detailed account statements formatted as ASCII tables showing deposits, withdrawals, transfers, target accounts, and timestamps.
5. **Database Diagnostics**: Built-in connectivity test and configuration updater in the CLI menu.

---

## 🛠️ Prerequisites

- **Java Development Kit (JDK)**: Java 8 or higher (`javac` and `java` commands installed).
- **MySQL Database Server**: MySQL Server 5.7+ or 8.0+ running on `localhost:3306`.
- **MySQL JDBC Driver**: `mysql-connector-j-8.x.x.jar` downloaded into the `lib/` directory.

---

## 🚀 Setup & Execution Guide

### Step 1: Database Setup
1. Open your MySQL client (Command Line or Workbench).
2. Execute the `database/banking.sql` script to create the database, tables, and pre-populated seed data:
   ```bash
   mysql -u root -p < database/banking.sql
   ```
   *Or copy and paste the contents of `database/banking.sql` into MySQL Workbench.*

### Step 2: Download MySQL JDBC Driver
1. Download MySQL Connector/J driver (JAR file) from [MySQL Official Website](https://dev.mysql.com/downloads/connector/j/).
2. Place `mysql-connector-j-8.x.x.jar` inside the `lib/` directory as `lib/mysql-connector-j.jar`.

### Step 3: Compilation
From the project root directory, compile all Java source files into a `bin/` directory:

#### On Windows (Command Prompt / PowerShell):
```cmd
javac -cp "lib/mysql-connector-j.jar;lib/*" -d bin src/banking/model/*.java src/banking/util/*.java src/banking/dao/*.java src/banking/service/*.java src/banking/Main.java
```

#### On Linux / macOS:
```bash
javac -cp "lib/mysql-connector-j.jar:lib/*" -d bin src/banking/model/*.java src/banking/util/*.java src/banking/dao/*.java src/banking/service/*.java src/banking/Main.java
```

---

### Step 4: Running the Application

#### On Windows (Command Prompt / PowerShell):
```cmd
java -cp "bin;lib/mysql-connector-j.jar;lib/*" banking.Main
```

#### On Linux / macOS:
```bash
java -cp "bin:lib/mysql-connector-j.jar:lib/*" banking.Main
```

---

## 💻 CLI Menu Overview

```text
==========================================================
           🏦 BANKING MANAGEMENT SYSTEM 🏦               
==========================================================

---------------- MAIN MENU ----------------
1. Customer Management
2. Account Management
3. Financial Operations (Deposit / Withdraw / Transfer)
4. Account Statements & Transaction History
5. Database Connection Diagnostics
6. Exit
```

---

## 📝 Default Database Configuration

- **Database URL**: `jdbc:mysql://localhost:3306/banking_db`
- **Username**: `root`
- **Password**: `password`

*(Note: You can update credentials at runtime via **Option 5: Database Connection Diagnostics** in the application menu, or directly edit `src/banking/util/DBConnection.java`)*.
