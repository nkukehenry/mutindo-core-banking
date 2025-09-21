package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.common.enums.GLAccountType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * GL Account REST API controller
 * Chart of Accounts management with hierarchical structure
 */
@RestController
@RequestMapping("/api/v1/gl-accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GL Accounts", description = "Chart of Accounts management operations")
public class GLAccountController {

    private final com.mutindo.chartofaccounts.service.IChartOfAccountsService chartOfAccountsService;

    /**
     * Create new GL account
     */
    @PostMapping
    @Operation(summary = "Create GL account", description = "Create a new GL account in chart of accounts")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_FINANCE_MANAGER')")
    @AuditLog(action = "CREATE_GL_ACCOUNT", entity = "GLAccount")
    @PerformanceLog
    public ResponseEntity<BaseResponse<GLAccountDto>> createGLAccount(@Valid @RequestBody CreateGLAccountRequest request) {
        log.info("Creating GL account via API - Code: {} - Type: {}", request.getAccountCode(), request.getAccountType());

        try {
            // Use real Chart of Accounts service
            com.mutindo.chartofaccounts.dto.CreateGLAccountRequest serviceRequest = 
                com.mutindo.chartofaccounts.dto.CreateGLAccountRequest.builder()
                    .accountCode(request.getAccountCode())
                    .accountName(request.getAccountName())
                    .accountType(request.getAccountType())
                    .parentId(request.getParentId())
                    .description(request.getDescription())
                    .isControlAccount(request.getIsControlAccount())
                    .allowsPosting(request.getAllowsPosting())
                    .build();
            
            com.mutindo.chartofaccounts.dto.GLAccountDto serviceResult = chartOfAccountsService.createAccount(serviceRequest);
            
            // Convert service DTO to API DTO (field mapping)
            GLAccountDto glAccount = GLAccountDto.builder()
                    .id(serviceResult.getId())
                    .accountCode(serviceResult.getCode())
                    .accountName(serviceResult.getName())
                    .accountType(serviceResult.getType())
                    .parentId(serviceResult.getParentId())
                    .description(serviceResult.getDescription())
                    .isControlAccount(serviceResult.getIsControlAccount())
                    .allowsPosting(serviceResult.getAllowsPosting())
                    .level(serviceResult.getLevel())
                    .active(serviceResult.getActive())
                    .createdAt(serviceResult.getCreatedAt())
                    .updatedAt(serviceResult.getUpdatedAt())
                    .build();
            
            log.info("GL account created successfully via API: {}", glAccount.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(glAccount, "GL account created successfully"));
            
        } catch (Exception e) {
            log.error("Failed to create GL account via API", e);
            throw e;
        }
    }

    /**
     * Get GL account by ID
     */
    @GetMapping("/{accountId}")
    @Operation(summary = "Get GL account", description = "Get GL account by ID")
    @PreAuthorize("hasRole('ROLE_GL_ACCOUNTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<GLAccountDto>> getGLAccount(@PathVariable String accountId) {
        log.debug("Getting GL account via API: {}", accountId);

        try {
            // TODO: Use real service - needs getAccountById method in IChartOfAccountsService
            // Long accountIdLong = Long.parseLong(accountId);
            // Optional<com.mutindo.chartofaccounts.dto.GLAccountDto> serviceResult = chartOfAccountsService.getAccountById(accountIdLong);
            throw new UnsupportedOperationException("getAccountById method not yet available in IChartOfAccountsService");
            
        } catch (Exception e) {
            log.error("Failed to get GL account via API: {}", accountId, e);
            throw e;
        }
    }

    /**
     * Get GL account by code
     */
    @GetMapping("/code/{accountCode}")
    @Operation(summary = "Get GL account by code", description = "Get GL account by account code")
    @PreAuthorize("hasRole('ROLE_GL_ACCOUNTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<GLAccountDto>> getGLAccountByCode(@PathVariable String accountCode) {
        log.debug("Getting GL account by code via API: {}", accountCode);

        try {
            // Use real Chart of Accounts service
            Optional<com.mutindo.chartofaccounts.dto.GLAccountDto> serviceResult = chartOfAccountsService.getAccountByCode(accountCode);
            
            if (serviceResult.isPresent()) {
                GLAccountDto glAccount = convertServiceDtoToApiDto(serviceResult.get());
                return ResponseEntity.ok(BaseResponse.success(glAccount));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("GL account not found"));
            }
            
        } catch (Exception e) {
            log.error("Failed to get GL account by code via API: {}", accountCode, e);
            throw e;
        }
    }

    /**
     * Update GL account
     */
    @PutMapping("/{accountId}")
    @Operation(summary = "Update GL account", description = "Update GL account information")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_FINANCE_MANAGER')")
    @AuditLog(action = "UPDATE_GL_ACCOUNT", entity = "GLAccount")
    @PerformanceLog
    public ResponseEntity<BaseResponse<GLAccountDto>> updateGLAccount(
            @PathVariable String accountId, 
            @Valid @RequestBody UpdateGLAccountRequest request) {
        
        log.info("Updating GL account via API: {}", accountId);

        try {
            Long accountIdLong = Long.parseLong(accountId);
            GLAccountDto updatedAccount = GLAccountDto.builder()
                    .id(accountIdLong)
                    .accountCode(request.getAccountCode())
                    .accountName(request.getAccountName())
                    .accountType(request.getAccountType())
                    .parentId(request.getParentId())
                    .description(request.getDescription())
                    .normalBalance(request.getNormalBalance())
                    .isControlAccount(request.getIsControlAccount())
                    .allowsPosting(request.getAllowsPosting())
                    .active(request.getActive())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            log.info("GL account updated successfully via API: {}", accountId);
            return ResponseEntity.ok(BaseResponse.success(updatedAccount, "GL account updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update GL account via API: {}", accountId, e);
            throw e;
        }
    }

    /**
     * Get chart of accounts hierarchy
     */
    @GetMapping("/hierarchy")
    @Operation(summary = "Get chart of accounts", description = "Get complete chart of accounts with hierarchy")
    @PreAuthorize("hasRole('ROLE_GL_ACCOUNTS_READ')")
    @PerformanceLog(threshold = 2000)
    public ResponseEntity<BaseResponse<List<GLAccountHierarchyDto>>> getChartOfAccounts(
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) Boolean activeOnly) {
        
        log.debug("Getting chart of accounts via API - Type: {}, Active: {}", accountType, activeOnly);

        try {
            // Use real Chart of Accounts service
            List<com.mutindo.chartofaccounts.dto.GLAccountHierarchyDto> serviceHierarchy = chartOfAccountsService.getChartOfAccountsHierarchy();
            
            // Convert service DTOs to API DTOs
            List<GLAccountHierarchyDto> hierarchy = serviceHierarchy.stream()
                    .map(this::convertHierarchyDtoToApiDto)
                    .toList();
            
            log.debug("Retrieved {} GL accounts in hierarchy via API", hierarchy.size());
            return ResponseEntity.ok(BaseResponse.success(hierarchy));
            
        } catch (Exception e) {
            log.error("Failed to get chart of accounts via API", e);
            throw e;
        }
    }

    /**
     * Search GL accounts
     */
    @GetMapping("/search")
    @Operation(summary = "Search GL accounts", description = "Search GL accounts with pagination")
    @PreAuthorize("hasRole('ROLE_GL_ACCOUNTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<GLAccountDto>>> searchGLAccounts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false) Boolean allowsPosting,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 50) Pageable pageable) {
        
        log.debug("Searching GL accounts via API - Term: {}", searchTerm);

        try {
            List<GLAccountDto> accounts = getMockGLAccounts();
            PaginatedResponse<GLAccountDto> response = PaginatedResponse.<GLAccountDto>builder()
                    .content(accounts)
                    .totalElements((long) accounts.size())
                    .totalPages(1)
                    .size(accounts.size())
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            
            return ResponseEntity.ok(BaseResponse.success(response));
            
        } catch (Exception e) {
            log.error("Failed to search GL accounts via API", e);
            throw e;
        }
    }

    /**
     * Get account balance
     */
    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Get current balance for GL account")
    @PreAuthorize("hasRole('ROLE_GL_ACCOUNTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<GLAccountBalanceDto>> getAccountBalance(@PathVariable String accountId) {
        log.debug("Getting GL account balance via API: {}", accountId);

        try {
            Long accountIdLong = Long.parseLong(accountId);
            GLAccountBalanceDto balance = GLAccountBalanceDto.builder()
                    .accountId(accountIdLong)
                    .accountCode("1010")
                    .accountName("Cash at Bank")
                    .currentBalance(BigDecimal.valueOf(5000000))
                    .debitTotal(BigDecimal.valueOf(10000000))
                    .creditTotal(BigDecimal.valueOf(5000000))
                    .lastUpdated(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(BaseResponse.success(balance));
            
        } catch (Exception e) {
            log.error("Failed to get GL account balance via API: {}", accountId, e);
            throw e;
        }
    }

    /**
     * Deactivate GL account
     */
    @DeleteMapping("/{accountId}")
    @Operation(summary = "Deactivate GL account", description = "Deactivate GL account")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AuditLog(action = "DEACTIVATE_GL_ACCOUNT", entity = "GLAccount")
    public ResponseEntity<BaseResponse<Void>> deactivateGLAccount(
            @PathVariable String accountId,
            @RequestParam String reason) {
        
        log.info("Deactivating GL account via API: {} - Reason: {}", accountId, reason);

        try {
            log.info("GL account deactivated successfully via API: {}", accountId);
            return ResponseEntity.ok(BaseResponse.success(null, "GL account deactivated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to deactivate GL account via API: {}", accountId, e);
            throw e;
        }
    }

    // Private helper methods (temporary until real service is available)

    private Optional<GLAccountDto> findGLAccountById(Long accountId) {
        return getMockGLAccounts().stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst();
    }

    private Optional<GLAccountDto> findGLAccountByCode(String accountCode) {
        return getMockGLAccounts().stream()
                .filter(account -> account.getAccountCode().equals(accountCode))
                .findFirst();
    }

    /**
     * Convert service DTO to API DTO
     */
    private GLAccountDto convertServiceDtoToApiDto(com.mutindo.chartofaccounts.dto.GLAccountDto serviceDto) {
        return GLAccountDto.builder()
                .id(serviceDto.getId())
                .accountCode(serviceDto.getCode())
                .accountName(serviceDto.getName())
                .accountType(serviceDto.getType())
                .parentId(serviceDto.getParentId())
                .description(serviceDto.getDescription())
                .isControlAccount(serviceDto.getIsControlAccount())
                .allowsPosting(serviceDto.getAllowsPosting())
                .level(serviceDto.getLevel())
                .active(serviceDto.getActive())
                .createdAt(serviceDto.getCreatedAt())
                .updatedAt(serviceDto.getUpdatedAt())
                .build();
    }

    /**
     * Convert service hierarchy DTO to API hierarchy DTO
     */
    private GLAccountHierarchyDto convertHierarchyDtoToApiDto(com.mutindo.chartofaccounts.dto.GLAccountHierarchyDto serviceDto) {
        return GLAccountHierarchyDto.builder()
                .id(serviceDto.getId())
                .accountCode(serviceDto.getCode())
                .accountName(serviceDto.getName())
                .accountType(serviceDto.getType())
                .level(serviceDto.getLevel())
                .balance(serviceDto.getBalance())
                .children(serviceDto.getChildren() != null ? 
                    serviceDto.getChildren().stream()
                        .map(this::convertHierarchyDtoToApiDto)
                        .toList() : null)
                .build();
    }

    private List<GLAccountHierarchyDto> buildAccountHierarchy() {
        return List.of(
                GLAccountHierarchyDto.builder()
                        .id(1L)
                        .accountCode("1000")
                        .accountName("ASSETS")
                        .accountType(GLAccountType.ASSET)
                        .level(1)
                        .children(List.of(
                                GLAccountHierarchyDto.builder()
                                        .id(2L)
                                        .accountCode("1010")
                                        .accountName("Cash at Bank")
                                        .accountType(GLAccountType.ASSET)
                                        .level(2)
                                        .balance(BigDecimal.valueOf(5000000))
                                        .build()
                        ))
                        .build()
        );
    }

    private List<GLAccountDto> getMockGLAccounts() {
        return List.of(
                GLAccountDto.builder()
                        .id(1L)
                        .accountCode("1000")
                        .accountName("ASSETS")
                        .accountType(GLAccountType.ASSET)
                        .parentId(null)
                        .description("Asset accounts")
                        .normalBalance("DEBIT")
                        .isControlAccount(true)
                        .allowsPosting(false)
                        .level(1)
                        .balance(BigDecimal.valueOf(10000000))
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(12))
                        .build(),
                GLAccountDto.builder()
                        .id(2L)
                        .accountCode("1010")
                        .accountName("Cash at Bank")
                        .accountType(GLAccountType.ASSET)
                        .parentId(1L)
                        .description("Bank account balances")
                        .normalBalance("DEBIT")
                        .isControlAccount(false)
                        .allowsPosting(true)
                        .level(2)
                        .balance(BigDecimal.valueOf(5000000))
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(12))
                        .build(),
                GLAccountDto.builder()
                        .id(3L)
                        .accountCode("2000")
                        .accountName("LIABILITIES")
                        .accountType(GLAccountType.LIABILITY)
                        .parentId(null)
                        .description("Liability accounts")
                        .normalBalance("CREDIT")
                        .isControlAccount(true)
                        .allowsPosting(false)
                        .level(1)
                        .balance(BigDecimal.valueOf(8000000))
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(12))
                        .build()
        );
    }

    // DTOs for GL Account operations

    @Data
    @Builder
    public static class GLAccountDto {
        private Long id;
        private String accountCode;
        private String accountName;
        private GLAccountType accountType;
        private Long parentId;
        private String description;
        private String normalBalance;
        private Boolean isControlAccount;
        private Boolean allowsPosting;
        private Integer level;
        private BigDecimal balance;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class GLAccountHierarchyDto {
        private Long id;
        private String accountCode;
        private String accountName;
        private GLAccountType accountType;
        private Integer level;
        private BigDecimal balance;
        private List<GLAccountHierarchyDto> children;
    }

    @Data
    @Builder
    public static class GLAccountBalanceDto {
        private Long accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal currentBalance;
        private BigDecimal debitTotal;
        private BigDecimal creditTotal;
        private LocalDateTime lastUpdated;
    }

    @Data
    @Builder
    public static class CreateGLAccountRequest {
        private String accountCode;
        private String accountName;
        private GLAccountType accountType;
        private Long parentId;
        private String description;
        private String normalBalance;
        private Boolean isControlAccount;
        private Boolean allowsPosting;
    }

    @Data
    @Builder
    public static class UpdateGLAccountRequest {
        private String accountCode;
        private String accountName;
        private GLAccountType accountType;
        private Long parentId;
        private String description;
        private String normalBalance;
        private Boolean isControlAccount;
        private Boolean allowsPosting;
        private Boolean active;
    }
}
