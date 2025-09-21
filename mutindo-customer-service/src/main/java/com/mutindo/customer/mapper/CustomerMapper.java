package com.mutindo.customer.mapper;

import com.mutindo.customer.dto.CustomerDto;
import com.mutindo.entities.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Customer mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    /**
     * Convert Customer entity to CustomerDto
     * Maps all fields except computed ones
     */
    @Mapping(source = "customerType", target = "customerType")
    @Mapping(target = "fullName", ignore = true) // Computed in service
    @Mapping(target = "branchName", ignore = true) // Computed in service
    @Mapping(target = "accountCount", ignore = true) // Computed in service
    @Mapping(target = "activeLoansCount", ignore = true) // Computed in service
    CustomerDto toDto(Customer entity);
}
