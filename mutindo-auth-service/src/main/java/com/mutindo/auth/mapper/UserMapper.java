package com.mutindo.auth.mapper;

import com.mutindo.auth.dto.UserDto;
import com.mutindo.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * User mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to UserDto
     * Excludes sensitive information like password hash (not included in DTO)
     */
    @Mapping(target = "roles", ignore = true) // Will be set separately
    @Mapping(target = "permissions", ignore = true) // Will be set separately
    @Mapping(source = "userType", target = "userType")
    UserDto toDto(User entity);
}
