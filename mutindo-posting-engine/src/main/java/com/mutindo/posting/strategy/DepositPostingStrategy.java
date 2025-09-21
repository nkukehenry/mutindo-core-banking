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
 * Deposit posting strategy implementation
 * Implements Strategy Pattern for deposit transactions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DepositPostingStrategy implements IPostingStrategy {
    
    private static final String POSTING_TYPE = "DEPOSIT";
    private static final String CASH_GL_CODE = "1010"; // Cash account
    private static final String CUSTOMER_DEPOSITS_GL_CODE = "2010"; // Customer deposits liability
    
    private final IChartOfAccountsService chartOfAccountsService;
    
    @Override
    public PostingResult executePosting(PostingRequest request) {
        log.info("Executing deposit posting for idempotency key: {}", request.getIdempotencyKey());
        
        try {
            // Validate request
            validateRequest(request);
            
            // Create posting entries for deposit
            List<PostingRequest.PostingEntry> entries = createDepositEntries(request);
            
            // Update request with calculated entries
            PostingRequest updatedRequest = request.toBuilder()
                    .entries(entries)
                    .build();
            
            log.info("Deposit posting entries created successfully");
            
            // Return success (actual posting would be done by the posting engine)
            return PostingResult.success(1L, request.getIdempotencyKey()); // Placeholder ID
            
        } catch (Exception e) {
            log.error("Failed to execute deposit posting", e);
            return PostingResult.failure("DEPOSIT_POSTING_ERROR", e.getMessage(), request.getIdempotencyKey());
        }
    }
    
    @Override
    public void validateRequest(PostingRequest request) {
        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            throw new ValidationException("Posting entries are required for deposit");
        }
        
        // For deposit, we expect one entry with debit amount (the deposit amount)
        PostingRequest.PostingEntry depositEntry = request.getEntries().get(0);
        if (depositEntry.getDebitAmount() == null || depositEntry.getDebitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Deposit amount must be greater than zero");
        }
        
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            throw new ValidationException("Currency is required for deposit");
        }
        
        if (request.getBranchId() == null) {
            throw new ValidationException("Branch ID is required for deposit");
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
    
    private List<PostingRequest.PostingEntry> createDepositEntries(PostingRequest request) {
        List<PostingRequest.PostingEntry> entries = new ArrayList<>();
        
        // Get the deposit amount from the original entry
        BigDecimal depositAmount = request.getEntries().get(0).getDebitAmount();
        
        // Debit: Cash (Asset increases)
        entries.add(PostingRequest.PostingEntry.builder()
                .glAccountCode(CASH_GL_CODE)
                .debitAmount(depositAmount)
                .creditAmount(BigDecimal.ZERO)
                .narration("Cash deposit - " + request.getNarration())
                .branchId(request.getBranchId())
                .build());
        
        // Credit: Customer Deposits (Liability increases)
        entries.add(PostingRequest.PostingEntry.builder()
                .glAccountCode(CUSTOMER_DEPOSITS_GL_CODE)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(depositAmount)
                .narration("Customer deposit liability - " + request.getNarration())
                .branchId(request.getBranchId())
                .build());
        
        return entries;
    }
}
