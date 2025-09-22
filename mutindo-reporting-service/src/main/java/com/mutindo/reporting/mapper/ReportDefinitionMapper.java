package com.mutindo.reporting.mapper;

import com.mutindo.reporting.dto.ReportDefinitionDto;
import com.mutindo.reporting.dto.CreateReportDefinitionRequest;
import com.mutindo.reporting.dto.UpdateReportDefinitionRequest;
import com.mutindo.entities.ReportDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Report Definition mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ReportDefinitionMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Convert ReportDefinition entity to ReportDefinitionDto
     */
    @Mapping(target = "outputFormats", expression = "java(parseJsonToList(entity.getOutputFormats()))")
    @Mapping(target = "emailRecipients", expression = "java(parseJsonToList(entity.getEmailRecipients()))")
    public abstract ReportDefinitionDto toDto(ReportDefinition entity);

    /**
     * Convert CreateReportDefinitionRequest to ReportDefinition entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "outputFormats", expression = "java(convertListToJson(request.getOutputFormats()))")
    @Mapping(target = "emailRecipients", expression = "java(convertListToJson(request.getEmailRecipients()))")
    public abstract ReportDefinition toEntity(CreateReportDefinitionRequest request);

    /**
     * Update existing ReportDefinition entity with UpdateReportDefinitionRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reportCode", ignore = true) // Code cannot be changed
    @Mapping(target = "isSystem", ignore = true) // System flag cannot be changed
    @Mapping(target = "active", ignore = true) // Active status managed separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "outputFormats", expression = "java(convertListToJson(request.getOutputFormats()))")
    @Mapping(target = "emailRecipients", expression = "java(convertListToJson(request.getEmailRecipients()))")
    public abstract void updateEntity(@MappingTarget ReportDefinition entity, UpdateReportDefinitionRequest request);

    /**
     * Parse JSON string to List<String>
     */
    protected List<String> parseJsonToList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Convert List<String> to JSON string
     */
    protected String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
