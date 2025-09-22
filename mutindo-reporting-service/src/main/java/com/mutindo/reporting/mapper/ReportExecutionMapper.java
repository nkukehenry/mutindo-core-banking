package com.mutindo.reporting.mapper;

import com.mutindo.reporting.dto.ReportExecutionDto;
import com.mutindo.entities.ReportExecution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Report Execution mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportExecutionMapper {

    /**
     * Convert ReportExecution entity to ReportExecutionDto
     */
    ReportExecutionDto toDto(ReportExecution entity);

    /**
     * Convert ReportExecutionDto to ReportExecution entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ReportExecution toEntity(ReportExecutionDto dto);
}
