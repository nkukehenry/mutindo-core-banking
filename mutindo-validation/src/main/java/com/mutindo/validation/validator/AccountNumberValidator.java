package com.mutindo.validation.validator;

import org.springframework.stereotype.Component;

/**
 * Account number validation - focused single responsibility
 */
@Component
public class AccountNumberValidator {
    
    private static final String ACCOUNT_NUMBER_PATTERN = "^[0-9]{10,12}$";
    
    public boolean isValid(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        
        return accountNumber.matches(ACCOUNT_NUMBER_PATTERN);
    }
    
    public void validate(String accountNumber) {
        if (!isValid(accountNumber)) {
            throw new com.mutindo.exceptions.ValidationException(
                "Account number must be 10-12 digits");
        }
    }
}
