package com.mutindo.otp.service;

import com.mutindo.otp.dto.OtpGenerateRequest;
import com.mutindo.otp.dto.OtpVerifyRequest;
import com.mutindo.otp.dto.OtpResponse;

/**
 * OTP service interface for polymorphic OTP operations
 * Follows our established pattern of interface-driven design
 */
public interface IOtpService {
    
    /**
     * Generate OTP for user
     * @param request OTP generation request
     * @return OTP response with status and expiry
     */
    OtpResponse generateOtp(OtpGenerateRequest request);
    
    /**
     * Verify OTP code
     * @param request OTP verification request
     * @return Verification result
     */
    OtpResponse verifyOtp(OtpVerifyRequest request);
    
    /**
     * Resend OTP to user
     * @param phoneNumber Phone number to resend to
     * @return OTP response
     */
    OtpResponse resendOtp(String phoneNumber);
    
    /**
     * Invalidate/cancel OTP
     * @param phoneNumber Phone number to invalidate OTP for
     */
    void invalidateOtp(String phoneNumber);
    
    /**
     * Check if OTP is valid and not expired
     * @param phoneNumber Phone number
     * @param otpCode OTP code to check
     * @return true if valid
     */
    boolean isOtpValid(String phoneNumber, String otpCode);
}
