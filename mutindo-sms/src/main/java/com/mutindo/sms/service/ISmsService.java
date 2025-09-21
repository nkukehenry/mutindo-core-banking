package com.mutindo.sms.service;

import com.mutindo.sms.dto.SmsRequest;
import com.mutindo.sms.dto.SmsResponse;

/**
 * SMS service interface for polymorphic SMS operations
 * Follows our established pattern of interface-driven design
 */
public interface ISmsService {
    
    /**
     * Send SMS message
     * @param request SMS request with phone number and message
     * @return SMS response with status
     */
    SmsResponse sendSms(SmsRequest request);
}
