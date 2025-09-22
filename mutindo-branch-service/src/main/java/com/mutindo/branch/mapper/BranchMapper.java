package com.mutindo.branch.mapper;

import com.mutindo.branch.dto.BranchDto;
import com.mutindo.branch.dto.CreateBranchRequest;
import com.mutindo.branch.dto.UpdateBranchRequest;
import com.mutindo.entities.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Branch mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BranchMapper {

    /**
     * Convert Branch entity to BranchDto
     * Maps all fields except computed ones
     */
    @Mapping(target = "managerName", ignore = true) // Computed in service
    @Mapping(target = "customerCount", ignore = true) // Computed in service
    @Mapping(target = "accountCount", ignore = true) // Computed in service
    @Mapping(target = "activeUsersCount", ignore = true) // Computed in service
    BranchDto toDto(Branch entity);

    /**
     * Convert CreateBranchRequest to Branch entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Branch toEntity(CreateBranchRequest request);

    /**
     * Update existing Branch entity with UpdateBranchRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Code cannot be changed
    @Mapping(target = "active", ignore = true) // Active status managed separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget Branch entity, UpdateBranchRequest request);
}
