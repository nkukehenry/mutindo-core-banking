package com.mutindo.branch.service;

import com.mutindo.branch.dto.BranchDto;
import com.mutindo.branch.dto.BranchSearchRequest;
import com.mutindo.branch.dto.CreateBranchRequest;
import com.mutindo.branch.dto.UpdateBranchRequest;
import com.mutindo.branch.mapper.BranchMapper;
import com.mutindo.common.context.BranchContextHolder;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.entities.Branch;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.repositories.BranchRepository;
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
 * Branch service implementation with multi-branch support and context handling
 * Reuses existing infrastructure: Logging, Caching, Repositories, Validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService implements IBranchService {

    // Reusing existing infrastructure components via interfaces
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    /**
     * Create new branch with comprehensive validation and security
     */
    @Override
    @Transactional
    @AuditLog // Reusing existing audit logging
    @PerformanceLog // Reusing existing performance logging
    @CacheEvict(value = {"branches", "activeBranches"}, allEntries = true) // Clear branch cache
    public BranchDto createBranch(CreateBranchRequest request) {
        log.info("Creating branch: {} - Code: {}", request.getName(), request.getCode());

        // Validate request
        validateCreateBranchRequest(request);

        // Check for duplicate code
        if (branchRepository.existsByCode(request.getCode())) {
            throw new ValidationException("Branch code already exists: " + request.getCode());
        }

        // Create branch entity
        Branch branch = branchMapper.toEntity(request);
        branch.setActive(true);

        // Save branch
        Branch savedBranch = branchRepository.save(branch);

        log.info("Branch created successfully: {} - ID: {}", savedBranch.getName(), savedBranch.getId());
        return enrichBranchDto(branchMapper.toDto(savedBranch));
    }

    /**
     * Get branch by ID - cached for performance
     */
    @Override
    @Cacheable(value = "branches", key = "#branchId")
    @PerformanceLog
    public Optional<BranchDto> getBranchById(Long branchId) {
        log.debug("Getting branch by ID: {}", branchId);

        return branchRepository.findById(branchId)
                .map(branchMapper::toDto)
                .map(this::enrichBranchDto);
    }

    /**
     * Get branch by code - cached for performance
     */
    @Override
    @Cacheable(value = "branches", key = "#code")
    @PerformanceLog
    public Optional<BranchDto> getBranchByCode(String code) {
        log.debug("Getting branch by code: {}", code);

        return branchRepository.findByCode(code)
                .map(branchMapper::toDto)
                .map(this::enrichBranchDto);
    }

    /**
     * Update branch information
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"branches", "activeBranches"}, allEntries = true)
    public BranchDto updateBranch(Long branchId, UpdateBranchRequest request) {
        log.info("Updating branch: {}", branchId);

        // Find existing branch
        Branch existingBranch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException("Branch not found", "BRANCH_NOT_FOUND"));

        // Validate update request
        validateUpdateBranchRequest(request);

        // Update branch
        branchMapper.updateEntity(existingBranch, request);
        Branch savedBranch = branchRepository.save(existingBranch);

        log.info("Branch updated successfully: {} - ID: {}", savedBranch.getName(), savedBranch.getId());
        return enrichBranchDto(branchMapper.toDto(savedBranch));
    }

    /**
     * Deactivate branch (soft delete)
     */
    @Override
    @Transactional
    @AuditLog
    @CacheEvict(value = {"branches", "activeBranches"}, allEntries = true)
    public void deactivateBranch(Long branchId, String reason) {
        log.info("Deactivating branch: {} - Reason: {}", branchId, reason);

        // Find branch
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BusinessException("Branch not found", "BRANCH_NOT_FOUND"));

        // Validate deactivation
        validateBranchDeactivation(branch);

        // Deactivate branch
        branch.setActive(false);
        branchRepository.save(branch);

        log.info("Branch deactivated successfully: {} - ID: {}", branch.getName(), branch.getId());
    }

    /**
     * Get all branches with pagination
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<BranchDto> getAllBranches(Boolean active, Pageable pageable) {
        log.debug("Getting all branches - Active: {}", active);

        Page<Branch> branchPage;
        if (active != null) {
            if (active) {
                branchPage = branchRepository.findByActiveTrue(pageable);
            } else {
                branchPage = branchRepository.findByActiveFalse(pageable);
            }
        } else {
            branchPage = branchRepository.findAll(pageable);
        }

        List<BranchDto> branchDtos = branchPage.getContent()
                .stream()
                .map(branchMapper::toDto)
                .map(this::enrichBranchDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                branchDtos,
                branchPage.getNumber(),
                branchPage.getSize(),
                branchPage.getTotalElements()
        );
    }

    /**
     * Search branches with filters
     */
    @Override
    @PerformanceLog
    public PaginatedResponse<BranchDto> searchBranches(BranchSearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching branches with term: {}", searchRequest.getSearchTerm());

        Page<Branch> branchPage = branchRepository.searchActiveBranches(
                searchRequest.getSearchTerm(), pageable);

        List<BranchDto> branchDtos = branchPage.getContent()
                .stream()
                .map(branchMapper::toDto)
                .map(this::enrichBranchDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(
                branchDtos,
                branchPage.getNumber(),
                branchPage.getSize(),
                branchPage.getTotalElements()
        );
    }

    /**
     * Get all active branches - cached for performance
     */
    @Override
    @Cacheable(value = "activeBranches")
    @PerformanceLog
    public List<BranchDto> getActiveBranches() {
        log.debug("Getting all active branches");

        return branchRepository.findByActiveTrue()
                .stream()
                .map(branchMapper::toDto)
                .map(this::enrichBranchDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if branch exists by code
     */
    @Override
    @PerformanceLog
    public boolean branchExistsByCode(String code) {
        return branchRepository.existsByCode(code);
    }

    /**
     * Check if branch exists by ID
     */
    @Override
    @PerformanceLog
    public boolean branchExistsById(Long branchId) {
        return branchRepository.existsById(branchId);
    }

    /**
     * Get branch statistics
     */
    @Override
    @Cacheable(value = "branchStatistics", key = "#branchId")
    @PerformanceLog
    public BranchDto getBranchStatistics(Long branchId) {
        log.debug("Getting branch statistics for: {}", branchId);

        BranchDto branchDto = getBranchById(branchId)
                .orElseThrow(() -> new BusinessException("Branch not found", "BRANCH_NOT_FOUND"));

        // TODO: Add statistics computation when related repositories are available
        // branchDto.setCustomerCount(customerRepository.countByBranchId(branchId));
        // branchDto.setAccountCount(accountRepository.countByBranchId(branchId));
        // branchDto.setActiveUsersCount(userRepository.countActiveByBranchId(branchId));

        return branchDto;
    }

    // Private helper methods

    /**
     * Validate branch creation request
     */
    private void validateCreateBranchRequest(CreateBranchRequest request) {
        if (request == null) {
            throw new ValidationException("Branch request cannot be null");
        }

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new ValidationException("Branch code is required");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("Branch name is required");
        }

        // Validate code format
        if (!request.getCode().matches("^[A-Z0-9]+$")) {
            throw new ValidationException("Branch code must contain only uppercase letters and numbers");
        }
    }

    /**
     * Validate branch update request
     */
    private void validateUpdateBranchRequest(UpdateBranchRequest request) {
        if (request == null) {
            throw new ValidationException("Update request cannot be null");
        }
    }

    /**
     * Validate branch deactivation
     */
    private void validateBranchDeactivation(Branch branch) {
        // Check if branch has active customers
        // TODO: Implement when customer repository is available
        // long customerCount = customerRepository.countActiveByBranchId(branch.getId());
        // if (customerCount > 0) {
        //     throw new BusinessException("Cannot deactivate branch with active customers", "HAS_ACTIVE_CUSTOMERS");
        // }

        // Check if branch has active accounts
        // TODO: Implement when account repository is available
        // long accountCount = accountRepository.countActiveByBranchId(branch.getId());
        // if (accountCount > 0) {
        //     throw new BusinessException("Cannot deactivate branch with active accounts", "HAS_ACTIVE_ACCOUNTS");
        // }
    }

    /**
     * Enrich branch DTO with computed fields
     */
    private BranchDto enrichBranchDto(BranchDto dto) {
        // TODO: Add computed fields when related repositories are available
        // dto.setManagerName(getManagerName(dto.getManagerId()));
        // dto.setCustomerCount(customerRepository.countByBranchId(dto.getId()));
        // dto.setAccountCount(accountRepository.countByBranchId(dto.getId()));
        // dto.setActiveUsersCount(userRepository.countActiveByBranchId(dto.getId()));

        return dto;
    }
}
