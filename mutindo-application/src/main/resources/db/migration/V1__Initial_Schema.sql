-- Mutindo Core Banking System - Initial Database Schema
-- Version: 1.0.0
-- Description: Create all core banking tables with proper indexes and constraints

-- =============================================================================
-- BRANCHES TABLE
-- =============================================================================
CREATE TABLE branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_code VARCHAR(10) NOT NULL UNIQUE,
    branch_name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100),
    manager_id BIGINT,
    is_head_office BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_branch_code (branch_code),
    INDEX idx_branch_active (active),
    INDEX idx_branch_manager (manager_id)
);

-- =============================================================================
-- USERS TABLE
-- =============================================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    user_type ENUM('SUPER_ADMIN', 'INSTITUTION_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'LOAN_OFFICER', 'CUSTOMER_SERVICE', 'AUDITOR', 'VIEWER') NOT NULL DEFAULT 'TELLER',
    branch_id BIGINT,
    supervisor_id BIGINT,
    department VARCHAR(64),
    position VARCHAR(64),
    employee_id VARCHAR(20),
    last_login TIMESTAMP NULL,
    failed_login_attempts INT DEFAULT 0,
    account_locked BOOLEAN DEFAULT FALSE,
    locked_until TIMESTAMP NULL,
    password_expires_at TIMESTAMP NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_branch (branch_id),
    INDEX idx_user_supervisor (supervisor_id),
    INDEX idx_user_type (user_type),
    INDEX idx_user_active (active),
    INDEX idx_user_login (username, active),
    
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE SET NULL,
    FOREIGN KEY (supervisor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =============================================================================
-- CUSTOMERS TABLE
-- =============================================================================
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_number VARCHAR(20) NOT NULL UNIQUE,
    customer_type ENUM('INDIVIDUAL', 'CORPORATE', 'GOVERNMENT', 'NGO') NOT NULL DEFAULT 'INDIVIDUAL',
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    company_name VARCHAR(100),
    national_id VARCHAR(20),
    passport_number VARCHAR(20),
    date_of_birth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    occupation VARCHAR(100),
    employer VARCHAR(100),
    monthly_income DECIMAL(15,2),
    primary_branch_id BIGINT NOT NULL,
    kyc_status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
    risk_score DECIMAL(5,2) DEFAULT 50.00,
    active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_customer_number (customer_number),
    INDEX idx_customer_national_id (national_id),
    INDEX idx_customer_phone (phone),
    INDEX idx_customer_branch (primary_branch_id),
    INDEX idx_customer_type (customer_type),
    INDEX idx_customer_kyc (kyc_status),
    INDEX idx_customer_active (active),
    
    FOREIGN KEY (primary_branch_id) REFERENCES branches(id) ON DELETE RESTRICT
);

-- =============================================================================
-- PRODUCTS TABLE
-- =============================================================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(20) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL,
    product_type ENUM('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT', 'LOAN') NOT NULL,
    description TEXT,
    minimum_balance DECIMAL(15,2) DEFAULT 0.00,
    maximum_balance DECIMAL(15,2),
    interest_rate DECIMAL(8,4) DEFAULT 0.0000,
    fees JSON,
    active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_product_code (product_code),
    INDEX idx_product_type (product_type),
    INDEX idx_product_active (active)
);

-- =============================================================================
-- ACCOUNTS TABLE
-- =============================================================================
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    currency VARCHAR(3) DEFAULT 'UGX',
    balance DECIMAL(15,2) DEFAULT 0.00,
    available_balance DECIMAL(15,2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE', 'DORMANT', 'CLOSED', 'FROZEN') DEFAULT 'ACTIVE',
    opened_date DATE NOT NULL,
    closed_date DATE NULL,
    last_transaction_date TIMESTAMP NULL,
    metadata JSON,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_account_number (account_number),
    INDEX idx_account_customer (customer_id),
    INDEX idx_account_branch (branch_id),
    INDEX idx_account_product (product_id),
    INDEX idx_account_status (status),
    INDEX idx_account_currency (currency),
    
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE RESTRICT
);

-- =============================================================================
-- GL ACCOUNTS (CHART OF ACCOUNTS)
-- =============================================================================
CREATE TABLE gl_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_code VARCHAR(20) NOT NULL UNIQUE,
    account_name VARCHAR(100) NOT NULL,
    account_type ENUM('ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE') NOT NULL,
    parent_id BIGINT,
    level INT NOT NULL DEFAULT 1,
    is_control_account BOOLEAN DEFAULT FALSE,
    is_posting_account BOOLEAN DEFAULT TRUE,
    currency VARCHAR(3) DEFAULT 'UGX',
    balance DECIMAL(15,2) DEFAULT 0.00,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_gl_account_code (account_code),
    INDEX idx_gl_account_type (account_type),
    INDEX idx_gl_account_parent (parent_id),
    INDEX idx_gl_account_level (level),
    INDEX idx_gl_account_active (active),
    
    FOREIGN KEY (parent_id) REFERENCES gl_accounts(id) ON DELETE RESTRICT
);

-- =============================================================================
-- JOURNAL ENTRIES
-- =============================================================================
CREATE TABLE journal_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    posting_date DATE NOT NULL,
    source_type VARCHAR(64),
    source_id BIGINT,
    narration TEXT,
    idempotency_key VARCHAR(64) UNIQUE,
    branch_id BIGINT,
    reversed BOOLEAN DEFAULT FALSE,
    reversal_entry_id BIGINT,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_je_posting_date (posting_date),
    INDEX idx_je_source (source_type, source_id),
    INDEX idx_je_idempotency (idempotency_key),
    INDEX idx_je_branch (branch_id),
    
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE SET NULL,
    FOREIGN KEY (reversal_entry_id) REFERENCES journal_entries(id) ON DELETE SET NULL
);

-- =============================================================================
-- JOURNAL ENTRY LINES
-- =============================================================================
CREATE TABLE journal_entry_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    journal_entry_id BIGINT NOT NULL,
    gl_account_id BIGINT NOT NULL,
    debit_amount DECIMAL(15,2) DEFAULT 0.00,
    credit_amount DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'UGX',
    description TEXT,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_jel_journal_entry (journal_entry_id),
    INDEX idx_jel_gl_account (gl_account_id),
    INDEX idx_jel_amounts (debit_amount, credit_amount),
    
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (gl_account_id) REFERENCES gl_accounts(id) ON DELETE RESTRICT,
    
    CONSTRAINT chk_jel_amounts CHECK (
        (debit_amount > 0 AND credit_amount = 0) OR 
        (credit_amount > 0 AND debit_amount = 0)
    )
);

-- =============================================================================
-- TRANSACTIONS TABLE
-- =============================================================================
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_number VARCHAR(30) NOT NULL UNIQUE,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'FEE', 'INTEREST', 'REVERSAL') NOT NULL,
    account_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'UGX',
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    description TEXT,
    reference_number VARCHAR(50),
    channel ENUM('TELLER', 'ATM', 'MOBILE', 'INTERNET', 'POS') DEFAULT 'TELLER',
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REVERSED') DEFAULT 'PENDING',
    processed_by BIGINT,
    processed_at TIMESTAMP NULL,
    journal_entry_id BIGINT,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_txn_number (transaction_number),
    INDEX idx_txn_account (account_id),
    INDEX idx_txn_type (transaction_type),
    INDEX idx_txn_date (created_at),
    INDEX idx_txn_status (status),
    INDEX idx_txn_channel (channel),
    INDEX idx_txn_processed_by (processed_by),
    
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
    FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE SET NULL
);

-- =============================================================================
-- AUDIT LOG TABLE
-- =============================================================================
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT') NOT NULL,
    old_values JSON,
    new_values JSON,
    user_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_date (created_at),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =============================================================================
-- SYSTEM PARAMETERS TABLE
-- =============================================================================
CREATE TABLE system_parameters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parameter_key VARCHAR(100) NOT NULL UNIQUE,
    parameter_value TEXT NOT NULL,
    description TEXT,
    data_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    is_encrypted BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_param_key (parameter_key)
);