# Banking Management System 🏦

A lightweight, beginner-friendly **Banking Management System** built using **Core Java, Java OOPs, JDBC, and MySQL**.

This project does **NOT** use Spring Boot, Spring Framework, Hibernate, JPA, REST APIs, Maven, or any external framework. It focuses on core Java principles, clean code structure, data access objects (DAO pattern), database persistence with MySQL, and explicit JDBC transaction management (`commit` and `rollback`).

---

## 📋 Features

1. **Customer Registration**: New customers can register with Name, Email, Phone, Address, and Password.
2. **Customer Login**: Secure authentication using Email and Password.
3. **Create Bank Account**: Customers can create multiple accounts (Savings or Current) with unique account numbers and initial balance.
4. **Check Balance**: View real-time balance for any owned account.
5. **Deposit Money**: Deposit funds into account with validation (`amount > 0`) and automatic transaction logging.
6. **Withdraw Money**: Withdraw funds with balance check validation (`balance >= amount`).
7. **Transfer Money (ACID Compliant)**: Secure money transfer between accounts utilizing **JDBC Transaction Management** (`setAutoCommit(false)`, `commit()`, and `rollback()`) to ensure no money is lost if an operation fails.
8. **Transaction History**: View detailed history of all deposits, withdrawals, and transfers with timestamps.

---

## 🛠️ Technologies Used

- **Programming Language**: Core Java (JDK 8+)
- **Concepts**: Java OOPs (Encapsulation, Abstraction, Polymorphism), Exception Handling, DAO Pattern
- **Database**: MySQL Database
- **Database Connectivity**: Plain JDBC (`DriverManager`, `PreparedStatement`, `ResultSet`, `Connection`)

---

## 📁 Project Structure

```
BankingManagementSystem/
│
├── lib/
│   └── mysql-connector-j-8.3.0.jar # MySQL JDBC Driver JAR
│
├── src/
│   ├── model/
│   │   ├── Customer.java       # Encapsulated Customer model (POJO)
│   │   ├── Account.java        # Encapsulated Account model (POJO)
│   │   └── Transaction.java    # Encapsulated Transaction model (POJO)
│   │
│   ├── dao/
│   │   ├── DBConnection.java   # Helper for MySQL JDBC Connection
│   │   ├── CustomerDAO.java    # DAO for customer registration & login
│   │   ├── AccountDAO.java     # DAO for deposit, withdraw, transfer
│   │   └── TransactionDAO.java # DAO for transaction history logs
│   │
│   ├── DBConnection.java       # Root wrapper for DB connection
│   └── Main.java               # Interactive console-based menu interface
│
├── database/
│   └── banking.sql             # MySQL Database setup script & sample data
│
└── README.md                   # Project documentation & interview guide
```

---

## 🗄️ Database Setup (MySQL)

1. Open your MySQL client (MySQL Workbench, Command Line, or phpMyAdmin).
2. Run the script provided in `database/banking.sql`:

```sql
CREATE DATABASE IF NOT EXISTS banking;
USE banking;

-- Run the complete database/banking.sql file to create tables & sample records
```

### Table Schemas:
- **`customers`**: `customer_id`, `name`, `email` (UNIQUE), `phone`, `address`, `password`
- **`accounts`**: `account_id`, `customer_id` (FK), `account_number` (UNIQUE), `account_type`, `balance`, `status`
- **`transactions`**: `transaction_id`, `account_id` (FK), `transaction_type`, `amount`, `transaction_date`

---

## ⚙️ How to Configure MySQL Credentials

Open [`src/dao/DBConnection.java`](file:///c:/Users/Deekshith%20Goud/OneDrive/Desktop/my%20all%20projects/bank%20project/src/dao/DBConnection.java) and update your MySQL credentials:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/banking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String DB_USER = "root";       // Change to your MySQL username
private static final String DB_PASSWORD = "root";   // Change to your MySQL password
```

---

## 🔌 How to Add MySQL JDBC Driver JAR

Download `mysql-connector-j-8.x.x.jar` (MySQL Connector/J) from Oracle MySQL Download page or Maven Central.

### Adding via Command Line (`javac` & `java`):

- **Compile**:
  ```bash
  javac -cp ".;mysql-connector-j-8.0.33.jar" -d bin src/model/*.java src/dao/*.java src/*.java
  ```

- **Run**:
  ```bash
  java -cp "bin;mysql-connector-j-8.0.33.jar" Main
  ```

### Adding in IDEs:

- **Eclipse**: Right-click project ➔ `Build Path` ➔ `Add External Archives` ➔ Select `mysql-connector-j-8.x.x.jar`.
- **IntelliJ IDEA**: `File` ➔ `Project Structure` ➔ `Libraries` ➔ Click `+` ➔ Select `mysql-connector-j-8.x.x.jar`.
- **VS Code**: Under `JAVA PROJECTS` view ➔ Expand `Referenced Libraries` ➔ Click `+` ➔ Select `mysql-connector-j-8.x.x.jar`.

---

## 🔑 Sample Credentials (from `banking.sql`)

| Email | Password | Account Number | Account Type | Balance |
|---|---|---|---|---|
| `john@example.com` | `password123` | `ACC10000001` | Savings | $5,000.00 |
| `jane@example.com` | `password123` | `ACC10000002` | Current | $10,000.00 |

---

## 🎓 Technical Interview Q&A & Concept Guide

When presenting this project in a Java interview, highlight these key topics:

### 1. Object-Oriented Programming (OOP)
- **Encapsulation**: Used in `Customer.java`, `Account.java`, and `Transaction.java` by marking fields `private` and exposing public `getters` and `setters`.
- **Data Access Object (DAO) Pattern**: Decouples presentation logic (`Main.java`) from SQL database interaction (`CustomerDAO`, `AccountDAO`, `TransactionDAO`).

### 2. JDBC Concepts
- **`DriverManager.getConnection()`**: Obtains physical connection to MySQL.
- **`PreparedStatement` vs `Statement`**: We use `PreparedStatement` everywhere because it pre-compiles SQL queries with input placeholders (`?`), providing strong protection against **SQL Injection attacks**.
- **Try-With-Resources**: Implemented across all DAOs to automatically close `Connection`, `PreparedStatement`, and `ResultSet` objects, avoiding database memory/connection leaks.

### 3. JDBC Transaction Management (ACID Compliance)
In `AccountDAO.transfer()`, money transfer requires 2 DB updates (deduct sender, credit receiver) + 2 transaction logs. To prevent partial money loss:
```java
conn.setAutoCommit(false); // 1. Start manual transaction boundary

// Execute debit from sender
// Execute credit to receiver
// Log transactions

conn.commit(); // 2. Commit all changes atomically

// In catch block:
conn.rollback(); // 3. Rollback changes if any exception occurs
```

---

## 🚀 Future Improvements

- Add password encryption / hashing (BCrypt / SHA-256).
- Implement mini statement generation (PDF/Text export).
- Implement Interest Calculation service for Savings accounts.
