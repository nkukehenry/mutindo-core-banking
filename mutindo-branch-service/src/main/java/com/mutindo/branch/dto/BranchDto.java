package com.mutindo.branch.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Branch DTO for API responses - focused on essential branch information
 */
@Data
@Builder
public class BranchDto {
    
    private Long id;
    private String code;
    private String name;
    private String timezone;
    private String address;
    private String phone;
    private String email;
    private String managerId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    // Computed fields for UI
    private String managerName;
    private Integer customerCount;
    private Integer accountCount;
    private Integer activeUsersCount;
}
