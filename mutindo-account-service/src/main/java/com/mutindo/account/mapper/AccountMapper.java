package com.mutindo.account.mapper;

import com.mutindo.account.dto.AccountDto;
import com.mutindo.entities.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Account mapper using MapStruct - following our established pattern
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    /**
     * Convert Account entity to AccountDto
     * Maps all fields except computed ones
     */
    @Mapping(source = "status", target = "status")
    @Mapping(target = "customerName", ignore = true) // Computed in service
    @Mapping(target = "branchName", ignore = true) // Computed in service
    @Mapping(target = "productName", ignore = true) // Computed in service
    @Mapping(target = "totalTransactionsToday", ignore = true) // Computed in service
    @Mapping(target = "transactionCountToday", ignore = true) // Computed in service
    @Mapping(target = "hasActiveLoans", ignore = true) // Computed in service
    @Mapping(target = "lastTransactionDate", ignore = true) // Computed in service
    AccountDto toDto(Account entity);
}
