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
-- Sample Seed Data
-- ---------------------------------------------------------
INSERT INTO customers (name, email, phone, address) VALUES
('John Doe', 'john.doe@example.com', '9876543210', '123 Main Street, New York, NY'),
('Alice Smith', 'alice.smith@example.com', '9123456789', '456 Oak Avenue, Los Angeles, CA'),
('Robert Johnson', 'robert.j@example.com', '9988776655', '789 Pine Road, Chicago, IL');

INSERT INTO accounts (account_number, customer_id, account_type, balance) VALUES
('SAV1001', 1, 'SAVINGS', 2500.00),
('CUR1002', 1, 'CURRENT', 5000.00),
('SAV1003', 2, 'SAVINGS', 1500.00),
('CUR1004', 3, 'CURRENT', 3200.00);

INSERT INTO transactions (account_number, transaction_type, amount, balance_after, target_account_number, remarks) VALUES
('SAV1001', 'DEPOSIT', 2500.00, 2500.00, NULL, 'Initial Deposit'),
('CUR1002', 'DEPOSIT', 5000.00, 5000.00, NULL, 'Initial Deposit'),
('SAV1003', 'DEPOSIT', 1500.00, 1500.00, NULL, 'Initial Deposit'),
('CUR1004', 'DEPOSIT', 3200.00, 3200.00, NULL, 'Initial Deposit');
