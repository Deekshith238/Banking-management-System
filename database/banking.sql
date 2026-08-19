-- ============================================================
-- Banking Management System - Database Setup Script
-- Database: MySQL
-- ============================================================

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS banking;

-- Step 2: Use Database
USE banking;

-- Step 3: Create 'customers' table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    address VARCHAR(200) NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Step 4: Create 'accounts' table
CREATE TABLE IF NOT EXISTS accounts (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- Step 5: Create 'transactions' table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- ============================================================
-- Sample Data Insertion (For Testing & Verification)
-- ============================================================

-- Insert Sample Customers (Password: password123)
INSERT INTO customers (name, email, phone, address, password) VALUES
('John Doe', 'john@example.com', '9876543210', '123 Main St, New York', 'password123'),
('Jane Smith', 'jane@example.com', '9123456789', '456 Oak Ave, California', 'password123');

-- Insert Sample Accounts for John (customer_id: 1) and Jane (customer_id: 2)
INSERT INTO accounts (customer_id, account_number, account_type, balance, status) VALUES
(1, 'ACC10000001', 'Savings', 5000.00, 'ACTIVE'),
(2, 'ACC10000002', 'Current', 10000.00, 'ACTIVE');

-- Insert Sample Transactions
INSERT INTO transactions (account_id, transaction_type, amount) VALUES
(1, 'DEPOSIT', 5000.00),
(2, 'DEPOSIT', 10000.00);

-- Display table status (optional summary query)
SELECT 'Customers Loaded:' AS Info, COUNT(*) FROM customers;
SELECT 'Accounts Loaded:' AS Info, COUNT(*) FROM accounts;
SELECT 'Transactions Loaded:' AS Info, COUNT(*) FROM transactions;
