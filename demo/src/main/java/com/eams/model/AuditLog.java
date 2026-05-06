package com.eams.model;

import com.eams.enums.AuditActionType;
import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;

import java.time.OffsetDateTime;
import java.util.UUID;

// NOTE: No @Where here — audit logs are NEVER soft deleted
@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "event_timestamp", nullable = false)
    private OffsetDateTime eventTimestamp;

    @Column(name = "actor_user_id", columnDefinition = "uuid")
    private UUID actorUserId;

    @Column(name = "actor_display_name", nullable = false, length = 255)
    private String actorDisplayName;

    // PostgreSQL 'inet' column — use a write-side cast so Hibernate's VARCHAR
    // bind parameter is converted to the native type. Reading back as text is
    // already supported by the JDBC driver.
    @Column(name = "actor_ip_address", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String actorIpAddress;

    @Column(name = "actor_user_agent")
    private String actorUserAgent;

    @Column(name = "session_id", columnDefinition = "uuid")
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AuditActionType actionType;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", columnDefinition = "uuid")
    private UUID resourceId;

    @Column(name = "resource_display", length = 255)
    private String resourceDisplay;

    @Column(name = "field_name", length = 150)
    private String fieldName;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "signature", nullable = false)
    private String signature;

    @Column(name = "is_sensitive")
    private Boolean isSensitive = false;
}