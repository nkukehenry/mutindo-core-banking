package com.mutindo.chartofaccounts.dto;

import com.mutindo.common.enums.GLAccountType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * GL Account DTO for API responses
 */
@Data
@Builder
public class GLAccountDto {
    
    private Long id;
    private String code;
    private String name;
    private GLAccountType type;
    private Long parentId;
    private String currency;
    private Boolean isControlAccount;
    private Boolean active;
    private String description;
    private String category;
    private Integer level;
    private Boolean allowsPosting;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
