package com.mutindo.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for CBS event-driven architecture
 * Connects to mutindo.com with specified credentials
 */
@Configuration
public class RabbitMQConfiguration {

    // Exchange names
    public static final String CBS_EXCHANGE = "cbs.exchange";
    
    // Queue names for different event types
    public static final String TRANSACTION_POSTED_QUEUE = "cbs.transactions.posted";
    public static final String CUSTOMER_CREATED_QUEUE = "cbs.customers.created";
    public static final String ACCOUNT_OPENED_QUEUE = "cbs.accounts.opened";
    public static final String LOAN_DISBURSED_QUEUE = "cbs.loans.disbursed";
    public static final String NOTIFICATION_QUEUE = "cbs.notifications";
    
    // Routing keys
    public static final String TRANSACTION_POSTED_KEY = "transaction.posted";
    public static final String CUSTOMER_CREATED_KEY = "customer.created";
    public static final String ACCOUNT_OPENED_KEY = "account.opened";
    public static final String LOAN_DISBURSED_KEY = "loan.disbursed";
    public static final String NOTIFICATION_KEY = "notification.send";

    /**
     * CBS main exchange for all events
     */
    @Bean
    public TopicExchange cbsExchange() {
        return new TopicExchange(CBS_EXCHANGE, true, false);
    }

    /**
     * Transaction posted events queue
     */
    @Bean
    public Queue transactionPostedQueue() {
        return QueueBuilder.durable(TRANSACTION_POSTED_QUEUE).build();
    }

    /**
     * Customer created events queue
     */
    @Bean
    public Queue customerCreatedQueue() {
        return QueueBuilder.durable(CUSTOMER_CREATED_QUEUE).build();
    }

    /**
     * Account opened events queue
     */
    @Bean
    public Queue accountOpenedQueue() {
        return QueueBuilder.durable(ACCOUNT_OPENED_QUEUE).build();
    }

    /**
     * Loan disbursed events queue
     */
    @Bean
    public Queue loanDisbursedQueue() {
        return QueueBuilder.durable(LOAN_DISBURSED_QUEUE).build();
    }

    /**
     * Notification queue
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding transactionPostedBinding() {
        return BindingBuilder.bind(transactionPostedQueue()).to(cbsExchange()).with(TRANSACTION_POSTED_KEY);
    }

    @Bean
    public Binding customerCreatedBinding() {
        return BindingBuilder.bind(customerCreatedQueue()).to(cbsExchange()).with(CUSTOMER_CREATED_KEY);
    }

    @Bean
    public Binding accountOpenedBinding() {
        return BindingBuilder.bind(accountOpenedQueue()).to(cbsExchange()).with(ACCOUNT_OPENED_KEY);
    }

    @Bean
    public Binding loanDisbursedBinding() {
        return BindingBuilder.bind(loanDisbursedQueue()).to(cbsExchange()).with(LOAN_DISBURSED_KEY);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(cbsExchange()).with(NOTIFICATION_KEY);
    }

    /**
     * RabbitMQ template with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    /**
     * JSON message converter
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
