package com.mutindo.settings.service;

import com.mutindo.settings.dto.SystemConfigurationDto;
import com.mutindo.settings.dto.CreateSystemConfigurationRequest;
import com.mutindo.settings.dto.UpdateSystemConfigurationRequest;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * System Configuration service interface for polymorphic configuration operations
 * Follows our established pattern of interface-driven design
 */
public interface ISystemConfigurationService {
    
    /**
     * Create new system configuration
     * @param request Configuration creation details
     * @return Created configuration information
     */
    SystemConfigurationDto createConfiguration(CreateSystemConfigurationRequest request);
    
    /**
     * Get configuration by key
     * @param configKey Configuration key
     * @return Configuration information if found
     */
    Optional<SystemConfigurationDto> getConfigurationByKey(String configKey);
    
    /**
     * Get configuration by ID
     * @param configId Configuration ID
     * @return Configuration information if found
     */
    Optional<SystemConfigurationDto> getConfigurationById(Long configId);
    
    /**
     * Update configuration information
     * @param configId Configuration ID
     * @param request Update request
     * @return Updated configuration information
     */
    SystemConfigurationDto updateConfiguration(Long configId, UpdateSystemConfigurationRequest request);
    
    /**
     * Deactivate configuration (soft delete)
     * @param configId Configuration ID
     * @param reason Deactivation reason
     */
    void deactivateConfiguration(Long configId, String reason);
    
    /**
     * Get configurations by category
     * @param category Configuration category (EMAIL, SMS, SECURITY, etc.)
     * @return List of configurations
     */
    List<SystemConfigurationDto> getConfigurationsByCategory(String category);
    
    /**
     * Get configurations by environment
     * @param environment Environment (DEV, TEST, PROD, etc.)
     * @return List of configurations
     */
    List<SystemConfigurationDto> getConfigurationsByEnvironment(String environment);
    
    /**
     * Get all active configurations
     * @return List of active configurations
     */
    List<SystemConfigurationDto> getActiveConfigurations();
    
    /**
     * Get system configurations (isSystem = true)
     * @return List of system configurations
     */
    List<SystemConfigurationDto> getSystemConfigurations();
    
    /**
     * Get all configurations with pagination
     * @param active Filter by active status (null for all)
     * @param pageable Pagination parameters
     * @return Paginated list of configurations
     */
    PaginatedResponse<SystemConfigurationDto> getAllConfigurations(Boolean active, Pageable pageable);
    
    /**
     * Check if configuration exists by key
     * @param configKey Configuration key
     * @return true if configuration exists
     */
    boolean configurationExistsByKey(String configKey);
    
    /**
     * Check if configuration exists by ID
     * @param configId Configuration ID
     * @return true if configuration exists
     */
    boolean configurationExistsById(Long configId);
}
