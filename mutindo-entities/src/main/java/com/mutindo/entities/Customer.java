package com.mutindo.entities;

import com.mutindo.common.enums.CustomerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Customer entity - focused only on data model
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_national_id", columnList = "nationalId"),
    @Index(name = "idx_customer_phone", columnList = "phone"),
    @Index(name = "idx_customer_branch", columnList = "primaryBranchId"),
    @Index(name = "idx_customer_type", columnList = "customerType")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 16)
    private CustomerType customerType;

    @Size(max = 128)
    @Column(name = "first_name", length = 128)
    private String firstName;

    @Size(max = 128)
    @Column(name = "last_name", length = 128)
    private String lastName;

    @Size(max = 255)
    @Column(name = "legal_name")
    private String legalName; // For business customers

    @Size(max = 64)
    @Column(name = "national_id", length = 64)
    private String nationalId;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Size(max = 16)
    @Column(name = "gender", length = 16)
    private String gender;

    @Email
    @Size(max = 128)
    @Column(name = "email", length = 128)
    private String email;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @NotBlank
    @Column(name = "primary_branch_id", nullable = false)
    private Long primaryBranchId;

    @Size(max = 32)
    @Column(name = "kyc_status", length = 32)
    private String kycStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "kyc_documents", columnDefinition = "JSON")
    private Map<String, Object> kycDocuments;

    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 64)
    @Column(name = "occupation", length = 64)
    private String occupation;

    @Column(name = "monthly_income", precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // Custom fields stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", columnDefinition = "JSON")
    private Map<String, Object> customData;
}
