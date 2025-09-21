package com.mutindo.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Centralized system properties configuration
 * Loads system.properties file and makes it available to all services
 */
@Configuration
@PropertySource("classpath:system.properties")
public class SystemPropertiesConfiguration {
    // This class enables @PropertySource loading for all CBS services
}
