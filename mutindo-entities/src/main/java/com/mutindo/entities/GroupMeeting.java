package com.mutindo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Group Meeting entity - for tracking group meetings and decisions
 */
@Entity
@Table(name = "group_meetings", indexes = {
    @Index(name = "idx_meeting_group", columnList = "groupId"),
    @Index(name = "idx_meeting_date", columnList = "meetingDate"),
    @Index(name = "idx_meeting_type", columnList = "meetingType"),
    @Index(name = "idx_meeting_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GroupMeeting extends BaseEntity {

    @NotBlank
    @Size(max = 36)
    @Column(name = "group_id", nullable = false, length = 36)
    private String groupId;

    @NotNull
    @Column(name = "meeting_date", nullable = false)
    private LocalDateTime meetingDate;

    @NotBlank
    @Size(max = 32)
    @Column(name = "meeting_type", nullable = false, length = 32)
    private String meetingType; // REGULAR, EMERGENCY, LOAN_APPROVAL, ANNUAL_GENERAL

    @Size(max = 255)
    @Column(name = "meeting_title")
    private String meetingTitle;

    @Column(name = "venue", columnDefinition = "TEXT")
    private String venue;

    @Size(max = 36)
    @Column(name = "chairperson_id", length = 36) // Customer ID
    private String chairpersonId;

    @Size(max = 36)
    @Column(name = "secretary_id", length = 36) // Customer ID
    private String secretaryId;

    @Column(name = "members_present")
    private Integer membersPresent;

    @Column(name = "members_absent")
    private Integer membersAbsent;

    @Column(name = "quorum_met", nullable = false)
    private Boolean quorumMet = false;

    @NotBlank
    @Size(max = 32)
    @Column(name = "status", nullable = false, length = 32)
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "agenda", columnDefinition = "TEXT")
    private String agenda;

    @Column(name = "minutes", columnDefinition = "TEXT")
    private String minutes;

    @Column(name = "resolutions", columnDefinition = "TEXT")
    private String resolutions;

    @Column(name = "next_meeting_date")
    private LocalDateTime nextMeetingDate;

    @Size(max = 36)
    @Column(name = "conducted_by", length = 36) // User ID of staff who facilitated
    private String conductedBy;

    // Meeting decisions and votes stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "decisions", columnDefinition = "JSON")
    private Map<String, Object> decisions;

    // Attendance tracking
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attendance", columnDefinition = "JSON")
    private Map<String, Object> attendance; // Member ID -> present/absent/late

    // Financial summary for the meeting
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "financial_summary", columnDefinition = "JSON")
    private Map<String, Object> financialSummary;
}
