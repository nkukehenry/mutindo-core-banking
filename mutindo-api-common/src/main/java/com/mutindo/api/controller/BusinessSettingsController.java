package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.settings.dto.BusinessSettingsDto;
import com.mutindo.settings.dto.CreateBusinessSettingsRequest;
import com.mutindo.settings.dto.UpdateBusinessSettingsRequest;
import com.mutindo.settings.service.IBusinessSettingsService;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Business Settings REST API controller
 * Complete CRUD operations for business settings management
 */
@RestController
@RequestMapping("/api/v1/business-settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Business Settings", description = "Business settings management operations")
public class BusinessSettingsController {

    private final IBusinessSettingsService businessSettingsService;

    /**
     * Create new business setting
     */
    @PostMapping
    @Operation(summary = "Create business setting", description = "Create a new business setting")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "CREATE_BUSINESS_SETTING", entity = "BusinessSettings")
    @PerformanceLog
    public ResponseEntity<BaseResponse<BusinessSettingsDto>> createSetting(@Valid @RequestBody CreateBusinessSettingsRequest request) {
        log.info("Creating business setting via API - Key: {} - Type: {}", request.getSettingKey(), request.getSettingType());

        try {
            BusinessSettingsDto setting = businessSettingsService.createSetting(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.<BusinessSettingsDto>builder()
                            .success(true)
                            .message("Business setting created successfully")
                            .data(setting)
                            .build());
            
        } catch (Exception e) {
            log.error("Failed to create business setting via API", e);
            throw e;
        }
    }

    /**
     * Get business setting by key
     */
    @GetMapping("/key/{settingKey}")
    @Operation(summary = "Get business setting by key", description = "Get business setting by key")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<BusinessSettingsDto>> getSettingByKey(@PathVariable String settingKey) {
        log.debug("Getting business setting via API: {}", settingKey);

        try {
            Optional<BusinessSettingsDto> settingOpt = businessSettingsService.getSettingByKey(settingKey);
            
            if (settingOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<BusinessSettingsDto>builder()
                        .success(true)
                        .message("Business setting retrieved successfully")
                        .data(settingOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get business setting via API: {}", settingKey, e);
            throw e;
        }
    }

    /**
     * Get business setting by ID
     */
    @GetMapping("/{settingId}")
    @Operation(summary = "Get business setting by ID", description = "Get business setting by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<BusinessSettingsDto>> getSettingById(@PathVariable String settingId) {
        log.debug("Getting business setting via API: {}", settingId);

        try {
            Long settingIdLong = Long.parseLong(settingId);
            Optional<BusinessSettingsDto> settingOpt = businessSettingsService.getSettingById(settingIdLong);
            
            if (settingOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<BusinessSettingsDto>builder()
                        .success(true)
                        .message("Business setting retrieved successfully")
                        .data(settingOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get business setting via API: {}", settingId, e);
            throw e;
        }
    }

    /**
     * Update business setting information
     */
    @PutMapping("/{settingId}")
    @Operation(summary = "Update business setting", description = "Update business setting information")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "UPDATE_BUSINESS_SETTING", entity = "BusinessSettings")
    @PerformanceLog
    public ResponseEntity<BaseResponse<BusinessSettingsDto>> updateSetting(
            @PathVariable String settingId, 
            @Valid @RequestBody UpdateBusinessSettingsRequest request) {
        
        log.info("Updating business setting via API: {}", settingId);

        try {
            Long settingIdLong = Long.parseLong(settingId);
            BusinessSettingsDto updatedSetting = businessSettingsService.updateSetting(settingIdLong, request);
            
            return ResponseEntity.ok(BaseResponse.<BusinessSettingsDto>builder()
                    .success(true)
                    .message("Business setting updated successfully")
                    .data(updatedSetting)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to update business setting via API: {}", settingId, e);
            throw e;
        }
    }

    /**
     * Get business settings by type
     */
    @GetMapping("/type/{settingType}")
    @Operation(summary = "Get business settings by type", description = "Get business settings by type")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<BusinessSettingsDto>>> getSettingsByType(@PathVariable String settingType) {
        log.debug("Getting business settings by type via API: {}", settingType);

        try {
            List<BusinessSettingsDto> settings = businessSettingsService.getSettingsByType(settingType);
            
            return ResponseEntity.ok(BaseResponse.<List<BusinessSettingsDto>>builder()
                    .success(true)
                    .message("Business settings retrieved successfully")
                    .data(settings)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get business settings by type via API: {}", settingType, e);
            throw e;
        }
    }

    /**
     * Get business settings by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get business settings by category", description = "Get business settings by category")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<BusinessSettingsDto>>> getSettingsByCategory(@PathVariable String category) {
        log.debug("Getting business settings by category via API: {}", category);

        try {
            List<BusinessSettingsDto> settings = businessSettingsService.getSettingsByCategory(category);
            
            return ResponseEntity.ok(BaseResponse.<List<BusinessSettingsDto>>builder()
                    .success(true)
                    .message("Business settings retrieved successfully")
                    .data(settings)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get business settings by category via API: {}", category, e);
            throw e;
        }
    }

    /**
     * Get public business settings
     */
    @GetMapping("/public")
    @Operation(summary = "Get public business settings", description = "Get public business settings visible to all users")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<BusinessSettingsDto>>> getPublicSettings() {
        log.debug("Getting public business settings via API");

        try {
            List<BusinessSettingsDto> settings = businessSettingsService.getPublicSettings();
            
            return ResponseEntity.ok(BaseResponse.<List<BusinessSettingsDto>>builder()
                    .success(true)
                    .message("Public business settings retrieved successfully")
                    .data(settings)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get public business settings via API", e);
            throw e;
        }
    }

    /**
     * Get all business settings with pagination
     */
    @GetMapping
    @Operation(summary = "List business settings", description = "Get all business settings with pagination")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN') or hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<BusinessSettingsDto>>> getAllSettings(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting all business settings via API - Active: {}", active);

        try {
            PaginatedResponse<BusinessSettingsDto> response = businessSettingsService.getAllSettings(active, pageable);
            
            return ResponseEntity.ok(BaseResponse.<PaginatedResponse<BusinessSettingsDto>>builder()
                    .success(true)
                    .message("Business settings retrieved successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get business settings via API", e);
            throw e;
        }
    }

    /**
     * Deactivate business setting
     */
    @DeleteMapping("/{settingId}")
    @Operation(summary = "Deactivate business setting", description = "Deactivate business setting")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AuditLog(action = "DEACTIVATE_BUSINESS_SETTING", entity = "BusinessSettings")
    public ResponseEntity<BaseResponse<Void>> deactivateSetting(
            @PathVariable String settingId,
            @RequestParam String reason) {
        
        log.info("Deactivating business setting via API: {} - Reason: {}", settingId, reason);

        try {
            Long settingIdLong = Long.parseLong(settingId);
            businessSettingsService.deactivateSetting(settingIdLong, reason);
            
            return ResponseEntity.ok(BaseResponse.<Void>builder()
                    .success(true)
                    .message("Business setting deactivated successfully")
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to deactivate business setting via API: {}", settingId, e);
            throw e;
        }
    }
}
