package com.mutindo.reporting.service;

import com.mutindo.reporting.dto.CreateReportDefinitionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to initialize default report definitions
 * Runs on application startup to ensure essential reports are available
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportInitializationService implements CommandLineRunner {

    private final IReportingService reportingService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default report definitions...");
        
        try {
            initializeDefaultReports();
            log.info("Default report definitions initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize default report definitions", e);
        }
    }

    private void initializeDefaultReports() {
        log.info("Initializing default reports...");

        // Financial Reports
        createReportIfNotExists("TRIAL_BALANCE", "Trial Balance", 
            "Trial balance report showing all GL account balances", 
            "FINANCIAL", "TABULAR",
            "SELECT gl.code, gl.name, gl.type, COALESCE(SUM(jel.debit_amount), 0) as debit_balance, COALESCE(SUM(jel.credit_amount), 0) as credit_balance FROM gl_accounts gl LEFT JOIN journal_entry_lines jel ON gl.id = jel.gl_account_id WHERE gl.active = true GROUP BY gl.id, gl.code, gl.name, gl.type ORDER BY gl.code",
            List.of("PDF", "EXCEL", "CSV"), "ROLE_ADMIN");

        createReportIfNotExists("PROFIT_LOSS", "Profit & Loss Statement", 
            "Profit and loss statement for the specified period", 
            "FINANCIAL", "TABULAR",
            "SELECT gl.code, gl.name, COALESCE(SUM(jel.debit_amount - jel.credit_amount), 0) as amount FROM gl_accounts gl LEFT JOIN journal_entry_lines jel ON gl.id = jel.gl_account_id WHERE gl.active = true AND gl.type IN ('REVENUE', 'EXPENSE') AND jel.created_at >= :startDate AND jel.created_at <= :endDate GROUP BY gl.id, gl.code, gl.name ORDER BY gl.code",
            List.of("PDF", "EXCEL"), "ROLE_ADMIN");

        createReportIfNotExists("BALANCE_SHEET", "Balance Sheet", 
            "Balance sheet showing assets, liabilities, and equity", 
            "FINANCIAL", "TABULAR",
            "SELECT gl.code, gl.name, gl.type, COALESCE(SUM(jel.debit_amount - jel.credit_amount), 0) as balance FROM gl_accounts gl LEFT JOIN journal_entry_lines jel ON gl.id = jel.gl_account_id WHERE gl.active = true AND gl.type IN ('ASSET', 'LIABILITY', 'EQUITY') GROUP BY gl.id, gl.code, gl.name, gl.type ORDER BY gl.type, gl.code",
            List.of("PDF", "EXCEL"), "ROLE_ADMIN");

        // Customer Reports
        createReportIfNotExists("CUSTOMER_LIST", "Customer List", 
            "Complete list of all customers with their details", 
            "CUSTOMER", "TABULAR",
            "SELECT c.id, c.customer_number, c.first_name, c.last_name, c.email, c.phone, c.customer_type, c.status, c.created_at FROM customers c WHERE c.active = true ORDER BY c.created_at DESC",
            List.of("PDF", "EXCEL", "CSV"), "ROLE_BRANCHES_READ");

        createReportIfNotExists("CUSTOMER_ACCOUNTS", "Customer Accounts", 
            "List of all customer accounts with balances", 
            "CUSTOMER", "TABULAR",
            "SELECT a.account_number, c.customer_number, c.first_name, c.last_name, a.account_type, a.balance, a.status, a.created_at FROM accounts a JOIN customers c ON a.customer_id = c.id WHERE a.active = true ORDER BY a.created_at DESC",
            List.of("PDF", "EXCEL"), "ROLE_BRANCHES_READ");

        // Transaction Reports
        createReportIfNotExists("TRANSACTION_HISTORY", "Transaction History", 
            "Transaction history for specified date range", 
            "TRANSACTION", "TABULAR",
            "SELECT at.transaction_id, at.account_number, at.transaction_type, at.amount, at.description, at.status, at.created_at FROM account_transactions at WHERE at.created_at >= :startDate AND at.created_at <= :endDate ORDER BY at.created_at DESC",
            List.of("PDF", "EXCEL", "CSV"), "ROLE_BRANCHES_READ");

        createReportIfNotExists("DAILY_TRANSACTIONS", "Daily Transactions", 
            "Daily transaction summary", 
            "TRANSACTION", "SUMMARY",
            "SELECT DATE(at.created_at) as transaction_date, at.transaction_type, COUNT(*) as transaction_count, SUM(at.amount) as total_amount FROM account_transactions at WHERE at.created_at >= :startDate AND at.created_at <= :endDate GROUP BY DATE(at.created_at), at.transaction_type ORDER BY transaction_date DESC, at.transaction_type",
            List.of("PDF", "EXCEL"), "ROLE_BRANCHES_READ");

        // Account Reports
        createReportIfNotExists("ACCOUNT_BALANCES", "Account Balances", 
            "Current balances for all accounts", 
            "ACCOUNT", "TABULAR",
            "SELECT a.account_number, c.customer_number, c.first_name, c.last_name, a.account_type, a.balance, a.status FROM accounts a JOIN customers c ON a.customer_id = c.id WHERE a.active = true ORDER BY a.balance DESC",
            List.of("PDF", "EXCEL"), "ROLE_BRANCHES_READ");

        createReportIfNotExists("ACCOUNT_SUMMARY", "Account Summary", 
            "Summary of accounts by type and status", 
            "ACCOUNT", "SUMMARY",
            "SELECT a.account_type, a.status, COUNT(*) as account_count, SUM(a.balance) as total_balance FROM accounts a WHERE a.active = true GROUP BY a.account_type, a.status ORDER BY a.account_type, a.status",
            List.of("PDF", "EXCEL"), "ROLE_BRANCHES_READ");

        // Branch Reports
        createReportIfNotExists("BRANCH_PERFORMANCE", "Branch Performance", 
            "Performance metrics by branch", 
            "BRANCH", "TABULAR",
            "SELECT b.code, b.name, COUNT(DISTINCT c.id) as customer_count, COUNT(DISTINCT a.id) as account_count, SUM(a.balance) as total_balance FROM branches b LEFT JOIN customers c ON b.id = c.branch_id LEFT JOIN accounts a ON c.id = a.customer_id WHERE b.active = true GROUP BY b.id, b.code, b.name ORDER BY total_balance DESC",
            List.of("PDF", "EXCEL"), "ROLE_ADMIN");

        // Audit Reports
        createReportIfNotExists("AUDIT_LOG", "Audit Log", 
            "System audit log for compliance", 
            "AUDIT", "TABULAR",
            "SELECT al.id, al.user_id, al.action, al.entity_type, al.entity_id, al.timestamp, al.ip_address, al.user_agent FROM audit_logs al WHERE al.timestamp >= :startDate AND al.timestamp <= :endDate ORDER BY al.timestamp DESC",
            List.of("PDF", "EXCEL", "CSV"), "ROLE_ADMIN");

        // Public Reports (visible to all users)
        createPublicReportIfNotExists("CUSTOMER_LIST", "Customer List", 
            "Complete list of all customers with their details", 
            "CUSTOMER", "TABULAR",
            "SELECT c.id, c.customer_number, c.first_name, c.last_name, c.email, c.phone, c.customer_type, c.status, c.created_at FROM customers c WHERE c.active = true ORDER BY c.created_at DESC",
            List.of("PDF", "EXCEL", "CSV"), "ROLE_BRANCHES_READ");
    }

    private void createReportIfNotExists(String code, String name, String description, String category, String type, String sqlQuery, List<String> outputFormats, String accessRole) {
        if (!reportingService.reportDefinitionExistsByCode(code)) {
            try {
                CreateReportDefinitionRequest request = CreateReportDefinitionRequest.builder()
                        .reportCode(code)
                        .reportName(name)
                        .description(description)
                        .category(category)
                        .reportType(type)
                        .sqlQuery(sqlQuery)
                        .outputFormats(outputFormats)
                        .accessRole(accessRole)
                        .isSystem(true) // Default reports are system reports
                        .isPublic(false)
                        .cacheDuration(60) // Cache for 1 hour
                        .maxRows(10000)
                        .build();

                reportingService.createReportDefinition(request);
                log.debug("Created report definition: {}", code);
            } catch (Exception e) {
                log.warn("Failed to create report definition {}: {}", code, e.getMessage());
            }
        }
    }

    private void createPublicReportIfNotExists(String code, String name, String description, String category, String type, String sqlQuery, List<String> outputFormats, String accessRole) {
        if (!reportingService.reportDefinitionExistsByCode(code + "_PUBLIC")) {
            try {
                CreateReportDefinitionRequest request = CreateReportDefinitionRequest.builder()
                        .reportCode(code + "_PUBLIC")
                        .reportName(name + " (Public)")
                        .description(description)
                        .category(category)
                        .reportType(type)
                        .sqlQuery(sqlQuery)
                        .outputFormats(outputFormats)
                        .accessRole(accessRole)
                        .isSystem(true) // Default reports are system reports
                        .isPublic(true) // Public report
                        .cacheDuration(30) // Cache for 30 minutes
                        .maxRows(5000) // Limit for public reports
                        .build();

                reportingService.createReportDefinition(request);
                log.debug("Created public report definition: {}", code + "_PUBLIC");
            } catch (Exception e) {
                log.warn("Failed to create public report definition {}: {}", code + "_PUBLIC", e.getMessage());
            }
        }
    }
}
