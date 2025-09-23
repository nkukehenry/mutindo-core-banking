package com.mutindo.api.controller;

import com.mutindo.otp.service.IOtpService;
import com.mutindo.otp.dto.OtpGenerateRequest;
import com.mutindo.otp.dto.OtpVerifyRequest;
import com.mutindo.otp.dto.OtpResponse;
import com.mutindo.common.dto.BaseResponse;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * OTP REST API controller
 * Complete OTP generation, verification, and management operations
 */
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OTP", description = "OTP generation and verification operations")
public class OtpController {

    private final IOtpService otpService;

    /**
     * Generate OTP
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate OTP", description = "Generate OTP for user authentication or verification")
    @PerformanceLog
    public ResponseEntity<BaseResponse<OtpResponse>> generateOtp(@Valid @RequestBody OtpGenerateRequest request) {
        log.info("OTP generation request via API - Phone: {} - Purpose: {}", 
                request.getPhoneNumber(), request.getPurpose());

        try {
            OtpResponse response = otpService.generateOtp(request);
            
            log.info("OTP generated successfully via API - Phone: {}", request.getPhoneNumber());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(response, "OTP generated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to generate OTP via API - Phone: {}", request.getPhoneNumber(), e);
            throw e;
        }
    }

    /**
     * Verify OTP
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP code for authentication or verification")
    @PerformanceLog
    public ResponseEntity<BaseResponse<OtpResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        log.info("OTP verification request via API - Phone: {}", request.getPhoneNumber());

        try {
            OtpResponse response = otpService.verifyOtp(request);
            
            log.info("OTP verification completed via API - Phone: {} - Valid: {}", 
                    request.getPhoneNumber(), response.isValid());
            return ResponseEntity.ok(BaseResponse.success(response, "OTP verification completed"));
            
        } catch (Exception e) {
            log.error("Failed to verify OTP via API - Phone: {}", request.getPhoneNumber(), e);
            throw e;
        }
    }

    /**
     * Resend OTP
     */
    @PostMapping("/resend")
    @Operation(summary = "Resend OTP", description = "Resend OTP to user's phone number")
    @PerformanceLog
    public ResponseEntity<BaseResponse<OtpResponse>> resendOtp(@RequestParam String phoneNumber) {
        log.info("OTP resend request via API - Phone: {}", phoneNumber);

        try {
            OtpResponse response = otpService.resendOtp(phoneNumber);
            
            log.info("OTP resent successfully via API - Phone: {}", phoneNumber);
            return ResponseEntity.ok(BaseResponse.success(response, "OTP resent successfully"));
            
        } catch (Exception e) {
            log.error("Failed to resend OTP via API - Phone: {}", phoneNumber, e);
            throw e;
        }
    }

    /**
     * Invalidate OTP
     */
    @PostMapping("/invalidate")
    @Operation(summary = "Invalidate OTP", description = "Invalidate/cancel OTP for security purposes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SECURITY')")
    @AuditLog(action = "INVALIDATE_OTP", entity = "OTP")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Void>> invalidateOtp(@RequestParam String phoneNumber) {
        log.info("OTP invalidation request via API - Phone: {}", phoneNumber);

        try {
            otpService.invalidateOtp(phoneNumber);
            
            log.info("OTP invalidated successfully via API - Phone: {}", phoneNumber);
            return ResponseEntity.ok(BaseResponse.success(null, "OTP invalidated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to invalidate OTP via API - Phone: {}", phoneNumber, e);
            throw e;
        }
    }

    /**
     * Check OTP validity
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate OTP", description = "Check if OTP is valid and not expired")
    @PerformanceLog
    public ResponseEntity<BaseResponse<Boolean>> validateOtp(
            @RequestParam String phoneNumber,
            @RequestParam String otpCode) {
        log.debug("OTP validation request via API - Phone: {}", phoneNumber);

        try {
            boolean isValid = otpService.isOtpValid(phoneNumber, otpCode);
            
            log.debug("OTP validation completed via API - Phone: {} - Valid: {}", phoneNumber, isValid);
            return ResponseEntity.ok(BaseResponse.success(isValid, "OTP validation completed"));
            
        } catch (Exception e) {
            log.error("Failed to validate OTP via API - Phone: {}", phoneNumber, e);
            throw e;
        }
    }
}
