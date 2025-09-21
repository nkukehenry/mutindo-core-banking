package com.mutindo.chartofaccounts.dto;

import com.mutindo.common.enums.GLAccountType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * GL Account Hierarchy DTO for tree structure
 */
@Data
@Builder
public class GLAccountHierarchyDto {
    
    private Long id;
    private String code;
    private String name;
    private GLAccountType type;
    private Integer level;
    private Boolean isControlAccount;
    private Boolean allowsPosting;
    private String currency;
    private List<GLAccountHierarchyDto> children;
}
