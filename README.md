# 🏦 NovaBank India - Banking Management System & Web Dashboard

A robust, multi-tier Java Banking Management System with an ultra-modern **Vanilla JS Single Page Application (SPA) Web Dashboard** (built without React) and interactive CLI. Built using standard Java OOP principles, Data Access Object (DAO) patterns, embedded HTTP REST server, and JDBC persistence with MySQL.

---

## 🌐 Web Dashboard Highlights (No React)

- **Indian Locale & INR Currency**: All financial metrics, statements, and transaction vouchers format using the Indian Rupee (`₹`) symbol and Indian numbering format (`₹8,00,500.50`).
- **Dark Glassmorphism Design**: High-end UI with HSL color palettes, subtle glowing borders, backdrop filters, responsive navigation sidebar, and digital clock widget.
- **Visual Analytics**: Interactive Chart.js doughnut chart for Savings vs Current account distribution and bar chart for transaction trends.
- **Customer Directory**: Real-time customer search & filter with contact cards and registration modal.
- **Financial Operations Hub**: Deposit, Withdraw (with minimum balance & overdraft validation rules), and Atomic Transfer (IMPS/UPI).
- **Statement Audit Logs & CSV Export**: One-click CSV download of transaction statements and printable official receipt voucher generator.

---

## 📁 Project Structure

```
Banking-Management-System/
│
├── src/
│   └── banking/
│       ├── model/             # Customer, Account, SavingsAccount, CurrentAccount, Transaction
│       ├── dao/               # CustomerDAO, AccountDAO, TransactionDAO
│       ├── service/           # CustomerService, AccountService, TransactionService
│       ├── util/              # DBConnection, InputValidator
│       ├── web/               # WebServer (Embedded HTTP REST API & Static File Server)
│       └── Main.java          # CLI Application Driver & Web Server Launcher
│
├── web/
│   ├── index.html             # Vanilla HTML5 SPA structure & modals
│   ├── styles.css             # Modern Dark Glassmorphism CSS Design System
│   └── app.js                 # Vanilla ES6 JS Logic & Chart.js integration
│
├── database/
│   └── banking.sql            # SQL DDL & Indian Seed Data (Rajesh Kumar, Priya Sharma, Aarav Patel)
│
├── lib/                       # MySQL Connector JAR
└── README.md
```

---

## 🚀 Setup & Running Guide

### 1. Compile Java Source Files
From the project root directory:

```cmd
javac -cp "lib/*" -d bin src/banking/model/*.java src/banking/util/*.java src/banking/dao/*.java src/banking/service/*.java src/banking/web/*.java src/banking/Main.java
```

### 2. Launch Web Server Mode
```cmd
java -cp "bin;lib/*" banking.Main --web
```
Open **[http://localhost:8080](http://localhost:8080)** in your browser!

### 3. Launch CLI Mode
```cmd
java -cp "bin;lib/*" banking.Main
```
Select **Option 6** from the CLI menu to launch the Web Server.
