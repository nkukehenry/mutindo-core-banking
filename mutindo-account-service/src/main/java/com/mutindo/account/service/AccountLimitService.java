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
 * Account Limit Service - Handles all limit configuration for accounts
 * Single responsibility: Account limit setup and management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLimitService {

    private final ProductRepository productRepository;

    /**
     * Setup comprehensive limit configuration for an account
     */
    public void setupAccountLimits(Account account) {
        log.debug("Setting up limits for account: {}", account.getAccountNumber());
        
        try {
            // Get product configuration for limit settings
            Product product = productRepository.findById(account.getProductId()).orElse(null);
            if (product == null) {
                log.warn("Product not found for account: {}", account.getAccountNumber());
                return;
            }
            
            // Setup transaction limits
            setupTransactionLimits(account, product);
            
            // Setup balance limits
            setupBalanceLimits(account, product);
            
            // Setup time-based limits
            setupTimeBasedLimits(account, product);
            
            // Setup channel-specific limits
            setupChannelLimits(account, product);
            
            log.info("Limit setup completed for account: {}", account.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to setup limits for account: {}", account.getAccountNumber(), e);
            // Don't throw exception as this is async operation
        }
    }

    /**
     * Setup transaction limits based on product configuration
     */
    private void setupTransactionLimits(Account account, Product product) {
        Map<String, Object> transactionLimits = new java.util.HashMap<>();
        
        // Daily limits
        if (product.getDailyWithdrawalLimit() != null) {
            transactionLimits.put("dailyWithdrawalLimit", product.getDailyWithdrawalLimit());
        }
        
        // Monthly limits
        if (product.getMonthlyWithdrawalLimit() != null) {
            transactionLimits.put("monthlyWithdrawalLimit", product.getMonthlyWithdrawalLimit());
        }
        
        // Single transaction limits
        if (product.getMaxTransactionAmount() != null) {
            transactionLimits.put("maxSingleTransaction", product.getMaxTransactionAmount());
        }
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("transactionLimits", transactionLimits);
        account.setCustomData(customData);
        
        log.debug("Transaction limits configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup balance limits including overdraft configuration
     */
    private void setupBalanceLimits(Account account, Product product) {
        Map<String, Object> balanceLimits = new java.util.HashMap<>();
        
        // Minimum balance
        if (product.getMinBalance() != null) {
            balanceLimits.put("minimumBalance", product.getMinBalance());
        }
        
        // Maximum balance
        if (product.getMaxBalance() != null) {
            balanceLimits.put("maximumBalance", product.getMaxBalance());
        }
        
        // Overdraft limits
        if (product.getAllowsOverdraft()) {
            balanceLimits.put("overdraftLimit", account.getOverdraftLimit());
            balanceLimits.put("overdraftEnabled", true);
        } else {
            balanceLimits.put("overdraftEnabled", false);
        }
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("balanceLimits", balanceLimits);
        account.setCustomData(customData);
        
        log.debug("Balance limits configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup time-based transaction limits
     */
    private void setupTimeBasedLimits(Account account, Product product) {
        Map<String, Object> timeLimits = new java.util.HashMap<>();
        
        // Business hours restrictions
        timeLimits.put("businessHoursOnly", false);
        timeLimits.put("businessHoursStart", "08:00");
        timeLimits.put("businessHoursEnd", "17:00");
        
        // Weekend restrictions
        timeLimits.put("weekendTransactionsAllowed", true);
        timeLimits.put("weekendLimitMultiplier", new BigDecimal("0.5"));
        
        // Holiday restrictions
        timeLimits.put("holidayTransactionsAllowed", true);
        timeLimits.put("holidayLimitMultiplier", new BigDecimal("0.3"));
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("timeLimits", timeLimits);
        account.setCustomData(customData);
        
        log.debug("Time-based limits configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup channel-specific limits (ATM, Mobile, Branch)
     */
    private void setupChannelLimits(Account account, Product product) {
        Map<String, Object> channelLimits = new java.util.HashMap<>();
        
        // ATM limits
        Map<String, Object> atmLimits = new java.util.HashMap<>();
        atmLimits.put("dailyLimit", account.getDailyWithdrawalLimit() != null ? 
            account.getDailyWithdrawalLimit().multiply(new BigDecimal("0.3")) : new BigDecimal("100000"));
        atmLimits.put("singleTransactionLimit", new BigDecimal("50000"));
        channelLimits.put("atm", atmLimits);
        
        // Mobile/Online limits
        Map<String, Object> mobileLimits = new java.util.HashMap<>();
        mobileLimits.put("dailyLimit", account.getDailyWithdrawalLimit() != null ? 
            account.getDailyWithdrawalLimit().multiply(new BigDecimal("0.7")) : new BigDecimal("200000"));
        mobileLimits.put("singleTransactionLimit", new BigDecimal("100000"));
        channelLimits.put("mobile", mobileLimits);
        
        // Branch limits
        Map<String, Object> branchLimits = new java.util.HashMap<>();
        branchLimits.put("dailyLimit", account.getDailyWithdrawalLimit());
        branchLimits.put("singleTransactionLimit", account.getDailyWithdrawalLimit());
        channelLimits.put("branch", branchLimits);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("channelLimits", channelLimits);
        account.setCustomData(customData);
        
        log.debug("Channel limits configured for account: {}", account.getAccountNumber());
    }

    /**
     * Update transaction limits for an existing account
     */
    public void updateTransactionLimits(Account account, Map<String, Object> newLimits) {
        log.debug("Updating transaction limits for account: {}", account.getAccountNumber());
        
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("transactionLimits", newLimits);
        account.setCustomData(customData);
        
        log.info("Transaction limits updated for account: {}", account.getAccountNumber());
    }

    /**
     * Get current transaction limits for an account
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTransactionLimits(Account account) {
        Map<String, Object> customData = account.getCustomData();
        if (customData != null && customData.containsKey("transactionLimits")) {
            return (Map<String, Object>) customData.get("transactionLimits");
        }
        return new java.util.HashMap<>();
    }

    /**
     * Validate if a transaction amount is within limits
     */
    public boolean isTransactionWithinLimits(Account account, BigDecimal amount, String channel) {
        Map<String, Object> channelLimits = getChannelLimits(account, channel);
        
        if (channelLimits.containsKey("singleTransactionLimit")) {
            BigDecimal singleLimit = (BigDecimal) channelLimits.get("singleTransactionLimit");
            if (amount.compareTo(singleLimit) > 0) {
                return false;
            }
        }
        
        // Additional validation logic can be added here
        return true;
    }

    /**
     * Get channel-specific limits for an account
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getChannelLimits(Account account, String channel) {
        Map<String, Object> customData = account.getCustomData();
        if (customData != null && customData.containsKey("channelLimits")) {
            Map<String, Object> channelLimits = (Map<String, Object>) customData.get("channelLimits");
            if (channelLimits.containsKey(channel)) {
                return (Map<String, Object>) channelLimits.get(channel);
            }
        }
        return new java.util.HashMap<>();
    }
}
