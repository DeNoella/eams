package com.eams.model;

import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "notification_rules")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "trigger_days_before")
    private Integer triggerDaysBefore;

    @Column(name = "recipient_type", nullable = false, length = 30)
    private String recipientType;

    @Column(name = "recipient_id", columnDefinition = "uuid")
    private UUID recipientId;

    @Column(name = "channels", columnDefinition = "text[]")
    private String[] channels;

    @Column(name = "subject_template", nullable = false)
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false)
    private String bodyTemplate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "cooldown_hours")
    private Integer cooldownHours = 24;
}