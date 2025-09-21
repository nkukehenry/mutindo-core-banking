package com.mutindo.common.context;

/**
 * Thread-local holder for branch context
 * Allows access to current user's branch context throughout the request lifecycle
 */
public class BranchContextHolder {
    
    private static final ThreadLocal<BranchContext> contextHolder = new ThreadLocal<>();
    
    public static void setContext(BranchContext context) {
        contextHolder.set(context);
    }
    
    public static BranchContext getContext() {
        return contextHolder.get();
    }
    
    public static void clearContext() {
        contextHolder.remove();
    }
    
    /**
     * Get current user's branch ID (null for institution admins)
     */
    public static Long getCurrentBranchId() {
        BranchContext context = getContext();
        return context != null ? context.getBranchId() : null;
    }
    
    /**
     * Get current user ID
     */
    public static Long getCurrentUserId() {
        BranchContext context = getContext();
        return context != null ? context.getUserId() : null;
    }
    
    /**
     * Check if current user is institution admin
     */
    public static boolean isCurrentUserInstitutionAdmin() {
        BranchContext context = getContext();
        return context != null && context.isInstitutionAdmin();
    }
    
    /**
     * Check if current user can access a specific branch
     */
    public static boolean canCurrentUserAccessBranch(Long branchId) {
        BranchContext context = getContext();
        return context != null && context.canAccessBranch(branchId);
    }
}
