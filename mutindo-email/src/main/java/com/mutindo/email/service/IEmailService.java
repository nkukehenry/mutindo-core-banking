package com.mutindo.email.service;

import com.mutindo.email.dto.EmailRequest;
import com.mutindo.email.dto.EmailResponse;

/**
 * Email service interface for polymorphic email operations
 * Follows our established pattern of interface-driven design
 */
public interface IEmailService {
    
    /**
     * Send simple text email
     * @param request Email request with recipient and content
     * @return Email response with status
     */
    EmailResponse sendSimpleEmail(EmailRequest request);
    
    /**
     * Send template-based email
     * @param request Email request with template name and variables
     * @return Email response with status
     */
    EmailResponse sendTemplateEmail(EmailRequest request);
}
