package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    // TODO: Inject IBranchService when available
    // private final IBranchService branchService;

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
            // TODO: Use real service when available
            BranchDto branch = BranchDto.builder()
                    .id(System.currentTimeMillis())
                    .name(request.getName())
                    .code(request.getCode())
                    .address(request.getAddress())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .managerName(request.getManagerName())
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("Branch created successfully via API: {}", branch.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(branch, "Branch created successfully"));
            
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
            // TODO: Use real service when available
            Long branchIdLong = Long.parseLong(branchId);
            Optional<BranchDto> branchOpt = findBranchById(branchIdLong);
            
            if (branchOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(branchOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Branch not found"));
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
            // TODO: Use real service when available
            Long branchIdLong = Long.parseLong(branchId);
            BranchDto updatedBranch = BranchDto.builder()
                    .id(branchIdLong)
                    .name(request.getName())
                    .code(request.getCode())
                    .address(request.getAddress())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .managerName(request.getManagerName())
                    .active(request.getActive())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            log.info("Branch updated successfully via API: {}", branchId);
            return ResponseEntity.ok(BaseResponse.success(updatedBranch, "Branch updated successfully"));
            
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
            // TODO: Use real service when available
            List<BranchDto> branches = getMockBranches();
            PaginatedResponse<BranchDto> response = PaginatedResponse.<BranchDto>builder()
                    .content(branches)
                    .totalElements((long) branches.size())
                    .totalPages(1)
                    .size(branches.size())
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            
            log.debug("Found {} branches via API", branches.size());
            return ResponseEntity.ok(BaseResponse.success(response));
            
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
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching branches via API - Term: {}", searchTerm);

        try {
            // TODO: Use real service when available
            List<BranchDto> branches = getMockBranches();
            PaginatedResponse<BranchDto> response = PaginatedResponse.<BranchDto>builder()
                    .content(branches)
                    .totalElements((long) branches.size())
                    .totalPages(1)
                    .size(branches.size())
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            
            return ResponseEntity.ok(BaseResponse.success(response));
            
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
            // TODO: Use real service when available
            log.info("Branch deactivated successfully via API: {}", branchId);
            return ResponseEntity.ok(BaseResponse.success(null, "Branch deactivated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to deactivate branch via API: {}", branchId, e);
            throw e;
        }
    }

    // Private helper methods (temporary until real service is available)

    private Optional<BranchDto> findBranchById(Long branchId) {
        return getMockBranches().stream()
                .filter(branch -> branch.getId().equals(branchId))
                .findFirst();
    }

    private List<BranchDto> getMockBranches() {
        return List.of(
                BranchDto.builder()
                        .id(1L)
                        .name("Head Office")
                        .code("HO001")
                        .address("Kampala Central Business District")
                        .phone("+256-700-123456")
                        .email("headoffice@mutindo.com")
                        .managerName("John Manager")
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(12))
                        .build(),
                BranchDto.builder()
                        .id(2L)
                        .name("Kampala Main Branch")
                        .code("KLA001")
                        .address("Kampala Road, Kampala")
                        .phone("+256-700-123457")
                        .email("kampala@mutindo.com")
                        .managerName("Jane Manager")
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(6))
                        .build(),
                BranchDto.builder()
                        .id(3L)
                        .name("Entebbe Branch")
                        .code("ENT001")
                        .address("Airport Road, Entebbe")
                        .phone("+256-700-123458")
                        .email("entebbe@mutindo.com")
                        .managerName("Bob Manager")
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(3))
                        .build()
        );
    }

    // DTOs for Branch operations

    @Data
    @Builder
    public static class BranchDto {
        private Long id;
        private String name;
        private String code;
        private String address;
        private String phone;
        private String email;
        private String managerName;
        private String region;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class CreateBranchRequest {
        private String name;
        private String code;
        private String address;
        private String phone;
        private String email;
        private String managerName;
        private String region;
    }

    @Data
    @Builder
    public static class UpdateBranchRequest {
        private String name;
        private String code;
        private String address;
        private String phone;
        private String email;
        private String managerName;
        private String region;
        private Boolean active;
    }
}
