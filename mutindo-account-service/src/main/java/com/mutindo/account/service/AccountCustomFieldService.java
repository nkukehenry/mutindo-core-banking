package com.mutindo.account.service;

import com.mutindo.entities.Account;
import com.mutindo.entities.Product;
import com.mutindo.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Account Custom Field Service - Handles all custom field configuration for accounts
 * Single responsibility: Account custom field setup and management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountCustomFieldService {

    private final ProductRepository productRepository;

    /**
     * Setup comprehensive custom field configuration for an account
     */
    public void setupAccountCustomFields(Account account) {
        log.debug("Setting up custom fields for account: {}", account.getAccountNumber());
        
        try {
            // Get product configuration for custom fields
            Product product = productRepository.findById(account.getProductId()).orElse(null);
            if (product == null) {
                log.warn("Product not found for account: {}", account.getAccountNumber());
                return;
            }
            
            // Setup product-specific custom fields
            setupProductCustomFields(account, product);
            
            // Setup account-specific custom fields
            setupAccountSpecificFields(account);
            
            // Setup regulatory custom fields
            setupRegulatoryFields(account, product);
            
            // Setup business-specific custom fields
            setupBusinessFields(account, product);
            
            log.info("Custom fields setup completed for account: {}", account.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Failed to setup custom fields for account: {}", account.getAccountNumber(), e);
            // Don't throw exception as this is async operation
        }
    }

    /**
     * Setup product-specific custom fields based on product type
     */
    private void setupProductCustomFields(Account account, Product product) {
        Map<String, Object> productFields = new java.util.HashMap<>();
        
        // Product-specific fields based on type
        switch (product.getProductType().toUpperCase()) {
            case "SAVINGS":
                productFields.put("interestCalculationMethod", "DAILY_BALANCE");
                productFields.put("interestPaymentFrequency", "MONTHLY");
                productFields.put("minimumDepositAmount", product.getMinBalance());
                break;
            case "CURRENT":
                productFields.put("checkBookEnabled", true);
                productFields.put("debitCardEnabled", true);
                productFields.put("onlineBankingEnabled", true);
                break;
            case "LOAN":
                productFields.put("repaymentMethod", product.getRepaymentFrequency());
                productFields.put("interestCalculationMethod", product.getInterestCalculationMethod());
                productFields.put("guarantorRequired", product.getRequiresGuarantor());
                break;
        }
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("productFields", productFields);
        account.setCustomData(customData);
        
        log.debug("Product custom fields configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup account-specific custom fields for tracking and management
     */
    private void setupAccountSpecificFields(Account account) {
        Map<String, Object> accountFields = new java.util.HashMap<>();
        
        // Account opening information
        accountFields.put("openingChannel", "BRANCH");
        accountFields.put("openingOfficer", account.getCreatedBy());
        accountFields.put("openingDate", account.getOpenedAt());
        
        // Account status tracking
        accountFields.put("lastActivityDate", account.getOpenedAt());
        accountFields.put("transactionCount", 0);
        accountFields.put("averageBalance", account.getBalance());
        
        // Risk assessment fields
        accountFields.put("riskLevel", "LOW");
        accountFields.put("kycStatus", "COMPLETED");
        accountFields.put("amlStatus", "CLEAR");
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("accountFields", accountFields);
        account.setCustomData(customData);
        
        log.debug("Account-specific fields configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup regulatory compliance fields for AML/KYC requirements
     */
    private void setupRegulatoryFields(Account account, Product product) {
        Map<String, Object> regulatoryFields = new java.util.HashMap<>();
        
        // AML/KYC fields
        regulatoryFields.put("amlRiskCategory", "STANDARD");
        regulatoryFields.put("kycLevel", "ENHANCED");
        regulatoryFields.put("sourceOfFunds", "SALARY");
        
        // Regulatory reporting
        regulatoryFields.put("reportingRequired", true);
        regulatoryFields.put("reportingFrequency", "MONTHLY");
        regulatoryFields.put("regulatoryCategory", product.getProductType());
        
        // Compliance flags
        regulatoryFields.put("sanctionsScreening", true);
        regulatoryFields.put("pepScreening", true);
        regulatoryFields.put("transactionMonitoring", true);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("regulatoryFields", regulatoryFields);
        account.setCustomData(customData);
        
        log.debug("Regulatory fields configured for account: {}", account.getAccountNumber());
    }

    /**
     * Setup business-specific custom fields for customer management
     */
    private void setupBusinessFields(Account account, Product product) {
        Map<String, Object> businessFields = new java.util.HashMap<>();
        
        // Business metrics
        businessFields.put("customerSegment", "RETAIL");
        businessFields.put("accountTier", "STANDARD");
        businessFields.put("relationshipManager", null);
        
        // Service preferences
        businessFields.put("preferredLanguage", "EN");
        businessFields.put("preferredContactMethod", "EMAIL");
        businessFields.put("marketingOptIn", false);
        
        // Business rules
        businessFields.put("autoRenewal", false);
        businessFields.put("feeWaiver", false);
        businessFields.put("priorityService", false);
        
        // Store in custom data
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        customData.put("businessFields", businessFields);
        account.setCustomData(customData);
        
        log.debug("Business fields configured for account: {}", account.getAccountNumber());
    }

    /**
     * Update custom field value for an account
     */
    public void updateCustomField(Account account, String fieldCategory, String fieldName, Object value) {
        log.debug("Updating custom field {}:{} for account: {}", fieldCategory, fieldName, account.getAccountNumber());
        
        Map<String, Object> customData = account.getCustomData() != null ? 
            new java.util.HashMap<>(account.getCustomData()) : new java.util.HashMap<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> categoryFields = (Map<String, Object>) customData.computeIfAbsent(fieldCategory, k -> new java.util.HashMap<>());
        categoryFields.put(fieldName, value);
        
        account.setCustomData(customData);
        
        log.info("Custom field {}:{} updated for account: {}", fieldCategory, fieldName, account.getAccountNumber());
    }

    /**
     * Get custom field value for an account
     */
    @SuppressWarnings("unchecked")
    public Object getCustomField(Account account, String fieldCategory, String fieldName) {
        Map<String, Object> customData = account.getCustomData();
        if (customData != null && customData.containsKey(fieldCategory)) {
            Map<String, Object> categoryFields = (Map<String, Object>) customData.get(fieldCategory);
            return categoryFields.get(fieldName);
        }
        return null;
    }

    /**
     * Get all custom fields for a specific category
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCustomFieldsByCategory(Account account, String fieldCategory) {
        Map<String, Object> customData = account.getCustomData();
        if (customData != null && customData.containsKey(fieldCategory)) {
            return (Map<String, Object>) customData.get(fieldCategory);
        }
        return new java.util.HashMap<>();
    }

    /**
     * Remove custom field from an account
     */
    public void removeCustomField(Account account, String fieldCategory, String fieldName) {
        log.debug("Removing custom field {}:{} for account: {}", fieldCategory, fieldName, account.getAccountNumber());
        
        Map<String, Object> customData = account.getCustomData();
        if (customData != null && customData.containsKey(fieldCategory)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> categoryFields = (Map<String, Object>) customData.get(fieldCategory);
            categoryFields.remove(fieldName);
            
            // Remove category if empty
            if (categoryFields.isEmpty()) {
                customData.remove(fieldCategory);
            }
            
            account.setCustomData(customData);
            log.info("Custom field {}:{} removed for account: {}", fieldCategory, fieldName, account.getAccountNumber());
        }
    }

    /**
     * Validate custom field value based on field type and constraints
     */
    public boolean validateCustomFieldValue(String fieldName, Object value, String fieldType) {
        if (value == null) {
            return true; // Allow null values
        }
        
        switch (fieldType.toUpperCase()) {
            case "STRING":
                return value instanceof String;
            case "NUMBER":
                return value instanceof Number;
            case "BOOLEAN":
                return value instanceof Boolean;
            case "DATE":
                return value instanceof java.time.LocalDate || value instanceof java.time.LocalDateTime;
            default:
                return true; // Allow any type for unknown field types
        }
    }
}
