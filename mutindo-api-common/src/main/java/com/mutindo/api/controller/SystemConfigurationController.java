package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.settings.dto.SystemConfigurationDto;
import com.mutindo.settings.dto.CreateSystemConfigurationRequest;
import com.mutindo.settings.dto.UpdateSystemConfigurationRequest;
import com.mutindo.settings.service.ISystemConfigurationService;
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
 * System Configuration REST API controller
 * Complete CRUD operations for system configuration management
 */
@RestController
@RequestMapping("/api/v1/system-configurations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "System Configurations", description = "System configuration management operations")
public class SystemConfigurationController {

    private final ISystemConfigurationService systemConfigurationService;

    /**
     * Create new system configuration
     */
    @PostMapping
    @Operation(summary = "Create system configuration", description = "Create a new system configuration")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "CREATE_SYSTEM_CONFIG", entity = "SystemConfiguration")
    @PerformanceLog
    public ResponseEntity<BaseResponse<SystemConfigurationDto>> createConfiguration(@Valid @RequestBody CreateSystemConfigurationRequest request) {
        log.info("Creating system configuration via API - Key: {} - Category: {}", request.getConfigKey(), request.getCategory());

        try {
            SystemConfigurationDto configuration = systemConfigurationService.createConfiguration(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.<SystemConfigurationDto>builder()
                            .success(true)
                            .message("System configuration created successfully")
                            .data(configuration)
                            .build());
            
        } catch (Exception e) {
            log.error("Failed to create system configuration via API", e);
            throw e;
        }
    }

    /**
     * Get system configuration by key
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "Get system configuration by key", description = "Get system configuration by key")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<SystemConfigurationDto>> getConfigurationByKey(@PathVariable String configKey) {
        log.debug("Getting system configuration via API: {}", configKey);

        try {
            Optional<SystemConfigurationDto> configOpt = systemConfigurationService.getConfigurationByKey(configKey);
            
            if (configOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<SystemConfigurationDto>builder()
                        .success(true)
                        .message("System configuration retrieved successfully")
                        .data(configOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get system configuration via API: {}", configKey, e);
            throw e;
        }
    }

    /**
     * Get system configuration by ID
     */
    @GetMapping("/{configId}")
    @Operation(summary = "Get system configuration by ID", description = "Get system configuration by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<SystemConfigurationDto>> getConfigurationById(@PathVariable String configId) {
        log.debug("Getting system configuration via API: {}", configId);

        try {
            Long configIdLong = Long.parseLong(configId);
            Optional<SystemConfigurationDto> configOpt = systemConfigurationService.getConfigurationById(configIdLong);
            
            if (configOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<SystemConfigurationDto>builder()
                        .success(true)
                        .message("System configuration retrieved successfully")
                        .data(configOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get system configuration via API: {}", configId, e);
            throw e;
        }
    }

    /**
     * Update system configuration information
     */
    @PutMapping("/{configId}")
    @Operation(summary = "Update system configuration", description = "Update system configuration information")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "UPDATE_SYSTEM_CONFIG", entity = "SystemConfiguration")
    @PerformanceLog
    public ResponseEntity<BaseResponse<SystemConfigurationDto>> updateConfiguration(
            @PathVariable String configId, 
            @Valid @RequestBody UpdateSystemConfigurationRequest request) {
        
        log.info("Updating system configuration via API: {}", configId);

        try {
            Long configIdLong = Long.parseLong(configId);
            SystemConfigurationDto updatedConfiguration = systemConfigurationService.updateConfiguration(configIdLong, request);
            
            return ResponseEntity.ok(BaseResponse.<SystemConfigurationDto>builder()
                    .success(true)
                    .message("System configuration updated successfully")
                    .data(updatedConfiguration)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to update system configuration via API: {}", configId, e);
            throw e;
        }
    }

    /**
     * Get system configurations by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get system configurations by category", description = "Get system configurations by category")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<SystemConfigurationDto>>> getConfigurationsByCategory(@PathVariable String category) {
        log.debug("Getting system configurations by category via API: {}", category);

        try {
            List<SystemConfigurationDto> configurations = systemConfigurationService.getConfigurationsByCategory(category);
            
            return ResponseEntity.ok(BaseResponse.<List<SystemConfigurationDto>>builder()
                    .success(true)
                    .message("System configurations retrieved successfully")
                    .data(configurations)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get system configurations by category via API: {}", category, e);
            throw e;
        }
    }

    /**
     * Get system configurations by environment
     */
    @GetMapping("/environment/{environment}")
    @Operation(summary = "Get system configurations by environment", description = "Get system configurations by environment")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<SystemConfigurationDto>>> getConfigurationsByEnvironment(@PathVariable String environment) {
        log.debug("Getting system configurations by environment via API: {}", environment);

        try {
            List<SystemConfigurationDto> configurations = systemConfigurationService.getConfigurationsByEnvironment(environment);
            
            return ResponseEntity.ok(BaseResponse.<List<SystemConfigurationDto>>builder()
                    .success(true)
                    .message("System configurations retrieved successfully")
                    .data(configurations)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get system configurations by environment via API: {}", environment, e);
            throw e;
        }
    }

    /**
     * Get system configurations (isSystem = true)
     */
    @GetMapping("/system")
    @Operation(summary = "Get system configurations", description = "Get system configurations (isSystem = true)")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<List<SystemConfigurationDto>>> getSystemConfigurations() {
        log.debug("Getting system configurations via API");

        try {
            List<SystemConfigurationDto> configurations = systemConfigurationService.getSystemConfigurations();
            
            return ResponseEntity.ok(BaseResponse.<List<SystemConfigurationDto>>builder()
                    .success(true)
                    .message("System configurations retrieved successfully")
                    .data(configurations)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get system configurations via API", e);
            throw e;
        }
    }

    /**
     * Get all system configurations with pagination
     */
    @GetMapping
    @Operation(summary = "List system configurations", description = "Get all system configurations with pagination")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<SystemConfigurationDto>>> getAllConfigurations(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting all system configurations via API - Active: {}", active);

        try {
            PaginatedResponse<SystemConfigurationDto> response = systemConfigurationService.getAllConfigurations(active, pageable);
            
            return ResponseEntity.ok(BaseResponse.<PaginatedResponse<SystemConfigurationDto>>builder()
                    .success(true)
                    .message("System configurations retrieved successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get system configurations via API", e);
            throw e;
        }
    }

    /**
     * Deactivate system configuration
     */
    @DeleteMapping("/{configId}")
    @Operation(summary = "Deactivate system configuration", description = "Deactivate system configuration")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AuditLog(action = "DEACTIVATE_SYSTEM_CONFIG", entity = "SystemConfiguration")
    public ResponseEntity<BaseResponse<Void>> deactivateConfiguration(
            @PathVariable String configId,
            @RequestParam String reason) {
        
        log.info("Deactivating system configuration via API: {} - Reason: {}", configId, reason);

        try {
            Long configIdLong = Long.parseLong(configId);
            systemConfigurationService.deactivateConfiguration(configIdLong, reason);
            
            return ResponseEntity.ok(BaseResponse.<Void>builder()
                    .success(true)
                    .message("System configuration deactivated successfully")
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to deactivate system configuration via API: {}", configId, e);
            throw e;
        }
    }
}
