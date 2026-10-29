# Simple Banking Management System (Core Java + JDBC + MySQL)

A clean, modular, and beginner-friendly console-based Banking Management System built from scratch using pure **Core Java**, **JDBC (Java Database Connectivity)**, **MySQL**, and **Object-Oriented Programming (OOP)** principles.

---

## 📁 1. Complete Project Structure

```
BankingManagementSystem/
│
├── src/
│   └── com/
│       └── bank/
│           ├── Main.java               # Application Entry Point & Console UI
│           │
│           ├── model/                  # Data Entities (Encapsulated POJOs)
│           │   ├── Customer.java
│           │   ├── Account.java
│           │   ├── Transaction.java
│           │   └── Admin.java
│           │
│           ├── dao/                    # Data Access Objects (JDBC Queries)
│           │   ├── CustomerDAO.java
│           │   ├── AccountDAO.java
│           │   └── TransactionDAO.java
│           │
│           ├── service/                # Business Logic & ACID Transactions
│           │   └── BankService.java
│           │
│           ├── util/                   # Database Connection Utility (Singleton)
│           │   └── DBConnection.java
│           │
│           └── exception/              # Custom Banking Exceptions
│               ├── InsufficientBalanceException.java
│               └── AccountNotFoundException.java
│
├── lib/
│   ├── mysql-connector-j.jar           # MySQL JDBC Driver Library
│   └── h2.jar                          # Zero-config Local Persistence Engine
│
├── database/
│   └── schema.sql                      # Database Schema & Initial Data
│
├── run.bat                             # 1-Click Build & Run Script for Windows
└── README.md                           # Documentation & Interview Guide
```

---

## 🚀 2. How to Run the Project

### Option A: Using `run.bat` (Recommended)
Double click `run.bat` or in terminal run:
```cmd
.\run.bat
```

### Option B: In VS Code
1. Open `src/com/bank/Main.java`.
2. Press **F5** or click the **▷ Run** button.

---

## 🎯 3. Pre-seeded Demo Credentials

| Role | Username / Email | Password | Pre-seeded Account |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | N/A (Can view all accounts & logs) |
| **Customer** | `rahul@example.com` | `pass123` | A/C No: `100001` (Balance: ₹5000.00) |
