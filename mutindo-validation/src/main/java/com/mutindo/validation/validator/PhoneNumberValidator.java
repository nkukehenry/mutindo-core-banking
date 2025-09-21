package com.mutindo.validation.validator;

import org.springframework.stereotype.Component;

/**
 * Phone number validation - single responsibility
 */
@Component
public class PhoneNumberValidator {
    
    private static final String PHONE_PATTERN = "^\\+?[1-9]\\d{1,14}$";
    
    public boolean isValid(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        return phoneNumber.matches(PHONE_PATTERN);
    }
    
    public void validate(String phoneNumber) {
        if (!isValid(phoneNumber)) {
            throw new com.mutindo.exceptions.ValidationException(
                "Invalid phone number format");
        }
    }
}
