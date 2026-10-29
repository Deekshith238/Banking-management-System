-- ====================================================================
-- Banking Management System - Database Schema
-- Database: banking_db
-- ====================================================================

CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS admins;

-- Table 1: customers
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Table 2: accounts
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    account_number BIGINT NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY')),
    CONSTRAINT chk_positive_balance CHECK (balance >= 0.00)
);

-- Table 3: transactions
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account FOREIGN KEY (account_id) 
        REFERENCES accounts(account_id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_tx_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT')),
    CONSTRAINT chk_tx_amount CHECK (amount > 0.00)
);

-- Table 4: admins
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Seed Data
INSERT INTO admins (username, password) 
VALUES ('admin', 'admin123');

INSERT INTO customers (customer_id, name, email, phone, password)
VALUES (1, 'Rahul Sharma', 'rahul@example.com', '9876543210', 'pass123');

INSERT INTO accounts (account_id, customer_id, account_number, account_type, balance)
VALUES (1, 1, 100001, 'SAVINGS', 5000.00);

INSERT INTO transactions (account_id, transaction_type, amount)
VALUES (1, 'DEPOSIT', 5000.00);
