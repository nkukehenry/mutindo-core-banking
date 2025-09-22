package com.mutindo.settings.service;

import com.mutindo.settings.dto.BusinessSettingsDto;
import com.mutindo.settings.dto.CreateBusinessSettingsRequest;
import com.mutindo.settings.dto.UpdateBusinessSettingsRequest;
import com.mutindo.settings.mapper.BusinessSettingsMapper;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.entities.BusinessSettings;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.BusinessSettingsRepository;
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
 * Business Settings service implementation with comprehensive settings management
 * Reuses existing infrastructure: Logging, Caching, Repositories, Validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessSettingsService implements IBusinessSettingsService {

    // Reusing existing infrastructure components via interfaces
    private final BusinessSettingsRepository businessSettingsRepository;
    private final BusinessSettingsMapper businessSettingsMapper;

    /**
     * Create new business setting with comprehensive validation and security
     */
    @Override
    @Transactional
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance logging
    @CacheEvict(value = {"businessSettings", "publicSettings"}, allEntries = true) // Clear settings cache
    public BusinessSettingsDto createSetting(CreateBusinessSettingsRequest request) {
        log.info("Creating business setting: {} - Type: {}", request.getSettingKey(), request.getSettingType());

        // Validate request
        validateCreateSettingRequest(request);

        // Check for duplicate key
        if (businessSettingsRepository.existsBySettingKey(request.getSettingKey())) {
            throw new ValidationException("Setting key already exists: " + request.getSettingKey());
        }

        // Create setting entity
        BusinessSettings setting = businessSettingsMapper.toEntity(request);
        setting.setActive(true);

        // Set defaults
        if (setting.getIsEncrypted() == null) {
            setting.setIsEncrypted(false);
        }
        if (setting.getIsSystem() == null) {
            setting.setIsSystem(false);
        }
        if (setting.getIsPublic() == null) {
            setting.setIsPublic(false);
        }

        // Save setting
        BusinessSettings savedSetting = businessSettingsRepository.save(setting);

        log.info("Business setting created successfully: {} - ID: {}", savedSetting.getSettingKey(), savedSetting.getId());
        return businessSettingsMapper.toDto(savedSetting);
    }

    /**
     * Get setting by key - cached for performance
     */
    @Override
    @Cacheable(value = "businessSettings", key = "#settingKey")
    @PerformanceLog
    public Optional<BusinessSettingsDto> getSettingByKey(String settingKey) {
        log.debug("Getting business setting by key: {}", settingKey);

        return businessSettingsRepository.findBySettingKey(settingKey)
                .map(businessSettingsMapper::toDto);
    }

    /**
     * Get setting by ID - cached for performance
     */
    @Override
    @Cacheable(value = "businessSettings", key = "#settingId")
    @PerformanceLog
    public Optional<BusinessSettingsDto> getSettingById(Long settingId) {
        log.debug("Getting business setting by ID: {}", settingId);

        return businessSettingsRepository.findById(settingId)
                .map(businessSettingsMapper::toDto);
    }

    /**
     * Update setting information
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"businessSettings", "publicSettings"}, allEntries = true)
    public BusinessSettingsDto updateSetting(Long settingId, UpdateBusinessSettingsRequest request) {
        log.info("Updating business setting: {}", settingId);

        // Find existing setting
        BusinessSettings existingSetting = businessSettingsRepository.findById(settingId)
                .orElseThrow(() -> new BusinessException("Business setting not found", "SETTING_NOT_FOUND"));

        // Validate update request
        validateUpdateSettingRequest(request);

        // Check if setting is system setting
        if (existingSetting.getIsSystem()) {
            throw new BusinessException("Cannot update system settings", "SYSTEM_SETTING_READONLY");
        }

        // Update setting
        businessSettingsMapper.updateEntity(existingSetting, request);
        BusinessSettings savedSetting = businessSettingsRepository.save(existingSetting);

        log.info("Business setting updated successfully: {} - ID: {}", savedSetting.getSettingKey(), savedSetting.getId());
        return businessSettingsMapper.toDto(savedSetting);
    }

    /**
     * Deactivate setting (soft delete)
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"businessSettings", "publicSettings"}, allEntries = true)
    public void deactivateSetting(Long settingId, String reason) {
        log.info("Deactivating business setting: {} - Reason: {}", settingId, reason);

        // Find setting
        BusinessSettings setting = businessSettingsRepository.findById(settingId)
                .orElseThrow(() -> new BusinessException("Business setting not found", "SETTING_NOT_FOUND"));

        // Check if setting is system setting
        if (setting.getIsSystem()) {
            throw new BusinessException("Cannot deactivate system settings", "SYSTEM_SETTING_READONLY");
        }

        // Deactivate setting
        setting.setActive(false);
        businessSettingsRepository.save(setting);

        log.info("Business setting deactivated successfully: {} - ID: {}", setting.getSettingKey(), setting.getId());
    }

    /**
     * Get settings by type - cached for performance
     */
    @Override
    @Cacheable(value = "businessSettings", key = "'type:' + #settingType")
    @PerformanceLog
    public List<BusinessSettingsDto> getSettingsByType(String settingType) {
        log.debug("Getting business settings by type: {}", settingType);

        return businessSettingsRepository.findActiveBySettingType(settingType)
                .stream()
                .map(businessSettingsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get settings by category - cached for performance
     */
    @Override
    @Cacheable(value = "businessSettings", key = "'category:' + #category")
    @PerformanceLog
    public List<BusinessSettingsDto> getSettingsByCategory(String category) {
        log.debug("Getting business settings by category: {}", category);

        return businessSettingsRepository.findActiveByCategory(category)
                .stream()
                .map(businessSettingsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active settings - cached for performance
     */
    @Override
    @Cacheable(value = "businessSettings", key = "'active'")
    @PerformanceLog
    public List<BusinessSettingsDto> getActiveSettings() {
        log.debug("Getting all active business settings");

        return businessSettingsRepository.findByActiveTrue()
                .stream()
                .map(businessSettingsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get public settings - cached for performance
     */
    @Override
    @Cacheable(value = "publicSettings")
    @PerformanceLog
    public List<BusinessSettingsDto> getPublicSettings() {
        log.debug("Getting public business settings");

        return businessSettingsRepository.findActivePublicSettings()
                .stream()
                .map(businessSettingsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all settings with pagination
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<BusinessSettingsDto> getAllSettings(Boolean active, Pageable pageable) {
        log.debug("Getting all business settings - Active: {}", active);

        Page<BusinessSettings> settingsPage;
        if (active != null) {
            if (active) {
                settingsPage = businessSettingsRepository.findByActiveTrue(pageable);
            } else {
                settingsPage = businessSettingsRepository.findByActiveFalse(pageable);
            }
        } else {
            settingsPage = businessSettingsRepository.findAll(pageable);
        }

        List<BusinessSettingsDto> settingsDtos = settingsPage.getContent()
                .stream()
                .map(businessSettingsMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                settingsDtos,
                settingsPage.getNumber(),
                settingsPage.getSize(),
                settingsPage.getTotalElements()
        );
    }

    /**
     * Check if setting exists by key
     */
    @Override
    @PerformanceLog
    public boolean settingExistsByKey(String settingKey) {
        return businessSettingsRepository.existsBySettingKey(settingKey);
    }

    /**
     * Check if setting exists by ID
     */
    @Override
    @PerformanceLog
    public boolean settingExistsById(Long settingId) {
        return businessSettingsRepository.existsById(settingId);
    }

    // Private helper methods

    /**
     * Validate setting creation request
     */
    private void validateCreateSettingRequest(CreateBusinessSettingsRequest request) {
        if (request == null) {
            throw new ValidationException("Setting request cannot be null");
        }

        if (request.getSettingKey() == null || request.getSettingKey().trim().isEmpty()) {
            throw new ValidationException("Setting key is required");
        }

        if (request.getSettingType() == null || request.getSettingType().trim().isEmpty()) {
            throw new ValidationException("Setting type is required");
        }

        // Validate key format
        if (!request.getSettingKey().matches("^[A-Z0-9_]+$")) {
            throw new ValidationException("Setting key must contain only uppercase letters, numbers, and underscores");
        }
    }

    /**
     * Validate setting update request
     */
    private void validateUpdateSettingRequest(UpdateBusinessSettingsRequest request) {
        if (request == null) {
            throw new ValidationException("Update request cannot be null");
        }
    }
}
