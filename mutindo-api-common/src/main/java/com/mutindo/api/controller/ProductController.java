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

    // TODO: Inject IProductService when available
    // private final IProductService productService;

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
            // TODO: Use real service when available
            ProductDto product = ProductDto.builder()
                    .id(System.currentTimeMillis())
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
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("Product created successfully via API: {}", product.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success(product, "Product created successfully"));
            
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
            Long productIdLong = Long.parseLong(productId);
            Optional<ProductDto> productOpt = findProductById(productIdLong);
            
            if (productOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(productOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Product not found"));
            }
            
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
            Optional<ProductDto> productOpt = findProductByCode(productCode);
            
            if (productOpt.isPresent()) {
                return ResponseEntity.ok(BaseResponse.success(productOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("Product not found"));
            }
            
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
            List<ProductDto> products = getMockProducts();
            PaginatedResponse<ProductDto> response = PaginatedResponse.<ProductDto>builder()
                    .content(products)
                    .totalElements((long) products.size())
                    .totalPages(1)
                    .size(products.size())
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            
            log.debug("Found {} products via API", products.size());
            return ResponseEntity.ok(BaseResponse.success(response));
            
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
            List<ProductDto> products = getMockProducts();
            PaginatedResponse<ProductDto> response = PaginatedResponse.<ProductDto>builder()
                    .content(products)
                    .totalElements((long) products.size())
                    .totalPages(1)
                    .size(products.size())
                    .number(0)
                    .first(true)
                    .last(true)
                    .build();
            
            return ResponseEntity.ok(BaseResponse.success(response));
            
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

    // Private helper methods (temporary until real service is available)

    private Optional<ProductDto> findProductById(Long productId) {
        return getMockProducts().stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst();
    }

    private Optional<ProductDto> findProductByCode(String productCode) {
        return getMockProducts().stream()
                .filter(product -> product.getCode().equals(productCode))
                .findFirst();
    }

    private List<ProductDto> getMockProducts() {
        return List.of(
                ProductDto.builder()
                        .id(1L)
                        .name("Savings Account")
                        .code("SAV001")
                        .description("Standard savings account with competitive interest rates")
                        .productType("SAVINGS")
                        .currency("UGX")
                        .minimumBalance(BigDecimal.valueOf(50000))
                        .maximumBalance(BigDecimal.valueOf(50000000))
                        .interestRate(BigDecimal.valueOf(5.5))
                        .fees(BigDecimal.valueOf(5000))
                        .allowOverdraft(false)
                        .overdraftLimit(BigDecimal.ZERO)
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(12))
                        .build(),
                ProductDto.builder()
                        .id(2L)
                        .name("Current Account")
                        .code("CUR001")
                        .description("Business current account with overdraft facility")
                        .productType("CURRENT")
                        .currency("UGX")
                        .minimumBalance(BigDecimal.valueOf(100000))
                        .maximumBalance(BigDecimal.valueOf(100000000))
                        .interestRate(BigDecimal.ZERO)
                        .fees(BigDecimal.valueOf(10000))
                        .allowOverdraft(true)
                        .overdraftLimit(BigDecimal.valueOf(5000000))
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(6))
                        .build(),
                ProductDto.builder()
                        .id(3L)
                        .name("Fixed Deposit")
                        .code("FD001")
                        .description("Fixed deposit with guaranteed returns")
                        .productType("FIXED_DEPOSIT")
                        .currency("UGX")
                        .minimumBalance(BigDecimal.valueOf(1000000))
                        .maximumBalance(BigDecimal.valueOf(500000000))
                        .interestRate(BigDecimal.valueOf(8.5))
                        .fees(BigDecimal.ZERO)
                        .allowOverdraft(false)
                        .overdraftLimit(BigDecimal.ZERO)
                        .active(true)
                        .createdAt(LocalDateTime.now().minusMonths(3))
                        .build()
        );
    }

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
