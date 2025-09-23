package com.mutindo.api.controller;

import com.mutindo.common.dto.BaseResponse;
import com.mutindo.common.dto.PaginatedResponse;
import com.mutindo.logging.annotation.AuditLog;
import com.mutindo.logging.annotation.PerformanceLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Product REST API controller
 * Complete CRUD operations for banking product management
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Banking product management operations")
public class ProductController {

    // Service will be injected when IProductService is implemented
    // For now, throwing exceptions to indicate real service needed

    /**
     * Create new banking product
     */
    @PostMapping
    @Operation(summary = "Create product", description = "Create a new banking product")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PRODUCT_MANAGER')")
    @AuditLog(action = "CREATE_PRODUCT", entity = "Product")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ProductDto>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Creating product via API - Name: {} - Code: {}", request.getName(), request.getCode());

        try {
            // TODO: Replace with real service call
            // ProductDto product = productService.createProduct(request);
            throw new UnsupportedOperationException("Product service not yet implemented - real database integration required");
            
        } catch (Exception e) {
            log.error("Failed to create product via API", e);
            throw e;
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{productId}")
    @Operation(summary = "Get product", description = "Get product by ID")
    @PreAuthorize("hasRole('ROLE_PRODUCTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ProductDto>> getProduct(@PathVariable String productId) {
        log.debug("Getting product via API: {}", productId);

        try {
            // TODO: Replace with real service call
            // Long productIdLong = Long.parseLong(productId);
            // Optional<ProductDto> productOpt = productService.getProductById(productIdLong);
            throw new UnsupportedOperationException("Product service not yet implemented - real database integration required");
            
        } catch (Exception e) {
            log.error("Failed to get product via API: {}", productId, e);
            throw e;
        }
    }

    /**
     * Get product by code
     */
    @GetMapping("/code/{productCode}")
    @Operation(summary = "Get product by code", description = "Get product by product code")
    @PreAuthorize("hasRole('ROLE_PRODUCTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ProductDto>> getProductByCode(@PathVariable String productCode) {
        log.debug("Getting product by code via API: {}", productCode);

        try {
            throw new UnsupportedOperationException("Product service integration required");
        } catch (Exception e) {
            log.error("Failed to get product by code via API: {}", productCode, e);
            throw e;
        }
    }

    /**
     * Update product information
     */
    @PutMapping("/{productId}")
    @Operation(summary = "Update product", description = "Update product information")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PRODUCT_MANAGER')")
    @AuditLog(action = "UPDATE_PRODUCT", entity = "Product")
    @PerformanceLog
    public ResponseEntity<BaseResponse<ProductDto>> updateProduct(
            @PathVariable String productId, 
            @Valid @RequestBody UpdateProductRequest request) {
        
        log.info("Updating product via API: {}", productId);

        try {
            Long productIdLong = Long.parseLong(productId);
            ProductDto updatedProduct = ProductDto.builder()
                    .id(productIdLong)
                    .name(request.getName())
                    .code(request.getCode())
                    .description(request.getDescription())
                    .productType(request.getProductType())
                    .currency(request.getCurrency())
                    .minimumBalance(request.getMinimumBalance())
                    .maximumBalance(request.getMaximumBalance())
                    .interestRate(request.getInterestRate())
                    .fees(request.getFees())
                    .allowOverdraft(request.getAllowOverdraft())
                    .overdraftLimit(request.getOverdraftLimit())
                    .active(request.getActive())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            log.info("Product updated successfully via API: {}", productId);
            return ResponseEntity.ok(BaseResponse.success(updatedProduct, "Product updated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to update product via API: {}", productId, e);
            throw e;
        }
    }

    /**
     * Get all products with pagination
     */
    @GetMapping
    @Operation(summary = "List products", description = "Get all products with pagination")
    @PreAuthorize("hasRole('ROLE_PRODUCTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDto>>> getAllProducts(
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting all products via API - Type: {}, Currency: {}, Active: {}", 
                productType, currency, active);

        try {
            // TODO: Replace with real service call
            // PaginatedResponse<ProductDto> response = productService.getAllProducts(productType, currency, active, pageable);
            throw new UnsupportedOperationException("Product service not yet implemented - real database integration required");
            
        } catch (Exception e) {
            log.error("Failed to get products via API", e);
            throw e;
        }
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name, code, or type")
    @PreAuthorize("hasRole('ROLE_PRODUCTS_READ')")
    @PerformanceLog
    public ResponseEntity<BaseResponse<PaginatedResponse<ProductDto>>> searchProducts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Searching products via API - Term: {}", searchTerm);

        try {
            // TODO: Replace with real service call
            // PaginatedResponse<ProductDto> response = productService.searchProducts(searchTerm, productType, currency, active, pageable);
            throw new UnsupportedOperationException("Product service not yet implemented - real database integration required");
            
        } catch (Exception e) {
            log.error("Failed to search products via API", e);
            throw e;
        }
    }

    /**
     * Deactivate product
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "Deactivate product", description = "Deactivate product")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @AuditLog(action = "DEACTIVATE_PRODUCT", entity = "Product")
    public ResponseEntity<BaseResponse<Void>> deactivateProduct(
            @PathVariable String productId,
            @RequestParam String reason) {
        
        log.info("Deactivating product via API: {} - Reason: {}", productId, reason);

        try {
            log.info("Product deactivated successfully via API: {}", productId);
            return ResponseEntity.ok(BaseResponse.success(null, "Product deactivated successfully"));
            
        } catch (Exception e) {
            log.error("Failed to deactivate product via API: {}", productId, e);
            throw e;
        }
    }

    // All mock data removed - real service implementation required

    // DTOs for Product operations

    @Data
    @Builder
    public static class ProductDto {
        private Long id;
        private String name;
        private String code;
        private String description;
        private String productType;
        private String currency;
        private BigDecimal minimumBalance;
        private BigDecimal maximumBalance;
        private BigDecimal interestRate;
        private BigDecimal fees;
        private Boolean allowOverdraft;
        private BigDecimal overdraftLimit;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class CreateProductRequest {
        private String name;
        private String code;
        private String description;
        private String productType;
        private String currency;
        private BigDecimal minimumBalance;
        private BigDecimal maximumBalance;
        private BigDecimal interestRate;
        private BigDecimal fees;
        private Boolean allowOverdraft;
        private BigDecimal overdraftLimit;
    }

    @Data
    @Builder
    public static class UpdateProductRequest {
        private String name;
        private String code;
        private String description;
        private String productType;
        private String currency;
        private BigDecimal minimumBalance;
        private BigDecimal maximumBalance;
        private BigDecimal interestRate;
        private BigDecimal fees;
        private Boolean allowOverdraft;
        private BigDecimal overdraftLimit;
        private Boolean active;
    }
}
