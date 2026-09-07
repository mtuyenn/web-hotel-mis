package com.hospitality.mis.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_actor_time", columnList = "actor,created_at"),
        @Index(name = "idx_audit_logs_action_time", columnList = "action,created_at")
})
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50) private String actor;
    @Column(nullable = false, length = 100) private String action;
    @Column(name = "entity_type", nullable = false, length = 100) private String entityType;
    @Column(name = "entity_id", nullable = false, length = 100) private String entityId;
    @Lob @Column(name = "before_data", columnDefinition = "TEXT") private String beforeData;
    @Lob @Column(name = "after_data", columnDefinition = "TEXT") private String afterData;
    @Column(length = 500) private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();

    protected AuditLog() {}
    public AuditLog(String actor, String action, String entityType, String entityId, String beforeData,
                    String afterData, String reason) {
        this.actor = actor; this.action = action; this.entityType = entityType; this.entityId = entityId;
        this.beforeData = beforeData; this.afterData = afterData; this.reason = reason;
    }
}
