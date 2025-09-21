package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit Log entity - immutable audit trail
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_audit_user", columnList = "performedBy"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "performedAt"),
    @Index(name = "idx_audit_branch", columnList = "branchId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank
    @Size(max = 32)
    @Column(name = "entity_type", nullable = false, length = 32)
    private String entityType;

    @NotBlank
    @Size(max = 36)
    @Column(name = "entity_id", nullable = false, length = 36)
    private String entityId;

    @NotBlank
    @Size(max = 32)
    @Column(name = "action", nullable = false, length = 32)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.

    @Size(max = 36)
    @Column(name = "performed_by", length = 36)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Size(max = 36)
    @Column(name = "branch_id", length = 36)
    private String branchId;

    @Size(max = 64)
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Size(max = 255)
    @Column(name = "user_agent")
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "diff", columnDefinition = "JSON")
    private Map<String, Object> diff; // Changes made

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Map<String, Object> metadata; // Additional context

    @Size(max = 64)
    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
        }
    }
}
