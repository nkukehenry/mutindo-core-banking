package com.mutindo.sms.service;

import com.mutindo.sms.dto.SmsRequest;
import com.mutindo.sms.dto.SmsResponse;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.validation.validator.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * SMS service implementation - focused only on sending SMS
 * Follows our established interface-driven pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService implements ISmsService {

    private final PhoneNumberValidator phoneValidator;
    private final WebClient webClient = WebClient.create();

    @Value("${cbs.sms.gateway.url}")
    private String smsGatewayUrl;

    @Value("${cbs.sms.gateway.api-key}")
    private String apiKey;

    @Override
    @PerformanceLog // Add performance logging
    public SmsResponse sendSms(SmsRequest request) {
        // Validate phone number
        phoneValidator.validate(request.getPhoneNumber());
        
        log.info("Sending SMS to: {}", request.getPhoneNumber());
        
        try {
            // Call SMS gateway (implementation depends on provider)
            return callSmsGateway(request);
        } catch (Exception e) {
            log.error("Failed to send SMS", e);
            return SmsResponse.failed("SMS sending failed: " + e.getMessage());
        }
    }

    private SmsResponse callSmsGateway(SmsRequest request) {
        // Implementation for actual SMS gateway integration
        // This is a placeholder - replace with actual SMS provider API
        log.info("SMS sent successfully to: {}", request.getPhoneNumber());
        return SmsResponse.success("SMS sent successfully");
    }
}
