package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.branch.dto.BranchDto;
import com.mutindo.branch.dto.BranchSearchRequest;
import com.mutindo.branch.dto.CreateBranchRequest;
import com.mutindo.branch.dto.UpdateBranchRequest;
import com.mutindo.branch.service.IBranchService;
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
 * Branch REST API controller
 * Complete CRUD operations for branch management
 */
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Branches", description = "Branch management operations")
public class BranchController {

    private final IBranchService branchService;

    /**
     * Create new branch
     */
    @PostMapping
    @Operation(summary = "Create branch", description = "Create a new branch")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "CREATE_BRANCH", entity = "Branch")
    @PerformanceLog
    public ResponseEntity<BaseResponse<BranchDto>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        log.info("Creating branch via API - Name: {} - Code: {}", request.getName(), request.getCode());

        try {
            BranchDto branch = branchService.createBranch(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.<BranchDto>builder()
                            .success(true)
                            .message("Branch created successfully")
                            .data(branch)
                            .build());
            
        } catch (Exception e) {
            log.error("Failed to create branch via API", e);
            throw e;
        }
    }

    /**
     * Get branch by ID
     */
    @GetMapping("/{branchId}")
    @Operation(summary = "Get branch", description = "Get branch by ID")
    @PreAuthorize("hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<BranchDto>> getBranch(@PathVariable String branchId) {
        log.debug("Getting branch via API: {}", branchId);

        try {
            Long branchIdLong = Long.parseLong(branchId);
            Optional<BranchDto> branchOpt = branchService.getBranchById(branchIdLong);
            
            if (branchOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.<BranchDto>builder()
                        .success(true)
                        .message("Branch retrieved successfully")
                        .data(branchOpt.get())
                        .build());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to get branch via API: {}", branchId, e);
            throw e;
        }
    }

    /**
     * Update branch information
     */
    @PutMapping("/{branchId}")
    @Operation(summary = "Update branch", description = "Update branch information")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_INSTITUTION_ADMIN')")
    @AuditLog(action = "UPDATE_BRANCH", entity = "Branch")
    @PerformanceLog
    public ResponseEntity<BaseResponse<BranchDto>> updateBranch(
            @PathVariable String branchId, 
            @Valid @RequestBody UpdateBranchRequest request) {
        
        log.info("Updating branch via API: {}", branchId);

        try {
            Long branchIdLong = Long.parseLong(branchId);
            BranchDto updatedBranch = branchService.updateBranch(branchIdLong, request);
            
            return ResponseEntity.ok(BaseResponse.<BranchDto>builder()
                    .success(true)
                    .message("Branch updated successfully")
                    .data(updatedBranch)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to update branch via API: {}", branchId, e);
            throw e;
        }
    }

    /**
     * Get all branches with pagination
     */
    @GetMapping
    @Operation(summary = "List branches", description = "Get all branches with pagination")
    @PreAuthorize("hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<BranchDto>>> getAllBranches(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting all branches via API - Active: {}", active);

        try {
            PaginatedResponse<BranchDto> response = branchService.getAllBranches(active, pageable);
            
            return ResponseEntity.ok(BaseResponse.<PaginatedResponse<BranchDto>>builder()
                    .success(true)
                    .message("Branches retrieved successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to get branches via API", e);
            throw e;
        }
    }

    /**
     * Search branches
     */
    @GetMapping("/search")
    @Operation(summary = "Search branches", description = "Search branches by name, code, or location")
    @PreAuthorize("hasRole('ROLE_BRANCHES_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<BranchDto>>> searchBranches(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String timezone,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching branches via API - Term: {}", searchTerm);

        try {
            BranchSearchRequest searchRequest = BranchSearchRequest.builder()
                    .searchTerm(searchTerm)
                    .timezone(timezone)
                    .active(active)
                    .build();
                    
            PaginatedResponse<BranchDto> response = branchService.searchBranches(searchRequest, pageable);
            
            return ResponseEntity.ok(BaseResponse.<PaginatedResponse<BranchDto>>builder()
                    .success(true)
                    .message("Branch search completed successfully")
                    .data(response)
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to search branches via API", e);
            throw e;
        }
    }

    /**
     * Deactivate branch
     */
    @DeleteMapping("/{branchId}")
    @Operation(summary = "Deactivate branch", description = "Deactivate branch")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AuditLog(action = "DEACTIVATE_BRANCH", entity = "Branch")
    public ResponseEntity<BaseResponse<Void>> deactivateBranch(
            @PathVariable String branchId,
            @RequestParam String reason) {
        
        log.info("Deactivating branch via API: {} - Reason: {}", branchId, reason);

        try {
            Long branchIdLong = Long.parseLong(branchId);
            branchService.deactivateBranch(branchIdLong, reason);
            
            return ResponseEntity.ok(BaseResponse.<Void>builder()
                    .success(true)
                    .message("Branch deactivated successfully")
                    .build());
            
        } catch (Exception e) {
            log.error("Failed to deactivate branch via API: {}", branchId, e);
            throw e;
        }
    }

}
