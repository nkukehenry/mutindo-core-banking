package com.mutindo.settings.service;

import com.mutindo.settings.dto.CreateBusinessSettingsRequest;
import com.mutindo.settings.dto.CreateSystemConfigurationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * Service to initialize default business settings and system configurations
 * Runs on application startup to ensure essential settings are available
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsInitializationService implements CommandLineRunner {

    private final IBusinessSettingsService businessSettingsService;
    private final ISystemConfigurationService systemConfigurationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default business settings and system configurations...");
        
        try {
            initializeBusinessSettings();
            initializeSystemConfigurations();
            log.info("Default settings initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize default settings", e);
        }
    }

    private void initializeBusinessSettings() {
        log.info("Initializing business settings...");

        // Business Information Settings
        createBusinessSettingIfNotExists("BUSINESS_NAME", "BUSINESS_INFO", "Business Name", "Mutindo Core Banking System", "STRING", "BUSINESS_INFO");
        createBusinessSettingIfNotExists("BUSINESS_LOGO", "BUSINESS_INFO", "Business Logo", "/assets/logo.png", "FILE", "BUSINESS_INFO");
        createBusinessSettingIfNotExists("BUSINESS_ADDRESS", "BUSINESS_INFO", "Business Address", "123 Banking Street, Nairobi, Kenya", "STRING", "BUSINESS_INFO");
        createBusinessSettingIfNotExists("BUSINESS_PHONE", "BUSINESS_INFO", "Business Phone", "+254 700 000 000", "STRING", "BUSINESS_INFO");
        createBusinessSettingIfNotExists("BUSINESS_EMAIL", "BUSINESS_INFO", "Business Email", "info@mutindo.com", "STRING", "BUSINESS_INFO");
        createBusinessSettingIfNotExists("BUSINESS_WEBSITE", "BUSINESS_INFO", "Business Website", "https://www.mutindo.com", "STRING", "BUSINESS_INFO");
        createBusinessSettingIfNotExists("BUSINESS_REGISTRATION_NUMBER", "BUSINESS_INFO", "Registration Number", "C.123456", "STRING", "BUSINESS_INFO");
        createBusinessSettingIfNotExists("BUSINESS_TAX_ID", "BUSINESS_INFO", "Tax ID", "P123456789X", "STRING", "BUSINESS_INFO");

        // Location Settings
        createBusinessSettingIfNotExists("DEFAULT_CURRENCY", "LOCATION", "Default Currency", "KES", "STRING", "LOCATION");
        createBusinessSettingIfNotExists("DEFAULT_TIMEZONE", "LOCATION", "Default Timezone", "Africa/Nairobi", "STRING", "LOCATION");
        createBusinessSettingIfNotExists("DEFAULT_COUNTRY", "LOCATION", "Default Country", "Kenya", "STRING", "LOCATION");
        createBusinessSettingIfNotExists("DEFAULT_LANGUAGE", "LOCATION", "Default Language", "en", "STRING", "LOCATION");

        // Branding Settings
        createBusinessSettingIfNotExists("PRIMARY_COLOR", "BRANDING", "Primary Color", "#1E40AF", "STRING", "BRANDING");
        createBusinessSettingIfNotExists("SECONDARY_COLOR", "BRANDING", "Secondary Color", "#3B82F6", "STRING", "BRANDING");
        createBusinessSettingIfNotExists("FAVICON", "BRANDING", "Favicon", "/assets/favicon.ico", "FILE", "BRANDING");
        createBusinessSettingIfNotExists("LOGIN_BACKGROUND", "BRANDING", "Login Background", "/assets/login-bg.jpg", "FILE", "BRANDING");

        // System Email Settings
        createBusinessSettingIfNotExists("SYSTEM_EMAIL_FROM", "SYSTEM_EMAIL", "System Email From", "noreply@mutindo.com", "STRING", "SYSTEM_EMAIL");
        createBusinessSettingIfNotExists("SYSTEM_EMAIL_NAME", "SYSTEM_EMAIL", "System Email Name", "Mutindo Banking System", "STRING", "SYSTEM_EMAIL");
        createBusinessSettingIfNotExists("SUPPORT_EMAIL", "SYSTEM_EMAIL", "Support Email", "support@mutindo.com", "STRING", "SYSTEM_EMAIL");
        createBusinessSettingIfNotExists("ADMIN_EMAIL", "SYSTEM_EMAIL", "Admin Email", "admin@mutindo.com", "STRING", "SYSTEM_EMAIL");

        // Public Settings (visible to all users)
        createBusinessSettingIfNotExists("BUSINESS_NAME", "BUSINESS_INFO", "Business Name", "Mutindo Core Banking System", "STRING", "BUSINESS_INFO", true);
        createBusinessSettingIfNotExists("BUSINESS_LOGO", "BUSINESS_INFO", "Business Logo", "/assets/logo.png", "FILE", "BRANDING", true);
        createBusinessSettingIfNotExists("SUPPORT_EMAIL", "SYSTEM_EMAIL", "Support Email", "support@mutindo.com", "STRING", "SYSTEM_EMAIL", true);
    }

    private void initializeSystemConfigurations() {
        log.info("Initializing system configurations...");

        // Email Configuration
        createSystemConfigIfNotExists("EMAIL_SMTP_HOST", "SMTP Host", "smtp.gmail.com", "Email SMTP server host", "STRING", "EMAIL");
        createSystemConfigIfNotExists("EMAIL_SMTP_PORT", "SMTP Port", "587", "Email SMTP server port", "NUMBER", "EMAIL");
        createSystemConfigIfNotExists("EMAIL_SMTP_USERNAME", "SMTP Username", "", "Email SMTP username", "STRING", "EMAIL", true);
        createSystemConfigIfNotExists("EMAIL_SMTP_PASSWORD", "SMTP Password", "", "Email SMTP password", "STRING", "EMAIL", true);
        createSystemConfigIfNotExists("EMAIL_SMTP_SSL", "SMTP SSL", "true", "Enable SSL for SMTP", "BOOLEAN", "EMAIL");

        // SMS Configuration
        createSystemConfigIfNotExists("SMS_PROVIDER", "SMS Provider", "AFRICASTALKING", "SMS service provider", "STRING", "SMS");
        createSystemConfigIfNotExists("SMS_API_KEY", "SMS API Key", "", "SMS provider API key", "STRING", "SMS", true);
        createSystemConfigIfNotExists("SMS_SENDER_ID", "SMS Sender ID", "MUTINDO", "SMS sender identifier", "STRING", "SMS");

        // Security Configuration
        createSystemConfigIfNotExists("JWT_SECRET", "JWT Secret", "", "JWT signing secret key", "STRING", "SECURITY", true);
        createSystemConfigIfNotExists("JWT_EXPIRATION", "JWT Expiration", "3600", "JWT token expiration in seconds", "NUMBER", "SECURITY");
        createSystemConfigIfNotExists("PASSWORD_MIN_LENGTH", "Password Min Length", "8", "Minimum password length", "NUMBER", "SECURITY");
        createSystemConfigIfNotExists("PASSWORD_REQUIRE_UPPERCASE", "Password Require Uppercase", "true", "Require uppercase in password", "BOOLEAN", "SECURITY");
        createSystemConfigIfNotExists("PASSWORD_REQUIRE_NUMBERS", "Password Require Numbers", "true", "Require numbers in password", "BOOLEAN", "SECURITY");
        createSystemConfigIfNotExists("PASSWORD_REQUIRE_SPECIAL", "Password Require Special", "true", "Require special characters in password", "BOOLEAN", "SECURITY");

        // Integration Configuration
        createSystemConfigIfNotExists("MPESA_CONSUMER_KEY", "M-Pesa Consumer Key", "", "M-Pesa API consumer key", "STRING", "INTEGRATION", true);
        createSystemConfigIfNotExists("MPESA_CONSUMER_SECRET", "M-Pesa Consumer Secret", "", "M-Pesa API consumer secret", "STRING", "INTEGRATION", true);
        createSystemConfigIfNotExists("MPESA_SHORTCODE", "M-Pesa Shortcode", "", "M-Pesa business shortcode", "STRING", "INTEGRATION", true);

        // System Configuration
        createSystemConfigIfNotExists("SYSTEM_MAINTENANCE_MODE", "Maintenance Mode", "false", "Enable system maintenance mode", "BOOLEAN", "SYSTEM");
        createSystemConfigIfNotExists("SYSTEM_DEBUG_MODE", "Debug Mode", "false", "Enable system debug mode", "BOOLEAN", "SYSTEM");
        createSystemConfigIfNotExists("SYSTEM_LOG_LEVEL", "Log Level", "INFO", "System logging level", "STRING", "SYSTEM");
        createSystemConfigIfNotExists("SYSTEM_BACKUP_ENABLED", "Backup Enabled", "true", "Enable automatic backups", "BOOLEAN", "SYSTEM");
        createSystemConfigIfNotExists("SYSTEM_BACKUP_FREQUENCY", "Backup Frequency", "DAILY", "Backup frequency (DAILY, WEEKLY, MONTHLY)", "STRING", "SYSTEM");
    }

    private void createBusinessSettingIfNotExists(String key, String type, String name, String value, String dataType, String category) {
        createBusinessSettingIfNotExists(key, type, name, value, dataType, category, false);
    }

    private void createBusinessSettingIfNotExists(String key, String type, String name, String value, String dataType, String category, boolean isPublic) {
        if (!businessSettingsService.settingExistsByKey(key)) {
            try {
                CreateBusinessSettingsRequest request = CreateBusinessSettingsRequest.builder()
                        .settingKey(key)
                        .settingType(type)
                        .settingName(name)
                        .settingValue(value)
                        .dataType(dataType)
                        .category(category)
                        .isPublic(isPublic)
                        .isSystem(true) // Default settings are system settings
                        .build();

                businessSettingsService.createSetting(request);
                log.debug("Created business setting: {}", key);
            } catch (Exception e) {
                log.warn("Failed to create business setting {}: {}", key, e.getMessage());
            }
        }
    }

    private void createSystemConfigIfNotExists(String key, String name, String value, String description, String dataType, String category) {
        createSystemConfigIfNotExists(key, name, value, description, dataType, category, false);
    }

    private void createSystemConfigIfNotExists(String key, String name, String value, String description, String dataType, String category, boolean isEncrypted) {
        if (!systemConfigurationService.configurationExistsByKey(key)) {
            try {
                CreateSystemConfigurationRequest request = CreateSystemConfigurationRequest.builder()
                        .configKey(key)
                        .configName(name)
                        .configValue(value)
                        .description(description)
                        .dataType(dataType)
                        .category(category)
                        .isEncrypted(isEncrypted)
                        .isSystem(true) // Default configurations are system configurations
                        .build();

                systemConfigurationService.createConfiguration(request);
                log.debug("Created system configuration: {}", key);
            } catch (Exception e) {
                log.warn("Failed to create system configuration {}: {}", key, e.getMessage());
            }
        }
    }
}
