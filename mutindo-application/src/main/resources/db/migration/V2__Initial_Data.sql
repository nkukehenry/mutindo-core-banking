-- Mutindo Core Banking System - Initial Seed Data
-- Version: 2.0.0
-- Description: Insert initial data for branches, users, products, GL accounts, and test customers

-- =============================================================================
-- BRANCHES DATA
-- =============================================================================
INSERT INTO branches (branch_code, branch_name, address, phone, email, is_head_office, active, created_by) VALUES
('HO001', 'Mutindo Head Office', 'Plot 123, Kampala Road, Kampala', '+256700123456', 'headoffice@mutindo.com', TRUE, TRUE, 'SYSTEM'),
('KLA001', 'Kampala Main Branch', 'Plot 456, Parliament Avenue, Kampala', '+256700234567', 'kampala@mutindo.com', FALSE, TRUE, 'SYSTEM'),
('ENT001', 'Entebbe Branch', 'Plot 789, Entebbe Road, Entebbe', '+256700345678', 'entebbe@mutindo.com', FALSE, TRUE, 'SYSTEM'),
('JIN001', 'Jinja Branch', 'Plot 101, Main Street, Jinja', '+256700456789', 'jinja@mutindo.com', FALSE, TRUE, 'SYSTEM');

-- =============================================================================
-- USERS DATA (Default Admin Users)
-- =============================================================================
-- Password for all users is "Admin!2025" - hashed with BCrypt using at.favre.lib.crypto.bcrypt.BCrypt
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, user_type, branch_id, active, created_by) VALUES
('admin', 'admin@mutindo.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcx.zcx4RgTZ.F5jKjK4e', 'System', 'Administrator', '+256700111111', 'SUPER_ADMIN', NULL, TRUE, 'SYSTEM'),
('manager1', 'manager1@mutindo.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcx.zcx4RgTZ.F5jKjK4e', 'John', 'Mutindo', '+256700222222', 'BRANCH_MANAGER', 2, TRUE, 'SYSTEM'),
('teller1', 'teller1@mutindo.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcx.zcx4RgTZ.F5jKjK4e', 'Sarah', 'Nakamura', '+256700333333', 'TELLER', 2, TRUE, 'SYSTEM'),
('teller2', 'teller2@mutindo.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcx.zcx4RgTZ.F5jKjK4e', 'Peter', 'Ssemakula', '+256700444444', 'TELLER', 3, TRUE, 'SYSTEM');

-- =============================================================================
-- PRODUCTS DATA
-- =============================================================================
INSERT INTO products (product_code, product_name, product_type, description, minimum_balance, maximum_balance, interest_rate, active, created_by) VALUES
('SAV001', 'Mutindo Savings Account', 'SAVINGS', 'Basic savings account with competitive interest rates', 10000.00, 50000000.00, 0.0500, TRUE, 'SYSTEM'),
('CUR001', 'Mutindo Current Account', 'CURRENT', 'Current account for business and personal transactions', 50000.00, 100000000.00, 0.0100, TRUE, 'SYSTEM'),
('FD001', 'Mutindo Fixed Deposit', 'FIXED_DEPOSIT', 'Fixed deposit account with guaranteed returns', 100000.00, 10000000.00, 0.0800, TRUE, 'SYSTEM'),
('LOAN001', 'Mutindo Personal Loan', 'LOAN', 'Personal loans for various needs', 50000.00, 5000000.00, 0.1500, TRUE, 'SYSTEM');

-- =============================================================================
-- GL ACCOUNTS (CHART OF ACCOUNTS)
-- =============================================================================

-- ASSETS (Level 1)
INSERT INTO gl_accounts (account_code, account_name, account_type, parent_id, level, is_control_account, is_posting_account, currency, active, created_by) VALUES
('100000', 'ASSETS', 'ASSET', NULL, 1, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM');

-- Current Assets (Level 2)
INSERT INTO gl_accounts (account_code, account_name, account_type, parent_id, level, is_control_account, is_posting_account, currency, active, created_by) VALUES
('110000', 'CURRENT ASSETS', 'ASSET', 1, 2, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('111000', 'Cash and Cash Equivalents', 'ASSET', 2, 3, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('111001', 'Cash in Hand', 'ASSET', 3, 4, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM'),
('111002', 'Cash at Bank', 'ASSET', 3, 4, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM'),
('112000', 'Customer Accounts', 'ASSET', 2, 3, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('112001', 'Savings Accounts', 'ASSET', 6, 4, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM'),
('112002', 'Current Accounts', 'ASSET', 6, 4, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM');

-- LIABILITIES (Level 1)
INSERT INTO gl_accounts (account_code, account_name, account_type, parent_id, level, is_control_account, is_posting_account, currency, active, created_by) VALUES
('200000', 'LIABILITIES', 'LIABILITY', NULL, 1, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM');

-- Current Liabilities (Level 2)
INSERT INTO gl_accounts (account_code, account_name, account_type, parent_id, level, is_control_account, is_posting_account, currency, active, created_by) VALUES
('210000', 'CURRENT LIABILITIES', 'LIABILITY', 8, 2, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('211000', 'Customer Deposits', 'LIABILITY', 9, 3, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('211001', 'Savings Deposits', 'LIABILITY', 10, 4, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM'),
('211002', 'Current Deposits', 'LIABILITY', 10, 4, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM');

-- EQUITY (Level 1)
INSERT INTO gl_accounts (account_code, account_name, account_type, parent_id, level, is_control_account, is_posting_account, currency, active, created_by) VALUES
('300000', 'EQUITY', 'EQUITY', NULL, 1, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('310000', 'Share Capital', 'EQUITY', 13, 2, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM'),
('320000', 'Retained Earnings', 'EQUITY', 13, 2, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM');

-- INCOME (Level 1)
INSERT INTO gl_accounts (account_code, account_name, account_type, parent_id, level, is_control_account, is_posting_account, currency, active, created_by) VALUES
('400000', 'INCOME', 'INCOME', NULL, 1, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('410000', 'Interest Income', 'INCOME', 16, 2, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM'),
('420000', 'Fee Income', 'INCOME', 16, 2, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM');

-- EXPENSES (Level 1)
INSERT INTO gl_accounts (account_code, account_name, account_type, parent_id, level, is_control_account, is_posting_account, currency, active, created_by) VALUES
('500000', 'EXPENSES', 'EXPENSE', NULL, 1, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('510000', 'Operating Expenses', 'EXPENSE', 19, 2, TRUE, FALSE, 'UGX', TRUE, 'SYSTEM'),
('511000', 'Staff Costs', 'EXPENSE', 20, 3, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM'),
('512000', 'Administrative Expenses', 'EXPENSE', 20, 3, FALSE, TRUE, 'UGX', TRUE, 'SYSTEM');

-- =============================================================================
-- TEST CUSTOMERS DATA
-- =============================================================================
INSERT INTO customers (customer_number, customer_type, first_name, last_name, national_id, date_of_birth, gender, phone, email, address, occupation, monthly_income, primary_branch_id, kyc_status, risk_score, active, created_by) VALUES
('CUST0000001', 'INDIVIDUAL', 'John', 'Mutindo', 'CF123456789012', '1990-05-15', 'MALE', '+256700123456', 'john.mutindo@email.com', 'Plot 123, Kampala', 'Software Engineer', 2500000.00, 2, 'VERIFIED', 25.00, TRUE, 'teller1'),
('CUST0000002', 'INDIVIDUAL', 'Jane', 'Nakamura', 'CF123456789013', '1985-08-22', 'FEMALE', '+256700234567', 'jane.nakamura@email.com', 'Plot 456, Entebbe', 'Business Owner', 3500000.00, 3, 'VERIFIED', 30.00, TRUE, 'teller2'),
('CUST0000003', 'CORPORATE', NULL, NULL, NULL, NULL, NULL, '+256700345678', 'info@mutindoltd.com', 'Plot 789, Industrial Area, Kampala', NULL, NULL, 2, 'VERIFIED', 40.00, TRUE, 'teller1');

-- Update corporate customer with company name
UPDATE customers SET company_name = 'Mutindo Technologies Ltd' WHERE customer_number = 'CUST0000003';

-- =============================================================================
-- TEST ACCOUNTS DATA
-- =============================================================================
INSERT INTO accounts (account_number, customer_id, product_id, branch_id, currency, balance, available_balance, status, opened_date, created_by) VALUES
('1001000000001', 1, 1, 2, 'UGX', 150000.00, 150000.00, 'ACTIVE', '2024-01-15', 'teller1'),
('1001000000002', 1, 2, 2, 'UGX', 500000.00, 500000.00, 'ACTIVE', '2024-02-01', 'teller1'),
('1001000000003', 2, 1, 3, 'UGX', 250000.00, 250000.00, 'ACTIVE', '2024-01-20', 'teller2'),
('1001000000004', 3, 2, 2, 'UGX', 1000000.00, 1000000.00, 'ACTIVE', '2024-02-10', 'teller1');

-- =============================================================================
-- SYSTEM PARAMETERS
-- =============================================================================
INSERT INTO system_parameters (parameter_key, parameter_value, description, data_type, created_by) VALUES
('SYSTEM_NAME', 'Mutindo Core Banking System', 'System display name', 'STRING', 'SYSTEM'),
('SYSTEM_VERSION', '1.0.0', 'Current system version', 'STRING', 'SYSTEM'),
('DEFAULT_CURRENCY', 'UGX', 'Default system currency', 'STRING', 'SYSTEM'),
('MIN_SAVINGS_BALANCE', '10000.00', 'Minimum balance for savings accounts', 'NUMBER', 'SYSTEM'),
('MAX_DAILY_WITHDRAWAL', '2000000.00', 'Maximum daily withdrawal limit', 'NUMBER', 'SYSTEM'),
('INTEREST_CALCULATION_METHOD', 'DAILY_BALANCE', 'Method for calculating interest', 'STRING', 'SYSTEM'),
('BACKUP_RETENTION_DAYS', '90', 'Number of days to retain database backups', 'NUMBER', 'SYSTEM'),
('SESSION_TIMEOUT_MINUTES', '30', 'User session timeout in minutes', 'NUMBER', 'SYSTEM');

-- =============================================================================
-- INITIAL JOURNAL ENTRIES FOR ACCOUNT OPENING
-- =============================================================================
-- Journal entry for John Mutindo's initial deposit
INSERT INTO journal_entries (posting_date, source_type, source_id, narration, idempotency_key, branch_id, created_by) VALUES
('2024-01-15', 'ACCOUNT_OPENING', 1, 'Initial deposit for account opening - John Mutindo', 'ACC_OPEN_001', 2, 'teller1');

INSERT INTO journal_entry_lines (journal_entry_id, gl_account_id, debit_amount, credit_amount, currency, description, created_by) VALUES
(1, 4, 150000.00, 0.00, 'UGX', 'Cash received for account opening', 'teller1'),
(1, 7, 0.00, 150000.00, 'UGX', 'Savings deposit liability', 'teller1');

-- =============================================================================
-- UPDATE GL ACCOUNT BALANCES
-- =============================================================================
UPDATE gl_accounts SET balance = 150000.00 WHERE account_code = '111001'; -- Cash in Hand
UPDATE gl_accounts SET balance = 150000.00 WHERE account_code = '211001'; -- Savings Deposits

-- =============================================================================
-- VERIFICATION QUERIES (FOR TESTING)
-- =============================================================================
-- These are comment queries to verify data integrity after migration

-- Check branch count: SELECT COUNT(*) FROM branches WHERE active = TRUE;
-- Check user count: SELECT COUNT(*) FROM users WHERE active = TRUE;
-- Check customer count: SELECT COUNT(*) FROM customers WHERE active = TRUE;
-- Check account count: SELECT COUNT(*) FROM accounts WHERE status = 'ACTIVE';
-- Check GL accounts count: SELECT COUNT(*) FROM gl_accounts WHERE active = TRUE;
-- Check journal entries balance: SELECT SUM(debit_amount) - SUM(credit_amount) FROM journal_entry_lines;

-- =============================================================================
-- END OF SEED DATA
-- =============================================================================