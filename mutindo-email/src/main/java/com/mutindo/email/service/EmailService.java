package com.mutindo.email.service;

import com.mutindo.email.dto.EmailRequest;
import com.mutindo.email.dto.EmailResponse;
import com.mutindo.logging.annotation.PerformanceLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email service implementation - focused only on sending emails
 * Follows our established interface-driven pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @PerformanceLog // Add performance logging
    public EmailResponse sendSimpleEmail(EmailRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            message.setFrom(request.getFrom());

            mailSender.send(message);
            
            log.info("Simple email sent successfully to: {}", request.getTo());
            return EmailResponse.success("Email sent successfully");
            
        } catch (Exception e) {
            log.error("Failed to send simple email", e);
            return EmailResponse.failed("Email sending failed: " + e.getMessage());
        }
    }

    @Override
    @PerformanceLog // Add performance logging
    public EmailResponse sendTemplateEmail(EmailRequest request) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setFrom(request.getFrom());

            // Process template with variables
            Context context = new Context();
            if (request.getTemplateVariables() != null) {
                request.getTemplateVariables().forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(request.getTemplateName(), context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            
            log.info("Template email sent successfully to: {}", request.getTo());
            return EmailResponse.success("Template email sent successfully");
            
        } catch (MessagingException e) {
            log.error("Failed to send template email", e);
            return EmailResponse.failed("Template email sending failed: " + e.getMessage());
        }
    }
}
