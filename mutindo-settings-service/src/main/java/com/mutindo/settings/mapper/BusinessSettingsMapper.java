package com.mutindo.settings.mapper;

import com.mutindo.settings.dto.BusinessSettingsDto;
import com.mutindo.settings.dto.CreateBusinessSettingsRequest;
import com.mutindo.settings.dto.UpdateBusinessSettingsRequest;
import com.mutindo.entities.BusinessSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Business Settings mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessSettingsMapper {

    /**
     * Convert BusinessSettings entity to BusinessSettingsDto
     */
    BusinessSettingsDto toDto(BusinessSettings entity);

    /**
     * Convert CreateBusinessSettingsRequest to BusinessSettings entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    BusinessSettings toEntity(CreateBusinessSettingsRequest request);

    /**
     * Update existing BusinessSettings entity with UpdateBusinessSettingsRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "settingKey", ignore = true) // Key cannot be changed
    @Mapping(target = "settingType", ignore = true) // Type cannot be changed
    @Mapping(target = "isSystem", ignore = true) // System flag cannot be changed
    @Mapping(target = "active", ignore = true) // Active status managed separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget BusinessSettings entity, UpdateBusinessSettingsRequest request);
}
