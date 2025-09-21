package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Biometric entity - encrypted biometric templates
 */
@Entity
@Table(name = "biometrics", indexes = {
    @Index(name = "idx_biometric_customer", columnList = "customerId"),
    @Index(name = "idx_biometric_format", columnList = "format"),
    @Index(name = "idx_biometric_device", columnList = "captureDevice")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Biometric extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Lob
    @Column(name = "template_encrypted", nullable = false)
    private byte[] templateEncrypted; // Encrypted biometric template

    @NotBlank
    @Size(max = 32)
    @Column(name = "format", nullable = false, length = 32)
    private String format; // ISO19794, ANSI, etc.

    @Size(max = 64)
    @Column(name = "capture_device", length = 64)
    private String captureDevice;

    @Size(max = 36)
    @Column(name = "captured_by", length = 36)
    private String capturedBy;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Size(max = 128)
    @Column(name = "consent_ref", length = 128)
    private String consentRef; // Reference to consent record

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 32)
    @Column(name = "biometric_type", length = 32)
    private String biometricType; // FINGERPRINT, FACE, IRIS, etc.

    @Column(name = "quality_score")
    private Integer qualityScore; // 0-100

    @PrePersist
    protected void onCreate() {
        if (capturedAt == null) {
            capturedAt = LocalDateTime.now();
        }
    }
}
