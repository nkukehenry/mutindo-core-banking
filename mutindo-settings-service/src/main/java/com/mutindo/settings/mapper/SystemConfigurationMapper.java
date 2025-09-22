package com.mutindo.settings.mapper;

import com.mutindo.settings.dto.SystemConfigurationDto;
import com.mutindo.settings.dto.CreateSystemConfigurationRequest;
import com.mutindo.settings.dto.UpdateSystemConfigurationRequest;
import com.mutindo.entities.SystemConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * System Configuration mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SystemConfigurationMapper {

    /**
     * Convert SystemConfiguration entity to SystemConfigurationDto
     */
    SystemConfigurationDto toDto(SystemConfiguration entity);

    /**
     * Convert CreateSystemConfigurationRequest to SystemConfiguration entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    SystemConfiguration toEntity(CreateSystemConfigurationRequest request);

    /**
     * Update existing SystemConfiguration entity with UpdateSystemConfigurationRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "configKey", ignore = true) // Key cannot be changed
    @Mapping(target = "isSystem", ignore = true) // System flag cannot be changed
    @Mapping(target = "active", ignore = true) // Active status managed separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget SystemConfiguration entity, UpdateSystemConfigurationRequest request);
}
