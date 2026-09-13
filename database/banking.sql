-- =========================================================
-- Banking Management System Database Schema
-- Database: banking_db
-- =========================================================

CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

-- ---------------------------------------------------------
-- Table Structure: Customers
-- ---------------------------------------------------------
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------
-- Table Structure: Accounts
-- ---------------------------------------------------------
CREATE TABLE accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id INT NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Table Structure: Transactions
-- ---------------------------------------------------------
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount DOUBLE NOT NULL,
    balance_after DOUBLE NOT NULL,
    target_account_number VARCHAR(20) DEFAULT NULL,
    remarks VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- Sample Seed Data (Indian Context)
-- ---------------------------------------------------------
INSERT INTO customers (name, email, phone, address) VALUES
('Rajesh Kumar', 'rajesh.kumar@example.in', '+91 98765 43210', '12, MG Road, Indiranagar, Bengaluru, Karnataka 560038'),
('Priya Sharma', 'priya.sharma@example.in', '+91 91234 56789', '45, Connaught Place, New Delhi 110001'),
('Aarav Patel', 'aarav.patel@example.in', '+91 99887 76655', '88, Bandra West, Mumbai, Maharashtra 400050');

INSERT INTO accounts (account_number, customer_id, account_type, balance) VALUES
('SAV1001', 1, 'SAVINGS', 125000.00),
('CUR1002', 1, 'CURRENT', 250000.00),
('SAV2001', 2, 'SAVINGS', 340500.50),
('CUR3001', 3, 'CURRENT', 85000.00);

INSERT INTO transactions (account_number, transaction_type, amount, balance_after, target_account_number, remarks) VALUES
('SAV1001', 'DEPOSIT', 125000.00, 125000.00, NULL, 'Initial Account Opening Deposit'),
('CUR1002', 'DEPOSIT', 300000.00, 300000.00, NULL, 'Initial Business Deposit'),
('CUR1002', 'WITHDRAWAL', 50000.00, 250000.00, NULL, 'ATM Cash Withdrawal'),
('SAV2001', 'DEPOSIT', 340500.50, 340500.50, NULL, 'Salary Credit'),
('CUR3001', 'DEPOSIT', 85000.00, 85000.00, NULL, 'UPI Transfer Received');
