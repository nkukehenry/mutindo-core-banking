package com.mutindo.posting.service;

import com.mutindo.entities.JournalEntry;
import com.mutindo.entities.JournalEntryLine;
import com.mutindo.exceptions.BusinessException;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import com.mutindo.posting.dto.PostingRequest;
import com.mutindo.posting.dto.PostingResult;
import com.mutindo.posting.dto.ReversalRequest;
import com.mutindo.posting.factory.IPostingStrategyFactory;
import com.mutindo.posting.strategy.IPostingStrategy;
import com.mutindo.repositories.JournalEntryLineRepository;
import com.mutindo.repositories.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Posting Engine implementation with Strategy and Factory patterns
 * Handles double-entry bookkeeping with polymorphic posting strategies
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostingEngine implements IPostingEngine {
    
    private final IPostingStrategyFactory strategyFactory;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    
    @Override
    @Transactional
    @AuditLog
    @PerformanceLog
    public PostingResult postTransaction(PostingRequest request) {
        log.info("Processing posting request: {}", request.getIdempotencyKey());
        
        try {
            // Check idempotency
            if (isAlreadyProcessed(request.getIdempotencyKey())) {
                log.info("Transaction already processed: {}", request.getIdempotencyKey());
                return PostingResult.success(0L, request.getIdempotencyKey()); // Already processed
            }
            
            // Validate request
            validatePostingRequest(request);
            
            // Get appropriate strategy
            IPostingStrategy strategy = strategyFactory.getPostingStrategy(request.getPostingType());
            
            // Execute strategy to get posting entries
            PostingResult strategyResult = strategy.executePosting(request);
            if (!strategyResult.isSuccess()) {
                return strategyResult;
            }
            
            // Create journal entry
            JournalEntry journalEntry = createJournalEntry(request);
            JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
            
            // Create journal entry lines
            List<JournalEntryLine> lines = createJournalEntryLines(savedEntry.getId(), request);
            journalEntryLineRepository.saveAll(lines);
            
            // Validate double-entry balance
            validateDoubleEntryBalance(lines);
            
            log.info("Transaction posted successfully: {}", request.getIdempotencyKey());
            
            return PostingResult.success(savedEntry.getId(), request.getIdempotencyKey());
            
        } catch (Exception e) {
            log.error("Failed to post transaction: {}", request.getIdempotencyKey(), e);
            return PostingResult.failure("POSTING_ERROR", e.getMessage(), request.getIdempotencyKey());
        }
    }
    
    @Override
    @Async
    public CompletableFuture<PostingResult> postTransactionAsync(PostingRequest request) {
        log.info("Processing posting request asynchronously: {}", request.getIdempotencyKey());
        
        try {
            PostingResult result = postTransaction(request);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Async posting failed: {}", request.getIdempotencyKey(), e);
            PostingResult failureResult = PostingResult.failure(
                "ASYNC_POSTING_ERROR", 
                e.getMessage(), 
                request.getIdempotencyKey()
            );
            return CompletableFuture.completedFuture(failureResult);
        }
    }
    
    @Override
    @Transactional
    @AuditLog
    public PostingResult reverseTransaction(ReversalRequest request) {
        log.info("Reversing transaction: {}", request.getOriginalJournalEntryId());
        
        try {
            // Find original journal entry
            JournalEntry originalEntry = journalEntryRepository.findById(request.getOriginalJournalEntryId())
                    .orElseThrow(() -> new BusinessException("Original journal entry not found", "JOURNAL_ENTRY_NOT_FOUND"));
            
            // Check if already reversed
            if (originalEntry.getReversed()) {
                throw new BusinessException("Transaction already reversed", "ALREADY_REVERSED");
            }
            
            // Create reversal entry
            JournalEntry reversalEntry = createReversalEntry(originalEntry, request);
            JournalEntry savedReversalEntry = journalEntryRepository.save(reversalEntry);
            
            // Create reversal lines (flip debits and credits)
            List<JournalEntryLine> originalLines = journalEntryLineRepository.findByJournalEntryIdOrderByCreatedAt(originalEntry.getId());
            List<JournalEntryLine> reversalLines = createReversalLines(savedReversalEntry.getId(), originalLines, request);
            journalEntryLineRepository.saveAll(reversalLines);
            
            // Mark original entry as reversed
            originalEntry.setReversed(true);
            originalEntry.setReversalEntryId(savedReversalEntry.getId());
            journalEntryRepository.save(originalEntry);
            
            log.info("Transaction reversed successfully: {}", request.getOriginalJournalEntryId());
            
            return PostingResult.success(savedReversalEntry.getId(), request.getIdempotencyKey());
            
        } catch (Exception e) {
            log.error("Failed to reverse transaction: {}", request.getOriginalJournalEntryId(), e);
            return PostingResult.failure("REVERSAL_ERROR", e.getMessage(), request.getIdempotencyKey());
        }
    }
    
    @Override
    public void validatePostingRequest(PostingRequest request) {
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().trim().isEmpty()) {
            throw new ValidationException("Idempotency key is required");
        }
        
        if (request.getPostingType() == null || request.getPostingType().trim().isEmpty()) {
            throw new ValidationException("Posting type is required");
        }
        
        if (!strategyFactory.hasStrategy(request.getPostingType())) {
            throw new ValidationException("Unsupported posting type: " + request.getPostingType());
        }
        
        if (request.getPostingDate() == null) {
            throw new ValidationException("Posting date is required");
        }
        
        if (request.getBranchId() == null) {
            throw new ValidationException("Branch ID is required");
        }
    }
    
    @Override
    @Cacheable(value = "processedTransactions", key = "#idempotencyKey")
    public boolean isAlreadyProcessed(String idempotencyKey) {
        return journalEntryRepository.existsByIdempotencyKey(idempotencyKey);
    }
    
    // Private helper methods
    
    private JournalEntry createJournalEntry(PostingRequest request) {
        JournalEntry entry = new JournalEntry();
        entry.setPostingDate(request.getPostingDate());
        entry.setSourceType(request.getSourceType());
        entry.setSourceId(request.getSourceId());
        entry.setNarration(request.getNarration());
        entry.setIdempotencyKey(request.getIdempotencyKey());
        entry.setBranchId(request.getBranchId());
        entry.setReversed(false);
        entry.setCreatedBy(request.getUserId().toString());
        return entry;
    }
    
    private List<JournalEntryLine> createJournalEntryLines(Long journalEntryId, PostingRequest request) {
        List<JournalEntryLine> lines = new ArrayList<>();
        
        for (PostingRequest.PostingEntry entry : request.getEntries()) {
            JournalEntryLine line = new JournalEntryLine();
            line.setJournalEntryId(journalEntryId);
            line.setGlAccountCode(entry.getGlAccountCode());
            line.setDebit(entry.getDebitAmount() != null ? entry.getDebitAmount() : BigDecimal.ZERO);
            line.setCredit(entry.getCreditAmount() != null ? entry.getCreditAmount() : BigDecimal.ZERO);
            line.setNarration(entry.getNarration());
            line.setBranchId(entry.getBranchId());
            line.setCurrency(request.getCurrency());
            line.setCreatedBy(request.getUserId().toString());
            
            lines.add(line);
        }
        
        return lines;
    }
    
    private void validateDoubleEntryBalance(List<JournalEntryLine> lines) {
        BigDecimal totalDebits = lines.stream()
                .map(JournalEntryLine::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = lines.stream()
                .map(JournalEntryLine::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new BusinessException(
                String.format("Double-entry validation failed: Debits=%s, Credits=%s", totalDebits, totalCredits),
                "DOUBLE_ENTRY_IMBALANCE"
            );
        }
    }
    
    private JournalEntry createReversalEntry(JournalEntry originalEntry, ReversalRequest request) {
        JournalEntry reversalEntry = new JournalEntry();
        reversalEntry.setPostingDate(request.getReversalDate());
        reversalEntry.setSourceType("REVERSAL");
        reversalEntry.setSourceId(originalEntry.getId());
        reversalEntry.setNarration("Reversal: " + request.getReversalReason());
        reversalEntry.setIdempotencyKey(request.getIdempotencyKey());
        reversalEntry.setBranchId(originalEntry.getBranchId());
        reversalEntry.setReversed(false);
        reversalEntry.setCreatedBy(request.getUserId().toString());
        return reversalEntry;
    }
    
    private List<JournalEntryLine> createReversalLines(Long reversalEntryId, List<JournalEntryLine> originalLines, ReversalRequest request) {
        List<JournalEntryLine> reversalLines = new ArrayList<>();
        
        for (JournalEntryLine originalLine : originalLines) {
            JournalEntryLine reversalLine = new JournalEntryLine();
            reversalLine.setJournalEntryId(reversalEntryId);
            reversalLine.setGlAccountCode(originalLine.getGlAccountCode());
            // Flip debits and credits for reversal
            reversalLine.setDebit(originalLine.getCredit());
            reversalLine.setCredit(originalLine.getDebit());
            reversalLine.setNarration("Reversal: " + originalLine.getNarration());
            reversalLine.setBranchId(originalLine.getBranchId());
            reversalLine.setCurrency(originalLine.getCurrency());
            reversalLine.setCreatedBy(request.getUserId().toString());
            
            reversalLines.add(reversalLine);
        }
        
        return reversalLines;
    }
}
