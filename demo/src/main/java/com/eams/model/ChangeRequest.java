package com.eams.model;

import com.eams.enums.ChangeStatus;
import com.eams.enums.ChangeType;
import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "change_requests")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChangeRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "change_reference", unique = true, nullable = false, length = 50)
    private String changeReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_asset_id", nullable = false)
    private Asset targetAsset;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 50)
    private ChangeType changeType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "business_justification", nullable = false)
    private String businessJustification;

    @Column(name = "risk_assessment")
    private String riskAssessment;

    @Column(name = "revert_plan", nullable = false)
    private String revertPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by_user_id", nullable = false)
    private User proposedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "implemented_by_user_id")
    private User implementedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedBy;

    @Column(name = "scheduled_start")
    private OffsetDateTime scheduledStart;

    @Column(name = "scheduled_revert_date", nullable = false)
    private LocalDate scheduledRevertDate;

    @Column(name = "actual_applied_at")
    private OffsetDateTime actualAppliedAt;

    @Column(name = "actual_reverted_at")
    private OffsetDateTime actualRevertedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ChangeStatus status = ChangeStatus.PROPOSED;

    @Column(name = "overdue_escalated_at")
    private OffsetDateTime overdueEscalatedAt;

    @Column(name = "made_permanent_reason")
    private String madePermanentReason;

    @Transient
    public long getDaysUntilRevert() {
        if (scheduledRevertDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), scheduledRevertDate);
    }

    @Transient
    public boolean isOverdue() {
        return scheduledRevertDate != null
                && LocalDate.now().isAfter(scheduledRevertDate)
                && status == ChangeStatus.PENDING_REVERT;
    }
}