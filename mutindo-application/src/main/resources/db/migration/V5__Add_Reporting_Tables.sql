-- Add Reporting Tables
-- Version: 5.0.0
-- Description: Create report_definitions and report_executions tables for reporting functionality

-- =============================================================================
-- REPORT DEFINITIONS TABLE
-- =============================================================================
CREATE TABLE report_definitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_code VARCHAR(64) NOT NULL UNIQUE,
    report_name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(64) NOT NULL,
    report_type VARCHAR(32) NOT NULL,
    sql_query TEXT,
    parameters JSON,
    output_formats JSON,
    template_path VARCHAR(500),
    is_system BOOLEAN DEFAULT FALSE,
    is_scheduled BOOLEAN DEFAULT FALSE,
    schedule_cron VARCHAR(100),
    email_recipients JSON,
    active BOOLEAN DEFAULT TRUE,
    access_role VARCHAR(64),
    cache_duration INT DEFAULT 0,
    max_rows INT DEFAULT 10000,
    is_public BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_report_def_code (report_code),
    INDEX idx_report_def_category (category),
    INDEX idx_report_def_active (active)
);

-- =============================================================================
-- REPORT EXECUTIONS TABLE
-- =============================================================================
CREATE TABLE report_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_definition_id BIGINT NOT NULL,
    execution_type ENUM('MANUAL', 'SCHEDULED', 'API') NOT NULL DEFAULT 'MANUAL',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    execution_time_ms BIGINT,
    rows_returned INT,
    output_format VARCHAR(32),
    output_file_path VARCHAR(500),
    error_message TEXT,
    parameters JSON,
    requested_by BIGINT,
    created_by VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    INDEX idx_report_exec_report_def (report_definition_id),
    INDEX idx_report_exec_status (status),
    INDEX idx_report_exec_type (execution_type),
    INDEX idx_report_exec_started (started_at),
    INDEX idx_report_exec_requested_by (requested_by),
    
    FOREIGN KEY (report_definition_id) REFERENCES report_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE SET NULL
);

-- =============================================================================
-- INSERT DEFAULT REPORT DEFINITIONS
-- =============================================================================
INSERT INTO report_definitions (
    report_code, report_name, description, category, report_type, 
    sql_query, output_formats, is_system, active, access_role, 
    max_rows, is_public, created_by
) VALUES 
(
    'CUSTOMER_SUMMARY', 
    'Customer Summary Report', 
    'Summary of all customers with their account balances and status',
    'CUSTOMER', 
    'TABULAR',
    'SELECT c.id, c.customer_number, c.first_name, c.last_name, c.email, c.phone, c.status, COUNT(a.id) as account_count, COALESCE(SUM(a.balance), 0) as total_balance FROM customers c LEFT JOIN accounts a ON c.id = a.customer_id GROUP BY c.id, c.customer_number, c.first_name, c.last_name, c.email, c.phone, c.status',
    '["PDF", "EXCEL", "CSV"]',
    TRUE,
    TRUE,
    'ROLE_REPORTS_VIEW',
    1000,
    FALSE,
    'SYSTEM'
),
(
    'ACCOUNT_BALANCES', 
    'Account Balances Report', 
    'Current balances for all active accounts',
    'FINANCIAL', 
    'TABULAR',
    'SELECT a.account_number, a.account_name, c.first_name, c.last_name, a.balance, a.currency, a.status, a.created_at FROM accounts a JOIN customers c ON a.customer_id = c.id WHERE a.status = ''ACTIVE'' ORDER BY a.balance DESC',
    '["PDF", "EXCEL", "CSV"]',
    TRUE,
    TRUE,
    'ROLE_REPORTS_VIEW',
    5000,
    FALSE,
    'SYSTEM'
),
(
    'TRANSACTION_SUMMARY', 
    'Transaction Summary Report', 
    'Summary of transactions by type and date range',
    'FINANCIAL', 
    'TABULAR',
    'SELECT transaction_type, COUNT(*) as transaction_count, SUM(amount) as total_amount, currency, DATE(created_at) as transaction_date FROM transactions WHERE created_at >= CURDATE() - INTERVAL 30 DAY GROUP BY transaction_type, currency, DATE(created_at) ORDER BY transaction_date DESC, transaction_type',
    '["PDF", "EXCEL", "CSV"]',
    TRUE,
    TRUE,
    'ROLE_REPORTS_VIEW',
    10000,
    FALSE,
    'SYSTEM'
),
(
    'BRANCH_PERFORMANCE', 
    'Branch Performance Report', 
    'Performance metrics by branch including transaction volumes and customer counts',
    'OPERATIONAL', 
    'TABULAR',
    'SELECT b.branch_code, b.branch_name, COUNT(DISTINCT c.id) as customer_count, COUNT(DISTINCT a.id) as account_count, COUNT(t.id) as transaction_count, COALESCE(SUM(t.amount), 0) as total_transaction_amount FROM branches b LEFT JOIN customers c ON b.id = c.branch_id LEFT JOIN accounts a ON c.id = a.customer_id LEFT JOIN transactions t ON a.id = t.account_id WHERE t.created_at >= CURDATE() - INTERVAL 30 DAY GROUP BY b.id, b.branch_code, b.branch_name',
    '["PDF", "EXCEL", "CSV"]',
    TRUE,
    TRUE,
    'ROLE_REPORTS_VIEW',
    100,
    FALSE,
    'SYSTEM'
);
