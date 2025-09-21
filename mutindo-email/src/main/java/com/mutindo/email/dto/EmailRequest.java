package com.mutindo.email.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Email request DTO - minimal and focused
 */
@Data
@Builder
public class EmailRequest {
    private String to;
    private String from;
    private String subject;
    private String body; // For simple emails
    private String templateName; // For template emails
    private Map<String, Object> templateVariables; // Variables for template processing
}
