package com.mutindo.branch.service;

import com.mutindo.branch.dto.BranchDto;
import com.mutindo.branch.dto.BranchSearchRequest;
import com.mutindo.branch.dto.CreateBranchRequest;
import com.mutindo.branch.dto.UpdateBranchRequest;
import com.mutindo.common.dto.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Branch service interface for polymorphic branch operations
 * Follows our established pattern of interface-driven design
 */
public interface IBranchService {
    
    /**
     * Create new branch
     * @param request Branch creation details
     * @return Created branch information
     */
    BranchDto createBranch(CreateBranchRequest request);
    
    /**
     * Get branch by ID
     * @param branchId Branch ID
     * @return Branch information if found
     */
    Optional<BranchDto> getBranchById(Long branchId);
    
    /**
     * Get branch by code
     * @param code Branch code
     * @return Branch information if found
     */
    Optional<BranchDto> getBranchByCode(String code);
    
    /**
     * Update branch information
     * @param branchId Branch ID
     * @param request Update request
     * @return Updated branch information
     */
    BranchDto updateBranch(Long branchId, UpdateBranchRequest request);
    
    /**
     * Deactivate branch (soft delete)
     * @param branchId Branch ID
     * @param reason Deactivation reason
     */
    void deactivateBranch(Long branchId, String reason);
    
    /**
     * Get all branches with pagination
     * @param active Filter by active status (null for all)
     * @param pageable Pagination parameters
     * @return Paginated list of branches
     */
    PaginatedResponse<BranchDto> getAllBranches(Boolean active, Pageable pageable);
    
    /**
     * Search branches with filters
     * @param searchRequest Search criteria
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    PaginatedResponse<BranchDto> searchBranches(BranchSearchRequest searchRequest, Pageable pageable);
    
    /**
     * Get all active branches
     * @return List of active branches
     */
    List<BranchDto> getActiveBranches();
    
    /**
     * Check if branch exists by code
     * @param code Branch code
     * @return true if branch exists
     */
    boolean branchExistsByCode(String code);
    
    /**
     * Check if branch exists by ID
     * @param branchId Branch ID
     * @return true if branch exists
     */
    boolean branchExistsById(Long branchId);
    
    /**
     * Get branch statistics
     * @param branchId Branch ID
     * @return Branch statistics
     */
    BranchDto getBranchStatistics(Long branchId);
}
