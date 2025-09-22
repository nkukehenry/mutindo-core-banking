package com.mutindo.settings.service;

import com.mutindo.settings.dto.SystemConfigurationDto;
import com.mutindo.settings.dto.CreateSystemConfigurationRequest;
import com.mutindo.settings.dto.UpdateSystemConfigurationRequest;
import com.mutindo.settings.mapper.SystemConfigurationMapper;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.entities.SystemConfiguration;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * System Configuration service implementation with comprehensive configuration management
 * Reuses existing infrastructure: Logging, Caching, Repositories, Validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigurationService implements ISystemConfigurationService {

    // Reusing existing infrastructure components via interfaces
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final SystemConfigurationMapper systemConfigurationMapper;

    /**
     * Create new system configuration with comprehensive validation and security
     */
    @Override
    @Transactional
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance logging
    @CacheEvict(value = {"systemConfigurations", "systemConfigs"}, allEntries = true) // Clear config cache
    public SystemConfigurationDto createConfiguration(CreateSystemConfigurationRequest request) {
        log.info("Creating system configuration: {} - Category: {}", request.getConfigKey(), request.getCategory());

        // Validate request
        validateCreateConfigurationRequest(request);

        // Check for duplicate key
        if (systemConfigurationRepository.existsByConfigKey(request.getConfigKey())) {
            throw new ValidationException("Configuration key already exists: " + request.getConfigKey());
        }

        // Create configuration entity
        SystemConfiguration configuration = systemConfigurationMapper.toEntity(request);
        configuration.setActive(true);

        // Set defaults
        if (configuration.getIsEncrypted() == null) {
            configuration.setIsEncrypted(false);
        }
        if (configuration.getIsSystem() == null) {
            configuration.setIsSystem(false);
        }
        if (configuration.getIsEnvironmentSpecific() == null) {
            configuration.setIsEnvironmentSpecific(false);
        }

        // Save configuration
        SystemConfiguration savedConfiguration = systemConfigurationRepository.save(configuration);

        log.info("System configuration created successfully: {} - ID: {}", savedConfiguration.getConfigKey(), savedConfiguration.getId());
        return systemConfigurationMapper.toDto(savedConfiguration);
    }

    /**
     * Get configuration by key - cached for performance
     */
    @Override
    @Cacheable(value = "systemConfigurations", key = "#configKey")
    @PerformanceLog
    public Optional<SystemConfigurationDto> getConfigurationByKey(String configKey) {
        log.debug("Getting system configuration by key: {}", configKey);

        return systemConfigurationRepository.findByConfigKey(configKey)
                .map(systemConfigurationMapper::toDto);
    }

    /**
     * Get configuration by ID - cached for performance
     */
    @Override
    @Cacheable(value = "systemConfigurations", key = "#configId")
    @PerformanceLog
    public Optional<SystemConfigurationDto> getConfigurationById(Long configId) {
        log.debug("Getting system configuration by ID: {}", configId);

        return systemConfigurationRepository.findById(configId)
                .map(systemConfigurationMapper::toDto);
    }

    /**
     * Update configuration information
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"systemConfigurations", "systemConfigs"}, allEntries = true)
    public SystemConfigurationDto updateConfiguration(Long configId, UpdateSystemConfigurationRequest request) {
        log.info("Updating system configuration: {}", configId);

        // Find existing configuration
        SystemConfiguration existingConfiguration = systemConfigurationRepository.findById(configId)
                .orElseThrow(() -> new BusinessException("System configuration not found", "CONFIG_NOT_FOUND"));

        // Validate update request
        validateUpdateConfigurationRequest(request);

        // Check if configuration is system configuration
        if (existingConfiguration.getIsSystem()) {
            throw new BusinessException("Cannot update system configurations", "SYSTEM_CONFIG_READONLY");
        }

        // Update configuration
        systemConfigurationMapper.updateEntity(existingConfiguration, request);
        SystemConfiguration savedConfiguration = systemConfigurationRepository.save(existingConfiguration);

        log.info("System configuration updated successfully: {} - ID: {}", savedConfiguration.getConfigKey(), savedConfiguration.getId());
        return systemConfigurationMapper.toDto(savedConfiguration);
    }

    /**
     * Deactivate configuration (soft delete)
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"systemConfigurations", "systemConfigs"}, allEntries = true)
    public void deactivateConfiguration(Long configId, String reason) {
        log.info("Deactivating system configuration: {} - Reason: {}", configId, reason);

        // Find configuration
        SystemConfiguration configuration = systemConfigurationRepository.findById(configId)
                .orElseThrow(() -> new BusinessException("System configuration not found", "CONFIG_NOT_FOUND"));

        // Check if configuration is system configuration
        if (configuration.getIsSystem()) {
            throw new BusinessException("Cannot deactivate system configurations", "SYSTEM_CONFIG_READONLY");
        }

        // Deactivate configuration
        configuration.setActive(false);
        systemConfigurationRepository.save(configuration);

        log.info("System configuration deactivated successfully: {} - ID: {}", configuration.getConfigKey(), configuration.getId());
    }

    /**
     * Get configurations by category - cached for performance
     */
    @Override
    @Cacheable(value = "systemConfigurations", key = "'category:' + #category")
    @PerformanceLog
    public List<SystemConfigurationDto> getConfigurationsByCategory(String category) {
        log.debug("Getting system configurations by category: {}", category);

        return systemConfigurationRepository.findActiveByCategory(category)
                .stream()
                .map(systemConfigurationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get configurations by environment - cached for performance
     */
    @Override
    @Cacheable(value = "systemConfigurations", key = "'env:' + #environment")
    @PerformanceLog
    public List<SystemConfigurationDto> getConfigurationsByEnvironment(String environment) {
        log.debug("Getting system configurations by environment: {}", environment);

        return systemConfigurationRepository.findActiveByEnvironment(environment)
                .stream()
                .map(systemConfigurationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active configurations - cached for performance
     */
    @Override
    @Cacheable(value = "systemConfigurations", key = "'active'")
    @PerformanceLog
    public List<SystemConfigurationDto> getActiveConfigurations() {
        log.debug("Getting all active system configurations");

        return systemConfigurationRepository.findByActiveTrue()
                .stream()
                .map(systemConfigurationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get system configurations - cached for performance
     */
    @Override
    @Cacheable(value = "systemConfigs")
    @PerformanceLog
    public List<SystemConfigurationDto> getSystemConfigurations() {
        log.debug("Getting system configurations");

        return systemConfigurationRepository.findActiveSystemConfigurations()
                .stream()
                .map(systemConfigurationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all configurations with pagination
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<SystemConfigurationDto> getAllConfigurations(Boolean active, Pageable pageable) {
        log.debug("Getting all system configurations - Active: {}", active);

        Page<SystemConfiguration> configPage;
        if (active != null) {
            if (active) {
                configPage = systemConfigurationRepository.findByActiveTrue(pageable);
            } else {
                configPage = systemConfigurationRepository.findByActiveFalse(pageable);
            }
        } else {
            configPage = systemConfigurationRepository.findAll(pageable);
        }

        List<SystemConfigurationDto> configDtos = configPage.getContent()
                .stream()
                .map(systemConfigurationMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                configDtos,
                configPage.getNumber(),
                configPage.getSize(),
                configPage.getTotalElements()
        );
    }

    /**
     * Check if configuration exists by key
     */
    @Override
    @PerformanceLog
    public boolean configurationExistsByKey(String configKey) {
        return systemConfigurationRepository.existsByConfigKey(configKey);
    }

    /**
     * Check if configuration exists by ID
     */
    @Override
    @PerformanceLog
    public boolean configurationExistsById(Long configId) {
        return systemConfigurationRepository.existsById(configId);
    }

    // Private helper methods

    /**
     * Validate configuration creation request
     */
    private void validateCreateConfigurationRequest(CreateSystemConfigurationRequest request) {
        if (request == null) {
            throw new ValidationException("Configuration request cannot be null");
        }

        if (request.getConfigKey() == null || request.getConfigKey().trim().isEmpty()) {
            throw new ValidationException("Configuration key is required");
        }

        if (request.getConfigName() == null || request.getConfigName().trim().isEmpty()) {
            throw new ValidationException("Configuration name is required");
        }

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new ValidationException("Configuration category is required");
        }

        if (request.getDataType() == null || request.getDataType().trim().isEmpty()) {
            throw new ValidationException("Configuration data type is required");
        }

        // Validate key format
        if (!request.getConfigKey().matches("^[A-Z0-9_]+$")) {
            throw new ValidationException("Configuration key must contain only uppercase letters, numbers, and underscores");
        }
    }

    /**
     * Validate configuration update request
     */
    private void validateUpdateConfigurationRequest(UpdateSystemConfigurationRequest request) {
        if (request == null) {
            throw new ValidationException("Update request cannot be null");
        }
    }
}
