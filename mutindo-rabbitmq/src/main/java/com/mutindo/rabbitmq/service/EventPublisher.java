package com.mutindo.rabbitmq.service;

import com.mutindo.exceptions.BusinessException; // Reusing existing exceptions
import com.mutindo.logging.annotation.PerformanceLog; // Reusing existing performance logging
import com.mutindo.rabbitmq.config.RabbitMQConfiguration;
import com.mutindo.rabbitmq.event.CBSEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Event publisher implementation for RabbitMQ messaging
 * Follows our established interface-driven pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher implements IEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish event to RabbitMQ with default routing
     */
    @Override
    @PerformanceLog // Reusing existing performance logging
    public void publishEvent(CBSEvent event) {
        log.info("Publishing event: {} - Entity: {}", event.getEventType(), event.getEntityId());

        try {
            // Determine routing key based on event type (small method)
            String routingKey = determineRoutingKey(event.getEventType());
            
            // Publish to RabbitMQ
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.CBS_EXCHANGE, routingKey, event);
            
            log.debug("Event published successfully: {} - Routing key: {}", 
                    event.getEventId(), routingKey);
            
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getEventId(), e);
            throw new BusinessException("Event publishing failed", "EVENT_PUBLISH_ERROR");
        }
    }

    /**
     * Publish event with custom routing key
     */
    @Override
    @PerformanceLog
    public void publishEvent(CBSEvent event, String routingKey) {
        log.info("Publishing event with custom routing: {} - Key: {}", event.getEventType(), routingKey);

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.CBS_EXCHANGE, routingKey, event);
            
            log.debug("Event published successfully with custom routing: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to publish event with custom routing: {}", event.getEventId(), e);
            throw new BusinessException("Event publishing failed", "EVENT_PUBLISH_ERROR");
        }
    }

    /**
     * Publish transaction posted event
     */
    @Override
    @PerformanceLog
    public void publishTransactionPosted(String transactionId, String accountId, 
                                       String amount, String transactionType) {
        log.info("Publishing transaction posted event: {}", transactionId);

        try {
            CBSEvent event = CBSEvent.transactionPosted(
                    transactionId, accountId, amount, transactionType,
                    getCurrentBranchId(), getCurrentUserId()
            );
            
            publishEvent(event, RabbitMQConfiguration.TRANSACTION_POSTED_KEY);
            
        } catch (Exception e) {
            log.error("Failed to publish transaction posted event: {}", transactionId, e);
            // Don't throw exception - event publishing shouldn't break main flow
        }
    }

    /**
     * Publish customer created event
     */
    @Override
    @PerformanceLog
    public void publishCustomerCreated(String customerId, String customerType, String branchId) {
        log.info("Publishing customer created event: {}", customerId);

        try {
            CBSEvent event = CBSEvent.customerCreated(
                    customerId, customerType, branchId, getCurrentUserId()
            );
            
            publishEvent(event, RabbitMQConfiguration.CUSTOMER_CREATED_KEY);
            
        } catch (Exception e) {
            log.error("Failed to publish customer created event: {}", customerId, e);
            // Don't throw exception - event publishing shouldn't break main flow
        }
    }

    /**
     * Publish account opened event
     */
    @Override
    @PerformanceLog
    public void publishAccountOpened(String accountId, String customerId, 
                                   String productCode, String branchId) {
        log.info("Publishing account opened event: {}", accountId);

        try {
            CBSEvent event = CBSEvent.accountOpened(
                    accountId, customerId, productCode, branchId, getCurrentUserId()
            );
            
            publishEvent(event, RabbitMQConfiguration.ACCOUNT_OPENED_KEY);
            
        } catch (Exception e) {
            log.error("Failed to publish account opened event: {}", accountId, e);
            // Don't throw exception - event publishing shouldn't break main flow
        }
    }

    // Private helper methods (small and focused)

    /**
     * Determine routing key based on event type
     */
    private String determineRoutingKey(String eventType) {
        return switch (eventType) {
            case "TRANSACTION_POSTED" -> RabbitMQConfiguration.TRANSACTION_POSTED_KEY;
            case "CUSTOMER_CREATED" -> RabbitMQConfiguration.CUSTOMER_CREATED_KEY;
            case "ACCOUNT_OPENED" -> RabbitMQConfiguration.ACCOUNT_OPENED_KEY;
            case "LOAN_DISBURSED" -> RabbitMQConfiguration.LOAN_DISBURSED_KEY;
            default -> "cbs.general";
        };
    }

    /**
     * Get current branch ID from context
     */
    private String getCurrentBranchId() {
        // This would extract from BranchContextHolder
        return "BR001"; // Placeholder for now
    }

    /**
     * Get current user ID from context
     */
    private String getCurrentUserId() {
        // This would extract from Spring Security context
        return "USER001"; // Placeholder for now
    }
}
