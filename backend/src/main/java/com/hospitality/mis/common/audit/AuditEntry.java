package com.hospitality.mis.common.audit;

import com.hospitality.mis.common.actor.ActorId;
import java.time.Instant;
import java.util.Objects;

/** Canonical, transport-independent description of an auditable operation. */
public record AuditEntry(ActorId actor, String action, String entityType, String entityId,
                         String beforeData, String afterData, String reason, Instant occurredAt) {
    public AuditEntry {
        actor = Objects.requireNonNull(actor, "actor must not be null");
        action = required(action, "action");
        entityType = required(entityType, "entity type");
        entityId = required(entityId, "entity id");
        occurredAt = Objects.requireNonNull(occurredAt, "occurred at must not be null");
    }

    public static AuditEntry now(ActorId actor, String action, String entityType, String entityId,
                                 String beforeData, String afterData, String reason) {
        return new AuditEntry(actor, action, entityType, entityId, beforeData, afterData, reason, Instant.now());
    }

    private static String required(String value, String field) {
        value = Objects.requireNonNull(value, field + " must not be null").trim();
        if (value.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
}
