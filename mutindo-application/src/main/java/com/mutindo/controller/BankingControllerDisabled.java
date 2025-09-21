package com.mutindo.controller;

import com.mutindo.entities.User;
import com.mutindo.entities.Customer;
import com.mutindo.entities.Account;
import com.mutindo.repositories.UserRepository;
import com.mutindo.repositories.CustomerRepository;
import com.mutindo.repositories.AccountRepository;
import com.mutindo.repositories.BranchRepository;
import com.mutindo.encryption.service.IEncryptionService;
import com.mutindo.jwt.service.IJwtService;
import com.mutindo.jwt.dto.JwtClaims;
import com.mutindo.jwt.dto.TokenPair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Banking Controller - Core banking operations with database integration
 * Authentication, Customer Management, Account Management
 */
// Temporarily disabled due to repository query issues
// @RestController
// @RequestMapping("/api/v1")
// @CrossOrigin(origins = "*")
public class BankingControllerDisabled {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final IEncryptionService encryptionService;
    private final IJwtService jwtService;

    @Autowired
    public BankingControllerDisabled(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            BranchRepository branchRepository,
            IEncryptionService encryptionService,
            IJwtService jwtService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
    }

    // =============================================================================
    // AUTHENTICATION ENDPOINTS - REAL DATABASE
    // =============================================================================

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        System.out.println("🔐 Authentication attempt - Username: '" + username + "'");
        
        try {
            // Find user in database
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return createErrorResponse("User not found", HttpStatus.UNAUTHORIZED);
            }
            
            User user = userOpt.get();
            
            // Verify password using encryption service
            if (!encryptionService.verifyPassword(password, user.getPasswordHash())) {
                return createErrorResponse("Invalid credentials", HttpStatus.UNAUTHORIZED);
            }
            
            // Check if user is active
            if (!user.getActive()) {
                return createErrorResponse("User account is inactive", HttpStatus.UNAUTHORIZED);
            }
            
            // Generate real JWT token
            JwtClaims claims = JwtClaims.builder()
                    .userId(user.getId().toString())
                    .userType(user.getUserType().toString())
                    .branchId(user.getBranchId() != null ? user.getBranchId().toString() : null)
                    .build();
            
            TokenPair tokenPair = jwtService.generateTokenPair(claims);
            
            // Update last login
            userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", tokenPair.getAccessToken());
            response.put("refreshToken", tokenPair.getRefreshToken());
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "userType", user.getUserType(),
                "branchId", user.getBranchId()
            ));
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("✅ Authentication successful - User ID: " + user.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Authentication error: " + e.getMessage());
            return createErrorResponse("Authentication failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =============================================================================
    // CUSTOMER ENDPOINTS - REAL DATABASE
    // =============================================================================

    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getAllCustomers() {
        try {
            List<Customer> customers = customerRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", customers);
            response.put("total", customers.size());
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("📊 Database query - Found " + customers.size() + " customers");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ CUSTOMER ERROR: " + e.getMessage());
            return createErrorResponse("Failed to retrieve customers: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable Long customerId) {
        try {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            
            if (customerOpt.isEmpty()) {
                return createErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", customerOpt.get());
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("👤 Database query - Customer found: " + customerId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ CUSTOMER ERROR: " + e.getMessage());
            return createErrorResponse("Failed to retrieve customer: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =============================================================================
    // ACCOUNT ENDPOINTS - REAL DATABASE
    // =============================================================================

    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> getAllAccounts() {
        try {
            List<Account> accounts = accountRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", accounts);
            response.put("total", accounts.size());
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("💰 Database query - Found " + accounts.size() + " accounts");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ ACCOUNT ERROR: " + e.getMessage());
            return createErrorResponse("Failed to retrieve accounts: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable Long accountId) {
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            
            if (accountOpt.isEmpty()) {
                return createErrorResponse("Account not found", HttpStatus.NOT_FOUND);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", accountOpt.get());
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("🏦 Database query - Account found: " + accountId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ ACCOUNT ERROR: " + e.getMessage());
            return createErrorResponse("Failed to retrieve account: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable Long accountId) {
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            
            if (accountOpt.isEmpty()) {
                return createErrorResponse("Account not found", HttpStatus.NOT_FOUND);
            }
            
            Account account = accountOpt.get();
            
            Map<String, Object> balanceData = new HashMap<>();
            balanceData.put("accountId", account.getId());
            balanceData.put("accountNumber", account.getAccountNumber());
            balanceData.put("balance", account.getBalance());
            balanceData.put("availableBalance", account.getAvailableBalance());
            balanceData.put("currency", account.getCurrency());
            balanceData.put("status", account.getStatus());
            balanceData.put("lastUpdated", account.getUpdatedAt());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", balanceData);
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("💵 Balance retrieved: " + account.getBalance() + " " + account.getCurrency());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ BALANCE ERROR: " + e.getMessage());
            return createErrorResponse("Failed to retrieve balance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =============================================================================
    // SYSTEM STATUS ENDPOINTS
    // =============================================================================

    @GetMapping("/system/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            long userCount = userRepository.count();
            long customerCount = customerRepository.count();
            long accountCount = accountRepository.count();
            long branchCount = branchRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "OPERATIONAL");
            response.put("application", "Mutindo Core Banking System");
            response.put("version", "1.0.0-SNAPSHOT");
            response.put("database", "MySQL - Connected");
            response.put("dataMode", "Database Operations");
            response.put("statistics", Map.of(
                "totalUsers", userCount,
                "totalCustomers", customerCount,
                "totalAccounts", accountCount,
                "totalBranches", branchCount
            ));
            response.put("features", Map.of(
                "authentication", "JWT with database validation",
                "encryption", "BCrypt password hashing",
                "database", "MySQL operations with numeric IDs",
                "audit", "Audit logging enabled",
                "caching", "Redis caching ready"
            ));
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("📊 System status - Users: " + userCount + ", Customers: " + customerCount + ", Accounts: " + accountCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ STATUS ERROR: " + e.getMessage());
            return createErrorResponse("Failed to get system status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}
