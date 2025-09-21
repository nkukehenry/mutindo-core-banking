package com.mutindo.posting.strategy;

import com.mutindo.chartofaccounts.service.IChartOfAccountsService;
import com.mutindo.exceptions.ValidationException;
import com.mutindo.posting.dto.PostingRequest;
import com.mutindo.posting.dto.PostingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Withdrawal posting strategy implementation
 * Implements Strategy Pattern for withdrawal transactions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalPostingStrategy implements IPostingStrategy {
    
    private static final String POSTING_TYPE = "WITHDRAWAL";
    private static final String CASH_GL_CODE = "1010"; // Cash account
    private static final String CUSTOMER_DEPOSITS_GL_CODE = "2010"; // Customer deposits liability
    
    private final IChartOfAccountsService chartOfAccountsService;
    
    @Override
    public PostingResult executePosting(PostingRequest request) {
        log.info("Executing withdrawal posting for idempotency key: {}", request.getIdempotencyKey());
        
        try {
            validateRequest(request);
            
            List<PostingRequest.PostingEntry> entries = createWithdrawalEntries(request);
            
            PostingRequest updatedRequest = request.toBuilder()
                    .entries(entries)
                    .build();
            
            log.info("Withdrawal posting entries created successfully");
            
            return PostingResult.success(1L, request.getIdempotencyKey()); // Placeholder ID
            
        } catch (Exception e) {
            log.error("Failed to execute withdrawal posting", e);
            return PostingResult.failure("WITHDRAWAL_POSTING_ERROR", e.getMessage(), request.getIdempotencyKey());
        }
    }
    
    @Override
    public void validateRequest(PostingRequest request) {
        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            throw new ValidationException("Posting entries are required for withdrawal");
        }
        
        PostingRequest.PostingEntry withdrawalEntry = request.getEntries().get(0);
        if (withdrawalEntry.getCreditAmount() == null || withdrawalEntry.getCreditAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Withdrawal amount must be greater than zero");
        }
        
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new ValidationException("Currency is required for withdrawal");
        }
        
        if (request.getBranchId() == null) {
            throw new ValidationException("Branch ID is required for withdrawal");
        }
    }
    
    @Override
    public boolean canHandle(String postingType) {
        return POSTING_TYPE.equals(postingType);
    }
    
    @Override
    public String getPostingType() {
        return POSTING_TYPE;
    }
    
    private List<PostingRequest.PostingEntry> createWithdrawalEntries(PostingRequest request) {
        List<PostingRequest.PostingEntry> entries = new ArrayList<>();
        
        BigDecimal withdrawalAmount = request.getEntries().get(0).getCreditAmount();
        
        // Debit: Customer Deposits (Liability decreases)
        entries.add(PostingRequest.PostingEntry.builder()
                .glAccountCode(CUSTOMER_DEPOSITS_GL_CODE)
                .debitAmount(withdrawalAmount)
                .creditAmount(BigDecimal.ZERO)
                .narration("Customer withdrawal - " + request.getNarration())
                .branchId(request.getBranchId())
                .build());
        
        // Credit: Cash (Asset decreases)
        entries.add(PostingRequest.PostingEntry.builder()
                .glAccountCode(CASH_GL_CODE)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(withdrawalAmount)
                .narration("Cash withdrawal - " + request.getNarration())
                .branchId(request.getBranchId())
                .build());
        
        return entries;
    }
}
