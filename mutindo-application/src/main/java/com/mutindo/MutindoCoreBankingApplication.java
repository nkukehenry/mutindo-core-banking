package com.mutindo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Main Spring Boot application for Mutindo Core Banking System
 * Full integration with database and business services
 */
@SpringBootApplication(scanBasePackages = {"com.mutindo", "com.mutindo.api"})
@PropertySource("classpath:system.properties")
@RestController
@EntityScan(basePackages = "com.mutindo.entities")
@EnableJpaRepositories(basePackages = "com.mutindo.repositories")
@EnableCaching
@EnableAsync
public class MutindoCoreBankingApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting Mutindo Core Banking System...");
        SpringApplication.run(MutindoCoreBankingApplication.class, args);
        System.out.println("✅ Mutindo Core Banking System started successfully!");
        System.out.println("🌐 Access the application at: http://localhost:8081");
        System.out.println("📊 Health check: http://localhost:8081/health");
        System.out.println("ℹ️  Application info: http://localhost:8081/actuator/info");
        System.out.println("📚 API Documentation: http://localhost:8081/swagger-ui.html");
        System.out.println("🔧 API Docs (JSON): http://localhost:8081/api-docs");
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "application", "Mutindo Core Banking System",
            "company", "Mutindo",
            "timestamp", LocalDateTime.now(),
            "message", "Mutindo Core Banking System is running successfully!",
            "version", "1.0.0-SNAPSHOT"
        );
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
            "application", "Mutindo Core Banking System",
            "company", "Mutindo",
            "version", "1.0.0-SNAPSHOT",
            "description", "Modular Mutindo Core Banking System built with Spring Boot",
            "features", Map.of(
                "modules", "Authentication, Customers, Accounts, GL, Posting Engine",
                "architecture", "Microservices-ready modular design",
                "security", "JWT-based authentication with multi-branch support",
                "database", "MySQL with Redis caching",
                "messaging", "RabbitMQ event-driven architecture"
            ),
            "endpoints", Map.of(
                "health", "/health",
                "info", "/info",
                "actuator", "/actuator/health",
                "swagger-ui", "/swagger-ui.html",
                "api-docs", "/api-docs"
            )
        );
    }

    @GetMapping("/")
    public Map<String, Object> welcome() {
        return Map.of(
            "message", "Welcome to Mutindo Core Banking System",
            "company", "Mutindo",
            "status", "System is operational",
            "timestamp", LocalDateTime.now()
        );
    }

    // ========================================
    // BUSINESS LOGIC ENDPOINTS FOR TESTING
    // ========================================

    @GetMapping("/api/v1/auth/status")
    public Map<String, Object> authStatus() {
        return Map.of(
            "service", "Authentication Service",
            "status", "UP",
            "features", Map.of(
                "jwt", "JWT token generation and validation",
                "multi_branch", "Multi-branch access control",
                "roles", "Role-based permissions"
            ),
            "endpoints", Map.of(
                "login", "POST /api/v1/auth/login",
                "refresh", "POST /api/v1/auth/refresh",
                "logout", "POST /api/v1/auth/logout"
            )
        );
    }

    @GetMapping("/api/v1/customers/status")
    public Map<String, Object> customerStatus() {
        return Map.of(
            "service", "Customer Management Service",
            "status", "UP",
            "features", Map.of(
                "kyc", "KYC validation and document management",
                "individual", "Individual customer management",
                "corporate", "Corporate customer management",
                "groups", "Group banking and SACCO support"
            ),
            "endpoints", Map.of(
                "create", "POST /api/v1/customers",
                "get", "GET /api/v1/customers/{id}",
                "search", "GET /api/v1/customers/search",
                "update_kyc", "PATCH /api/v1/customers/{id}/kyc"
            )
        );
    }

    @GetMapping("/api/v1/accounts/status")
    public Map<String, Object> accountStatus() {
        return Map.of(
            "service", "Account Management Service", 
            "status", "UP",
            "features", Map.of(
                "products", "Savings, Current, Fixed Deposit accounts",
                "multi_currency", "Multiple currency support",
                "balance_inquiry", "Real-time balance checking",
                "freeze_unfreeze", "Account freeze/unfreeze operations"
            ),
            "endpoints", Map.of(
                "create", "POST /api/v1/accounts",
                "get", "GET /api/v1/accounts/{id}",
                "balance", "GET /api/v1/accounts/{id}/balance",
                "search", "GET /api/v1/accounts/search"
            )
        );
    }

    @GetMapping("/api/v1/transactions/status")
    public Map<String, Object> transactionStatus() {
        return Map.of(
            "service", "Transaction Processing Service",
            "status", "UP", 
            "features", Map.of(
                "posting_engine", "Double-entry posting with GL integration",
                "real_time", "Real-time transaction processing",
                "audit_trail", "Complete audit trail and reconciliation",
                "idempotency", "Idempotent transaction handling"
            ),
            "endpoints", Map.of(
                "deposit", "POST /api/v1/transactions/deposit",
                "withdrawal", "POST /api/v1/transactions/withdrawal",
                "transfer", "POST /api/v1/transactions/transfer",
                "history", "GET /api/v1/transactions/history"
            )
        );
    }

    @GetMapping("/api/v1/gl/status")
    public Map<String, Object> glStatus() {
        return Map.of(
            "service", "General Ledger Service",
            "status", "UP",
            "features", Map.of(
                "chart_of_accounts", "Hierarchical chart of accounts",
                "double_entry", "Double-entry bookkeeping",
                "real_time_posting", "Real-time GL posting",
                "financial_reports", "Trial balance and financial reports"
            ),
            "endpoints", Map.of(
                "accounts", "GET /api/v1/gl/accounts",
                "balance", "GET /api/v1/gl/balance",
                "trial_balance", "GET /api/v1/gl/trial-balance",
                "journal_entries", "GET /api/v1/gl/journal-entries"
            )
        );
    }
}