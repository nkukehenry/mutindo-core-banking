package com.mutindo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration for Mutindo Core Banking System
 * Provides comprehensive API documentation with security configuration
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mutindoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mutindo Core Banking System API")
                        .description("""
                                **Mutindo Core Banking System** - Enterprise-grade banking platform
                                
                                ## 🏦 **System Overview**
                                A comprehensive core banking system built with Spring Boot 3.2.0, featuring:
                                
                                ### **Core Features**
                                - **Customer Management** - Complete customer lifecycle management
                                - **Account Management** - Multi-currency account operations
                                - **Transaction Processing** - Real-time transaction processing with audit trails
                                - **Loan Management** - Loan origination, servicing, and collections
                                - **GL & Chart of Accounts** - Multi-level chart of accounts with posting engine
                                - **Branch Management** - Multi-branch banking operations
                                - **User Management** - Role-based access control with JWT authentication
                                - **Reporting** - Comprehensive reporting and analytics
                                - **OTP Service** - SMS-based OTP verification
                                - **System Configuration** - Dynamic system configuration management
                                
                                ### **Security Features**
                                - JWT-based authentication
                                - Role-based authorization (ADMIN, USER, INSTITUTION_ADMIN)
                                - Multi-branch access control
                                - Audit logging for all operations
                                - Performance monitoring
                                
                                ### **Architecture**
                                - **Microservices-ready** modular design
                                - **Interface-driven** development with Strategy, Factory, Repository patterns
                                - **Event-driven** architecture with RabbitMQ
                                - **Redis caching** for performance optimization
                                - **MySQL database** with Flyway migrations
                                
                                ## 🔐 **Authentication**
                                Most endpoints require JWT authentication. Use the `/api/v1/auth/login` endpoint to obtain a token,
                                then include it in the Authorization header: `Bearer YOUR_JWT_TOKEN`
                                
                                ## 📊 **Response Format**
                                All API responses follow a consistent format:
                                ```json
                                {
                                  "success": true,
                                  "message": "Operation completed successfully",
                                  "data": { ... },
                                  "timestamp": "2024-01-01T00:00:00Z"
                                }
                                ```
                                
                                ## 🚀 **Getting Started**
                                1. **Login** - Use `/api/v1/auth/login` to authenticate
                                2. **Explore** - Browse available endpoints by category
                                3. **Test** - Use the "Try it out" feature to test endpoints
                                4. **Monitor** - Check `/health` for system status
                                """)
                        .version("1.0.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("Mutindo Development Team")
                                .email("dev@mutindo.com")
                                .url("https://mutindo.com"))
                        .license(new License()
                                .name("Mutindo License")
                                .url("https://mutindo.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.mutindo.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/v1/auth/login endpoint")))
                .tags(List.of(
                        new Tag()
                                .name("Authentication")
                                .description("User authentication and authorization operations"),
                        new Tag()
                                .name("Customers")
                                .description("Customer management operations - create, read, update, delete customers"),
                        new Tag()
                                .name("Accounts")
                                .description("Account management operations - account creation, balance inquiries, account updates"),
                        new Tag()
                                .name("Transactions")
                                .description("Transaction processing operations - deposits, withdrawals, transfers, transaction history"),
                        new Tag()
                                .name("Loans")
                                .description("Loan management operations - loan origination, servicing, collections"),
                        new Tag()
                                .name("GL Accounts")
                                .description("General Ledger and Chart of Accounts management"),
                        new Tag()
                                .name("Branches")
                                .description("Branch management operations - branch creation, updates, branch-specific operations"),
                        new Tag()
                                .name("Users")
                                .description("User management operations - user creation, role assignment, user updates"),
                        new Tag()
                                .name("Products")
                                .description("Banking product management - savings, checking, loan products"),
                        new Tag()
                                .name("Custom Fields")
                                .description("Custom field management for dynamic form configurations"),
                        new Tag()
                                .name("Posting Engine")
                                .description("Transaction posting operations - synchronous and asynchronous posting"),
                        new Tag()
                                .name("Reporting")
                                .description("Report generation and management operations"),
                        new Tag()
                                .name("OTP Service")
                                .description("OTP generation, verification, and management operations"),
                        new Tag()
                                .name("System Configuration")
                                .description("System configuration management operations"),
                        new Tag()
                                .name("Business Settings")
                                .description("Business settings and parameters management")
                ));
    }
}
