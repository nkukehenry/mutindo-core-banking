package com.mutindo.rabbitmq.service;

import com.mutindo.rabbitmq.event.CBSEvent;

/**
 * Event publisher interface for polymorphic event publishing
 * Follows our established pattern of interface-driven design
 */
public interface IEventPublisher {
    
    /**
     * Publish event to RabbitMQ
     * @param event Event to publish
     */
    void publishEvent(CBSEvent event);
    
    /**
     * Publish event with specific routing key
     * @param event Event to publish
     * @param routingKey Custom routing key
     */
    void publishEvent(CBSEvent event, String routingKey);
    
    /**
     * Publish transaction posted event
     * @param transactionId Transaction ID
     * @param accountId Account ID
     * @param amount Transaction amount
     * @param transactionType Transaction type
     */
    void publishTransactionPosted(String transactionId, String accountId, String amount, String transactionType);
    
    /**
     * Publish customer created event
     * @param customerId Customer ID
     * @param customerType Customer type
     * @param branchId Branch ID
     */
    void publishCustomerCreated(String customerId, String customerType, String branchId);
    
    /**
     * Publish account opened event
     * @param accountId Account ID
     * @param customerId Customer ID
     * @param productCode Product code
     * @param branchId Branch ID
     */
    void publishAccountOpened(String accountId, String customerId, String productCode, String branchId);
}
