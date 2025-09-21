package com.mutindo.chartofaccounts.mapper;

import com.mutindo.chartofaccounts.dto.CreateGLAccountRequest;
import com.mutindo.chartofaccounts.dto.GLAccountDto;
import com.mutindo.entities.GLAccount;
import org.mapstruct.*;

/**
 * MapStruct mapper for GL Account entities and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GLAccountMapper {

    GLAccountDto toDto(GLAccount entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "level", ignore = true) // Calculated in service
    GLAccount toEntity(CreateGLAccountRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "code", ignore = true) // Cannot change code
    @Mapping(target = "level", ignore = true) // Calculated in service
    void updateEntity(@MappingTarget GLAccount entity, CreateGLAccountRequest request);
}
