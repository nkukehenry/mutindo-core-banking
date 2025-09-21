package com.mutindo.common.context;

import com.mutindo.common.enums.UserType;
import lombok.Builder;
import lombok.Data;

/**
 * Branch context for multi-branch operations
 * Institution admins have no branchId (null), branch users have specific branchId
 */
@Data
@Builder
public class BranchContext {
    
    private Long userId;
    private Long branchId; // null for institution admins
    private UserType userType;
    private Long institutionId;
    
    /**
     * Check if user is institution admin (no branch restriction)
     */
    public boolean isInstitutionAdmin() {
        return UserType.INSTITUTION_ADMIN.equals(userType) && branchId == null;
    }
    
    /**
     * Check if user belongs to a specific branch
     */
    public boolean isBranchUser() {
        // Branch users are all non-admin user types that have a branch assignment
        return !isInstitutionAdmin() && !UserType.SUPER_ADMIN.equals(userType) && branchId != null;
    }
    
    /**
     * Check if user can access a specific branch
     * Institution admins can access all branches, branch users only their own
     */
    public boolean canAccessBranch(Long targetBranchId) {
        if (isInstitutionAdmin()) {
            return true;
        }
        return branchId != null && branchId.equals(targetBranchId);
    }
}
