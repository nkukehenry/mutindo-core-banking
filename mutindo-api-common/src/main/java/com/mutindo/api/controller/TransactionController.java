package com.mutindo.api.controller;

import com.mutindo.common.context.BranchContextHolder; // Real context access
import com.mutindo.common.dto.BaseResponse; // Reusing existing response wrapper
import com.mutindo.logging.annotation.AuditLog; // Reusing existing audit logging
import com.mutindo.logging.annotation.PerformanceLog; // Reusing existing performance logging
import com.mutindo.posting.dto.PostingRequest; // Reusing existing posting DTOs
import com.mutindo.posting.dto.PostingResult; // Reusing existing posting DTOs
import com.mutindo.posting.service.IPostingEngine; // Reusing existing posting service interface
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Transaction REST API controller for testing posting engine
 * Reuses existing posting engine and response infrastructure
 */
// Temporarily disabled until ITransactionService is implemented
// @RestController
// @RequestMapping("/api/v1/transactions")
// @RequiredArgsConstructor
// @Slf4j
// @Tag(name = "Transactions", description = "Transaction processing operations")
public class TransactionController {
    // Temporarily disabled until IPostingEngine is available
    // TODO: Re-enable when posting engine service is properly implemented

}
