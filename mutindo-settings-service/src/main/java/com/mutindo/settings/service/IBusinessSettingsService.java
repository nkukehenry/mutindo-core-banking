package com.mutindo.settings.service;

import com.mutindo.settings.dto.BusinessSettingsDto;
import com.mutindo.settings.dto.CreateBusinessSettingsRequest;
import com.mutindo.settings.dto.UpdateBusinessSettingsRequest;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Business Settings service interface for polymorphic settings operations
 * Follows our established pattern of interface-driven design
 */
public interface IBusinessSettingsService {
    
    /**
     * Create new business setting
     * @param request Setting creation details
     * @return Created setting information
     */
    BusinessSettingsDto createSetting(CreateBusinessSettingsRequest request);
    
    /**
     * Get setting by key
     * @param settingKey Setting key
     * @return Setting information if found
     */
    Optional<BusinessSettingsDto> getSettingByKey(String settingKey);
    
    /**
     * Get setting by ID
     * @param settingId Setting ID
     * @return Setting information if found
     */
    Optional<BusinessSettingsDto> getSettingById(Long settingId);
    
    /**
     * Update setting information
     * @param settingId Setting ID
     * @param request Update request
     * @return Updated setting information
     */
    BusinessSettingsDto updateSetting(Long settingId, UpdateBusinessSettingsRequest request);
    
    /**
     * Deactivate setting (soft delete)
     * @param settingId Setting ID
     * @param reason Deactivation reason
     */
    void deactivateSetting(Long settingId, String reason);
    
    /**
     * Get settings by type
     * @param settingType Setting type (BUSINESS_INFO, BRANDING, etc.)
     * @return List of settings
     */
    List<BusinessSettingsDto> getSettingsByType(String settingType);
    
    /**
     * Get settings by category
     * @param category Setting category
     * @return List of settings
     */
    List<BusinessSettingsDto> getSettingsByCategory(String category);
    
    /**
     * Get all active settings
     * @return List of active settings
     */
    List<BusinessSettingsDto> getActiveSettings();
    
    /**
     * Get public settings (visible to all users)
     * @return List of public settings
     */
    List<BusinessSettingsDto> getPublicSettings();
    
    /**
     * Get all settings with pagination
     * @param active Filter by active status (null for all)
     * @param pageable Pagination parameters
     * @return Paginated list of settings
     */
    PaginatedResponse<BusinessSettingsDto> getAllSettings(Boolean active, Pageable pageable);
    
    /**
     * Check if setting exists by key
     * @param settingKey Setting key
     * @return true if setting exists
     */
    boolean settingExistsByKey(String settingKey);
    
    /**
     * Check if setting exists by ID
     * @param settingId Setting ID
     * @return true if setting exists
     */
    boolean settingExistsById(Long settingId);
}
