package com.mutindo.rabbitmq.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base CBS event for RabbitMQ messaging
 */
@Data
@Builder
public class CBSEvent {
    
    private String eventId;
    private String eventType; // TRANSACTION_POSTED, CUSTOMER_CREATED, etc.
    private String entityId; // ID of the entity the event relates to
    private String entityType; // TRANSACTION, CUSTOMER, ACCOUNT, LOAN, etc.
    private String branchId;
    private String userId; // User who triggered the event
    private LocalDateTime timestamp;
    private String correlationId; // For distributed tracing
    
    // Event payload
    private Map<String, Object> data;
    
    // Metadata
    private Map<String, Object> metadata;
    
    /**
     * Create transaction posted event
     */
    public static CBSEvent transactionPosted(String transactionId, String accountId, 
                                           String amount, String transactionType, 
                                           String branchId, String userId) {
        return CBSEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("TRANSACTION_POSTED")
                .entityId(transactionId)
                .entityType("TRANSACTION")
                .branchId(branchId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                        "transactionId", transactionId,
                        "accountId", accountId,
                        "amount", amount,
                        "transactionType", transactionType
                ))
                .build();
    }
    
    /**
     * Create customer created event
     */
    public static CBSEvent customerCreated(String customerId, String customerType, 
                                         String branchId, String userId) {
        return CBSEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("CUSTOMER_CREATED")
                .entityId(customerId)
                .entityType("CUSTOMER")
                .branchId(branchId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                        "customerId", customerId,
                        "customerType", customerType
                ))
                .build();
    }
    
    /**
     * Create account opened event
     */
    public static CBSEvent accountOpened(String accountId, String customerId, 
                                       String productCode, String branchId, String userId) {
        return CBSEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("ACCOUNT_OPENED")
                .entityId(accountId)
                .entityType("ACCOUNT")
                .branchId(branchId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .data(Map.of(
                        "accountId", accountId,
                        "customerId", customerId,
                        "productCode", productCode
                ))
                .build();
    }
}
