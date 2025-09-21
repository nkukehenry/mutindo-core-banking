package com.mutindo.common.enums;

/**
 * User types in the CBS system
 * SUPER_ADMIN - System administrator with full access
 * INSTITUTION_ADMIN - Can manage entire institution across all branches
 * BRANCH_MANAGER - Manages a specific branch
 * TELLER - Handles customer transactions
 * LOAN_OFFICER - Manages loans and credit
 * CUSTOMER_SERVICE - Handles customer support
 * AUDITOR - Reviews and audits transactions
 * VIEWER - Read-only access for reporting
 */
public enum UserType {
    SUPER_ADMIN,
    INSTITUTION_ADMIN,
    BRANCH_MANAGER,
    TELLER,
    LOAN_OFFICER,
    CUSTOMER_SERVICE,
    AUDITOR,
    VIEWER
}
