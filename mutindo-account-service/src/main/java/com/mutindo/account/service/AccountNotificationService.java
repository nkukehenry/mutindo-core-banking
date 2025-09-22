package com.mutindo.account.service;

import com.mutindo.entities.Account;
import com.mutindo.entities.Product;
import com.mutindo.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Account Notification Service - Handles all notification configuration for accounts
 * Single responsibility: Account notification setup and management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountNotificationService {

    private final ProductRepository productRepository;

    /**
     * Setup comprehensive notification configuration for an account
     */
    public void setupAccountNotifications(Account account) {
        log.debug("Setting up notifications for account: {}", account.getAccountNumber());
        
        try {
            // Get product configuration for notification settings
            Product product = productRepository.findById(account.getProductId()).orElse(null);
            if (product == null) {
                log.warn("Product not found for account: {}", account.getAccountNumber());
                return;
            }
            
            // Setup default notification preferences based on product type
            setupDefaultNotificationPreferences(account, product);
            
            // Setup transaction notifications
            setupTransactionNotifications(account, product);
            
            // Setup balance alerts
            setupBalanceAlerts(account, product);
            
            // Setup statement notifications
            setupStatementNotifications(account, product);
            
            log.info("Notification setup completed for account: {}", account.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to setup notifications for account: {}", account.getAccountNumber(), e);
            // Don't throw exception as this is async operation
        }
    }

    /**
     * Setup default notification preferences based on product type
     */
    private void setupDefaultNotificationPreferences(Account account, Product product) {
        Map<String, Object> notificationPrefs = new java.util.HashMap<>();
        
        switch (product.getProductType().toUpperCase()) {
            case "SAVINGS":
                notificationPrefs.put("lowBalanceAlert", true);
                notificationPrefs.put("transactionAlert", true);
                notificationPrefs.put("statementAlert", true);
                notificationPrefs.put("interestCreditAlert", true);
                break;
            case "CURRENT":
                notificationPrefs.put("lowBalanceAlert", true);
                notificationPrefs.put("transactionAlert", false); // Too frequent for current accounts
                notificationPrefs.put("statementAlert", true);
                notificationPrefs.put("overdraftAlert", true);
                break;
            case "LOAN":
                notificationPrefs.put("paymentDueAlert", true);
                notificationPrefs.put("paymentOverdueAlert", true);
                notificationPrefs.put("paymentReceivedAlert", true);
                notificationPrefs.put("loanDisbursementAlert", true);
                break;
            default:
                notificationPrefs.put("transactionAlert", true);
                notificationPrefs.put("statementAlert", true);
        }
        
        // Store in account custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("notificationPreferences", notificationPrefs);
        account.setCustomData(customData);
        
        log.debug("Default notification preferences set for account: {}", account.getAccountNumber());
    }

    /**
     * Setup transaction notification thresholds and channels
     */
    private void setupTransactionNotifications(Account account, Product product) {
        Map<String, Object> transactionAlerts = new java.util.HashMap<>();
        
        // Set thresholds based on product type and limits
        BigDecimal dailyLimit = account.getDailyWithdrawalLimit();
        if (dailyLimit != null) {
            transactionAlerts.put("largeTransactionThreshold", dailyLimit.multiply(new BigDecimal("0.8")));
            transactionAlerts.put("dailyLimitWarningThreshold", dailyLimit.multiply(new BigDecimal("0.9")));
        }
        
        // Set notification channels
        transactionAlerts.put("smsEnabled", true);
        transactionAlerts.put("emailEnabled", true);
        transactionAlerts.put("pushEnabled", false); // Can be enabled later
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("transactionAlerts", transactionAlerts);
        account.setCustomData(customData);
        
        log.debug("Transaction notifications configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup balance alert thresholds and frequencies
     */
    private void setupBalanceAlerts(Account account, Product product) {
        Map<String, Object> balanceAlerts = new java.util.HashMap<>();
        
        BigDecimal minBalance = account.getMinimumBalance();
        if (minBalance != null) {
            balanceAlerts.put("lowBalanceThreshold", minBalance.multiply(new BigDecimal("1.1")));
            balanceAlerts.put("criticalBalanceThreshold", minBalance);
        }
        
        // Set alert frequencies
        balanceAlerts.put("lowBalanceFrequency", "IMMEDIATE");
        balanceAlerts.put("criticalBalanceFrequency", "IMMEDIATE");
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("balanceAlerts", balanceAlerts);
        account.setCustomData(customData);
        
        log.debug("Balance alerts configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup statement notification preferences
     */
    private void setupStatementNotifications(Account account, Product product) {
        Map<String, Object> statementAlerts = new java.util.HashMap<>();
        
        // Set statement frequency based on product type
        switch (product.getProductType().toUpperCase()) {
            case "SAVINGS":
                statementAlerts.put("frequency", "MONTHLY");
                break;
            case "CURRENT":
                statementAlerts.put("frequency", "MONTHLY");
                break;
            case "LOAN":
                statementAlerts.put("frequency", "MONTHLY");
                break;
            default:
                statementAlerts.put("frequency", "QUARTERLY");
        }
        
        statementAlerts.put("emailEnabled", true);
        statementAlerts.put("smsEnabled", false);
        statementAlerts.put("paperEnabled", false);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("statementAlerts", statementAlerts);
        account.setCustomData(customData);
        
        log.debug("Statement notifications configured for account: {}", account.getAccountNumber());
    }

    /**
     * Update notification preferences for an existing account
     */
    public void updateNotificationPreferences(Account account, Map<String, Object> newPreferences) {
        log.debug("Updating notification preferences for account: {}", account.getAccountNumber());
        
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("notificationPreferences", newPreferences);
        account.setCustomData(customData);
        
        log.info("Notification preferences updated for account: {}", account.getAccountNumber());
    }

    /**
     * Get current notification preferences for an account
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getNotificationPreferences(Account account) {
        Map<String, Object> customData = account.getCustomData();
        if (customData != null && customData.containsKey("notificationPreferences")) {
            return (Map<String, Object>) customData.get("notificationPreferences");
        }
        return new java.util.HashMap<>();
    }
}
