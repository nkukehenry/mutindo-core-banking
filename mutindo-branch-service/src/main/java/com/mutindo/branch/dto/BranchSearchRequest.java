package com.mutindo.branch.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Branch search request DTO
 */
@Data
@Builder
public class BranchSearchRequest {
    
    private String searchTerm;
    private String timezone;
    private Boolean active;
    private String managerId;
}
