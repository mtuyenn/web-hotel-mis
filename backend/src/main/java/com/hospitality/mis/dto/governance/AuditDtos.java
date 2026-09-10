package com.hospitality.mis.dto.governance;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.governance.AuditLog;

import java.time.Instant;

public final class AuditDtos {
    private AuditDtos() {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(
            Long id,
            String actor,
            String action,
            String entityType,
            String entityId,
            String beforeData,
            String afterData,
            String reason,
            String correlationKey,
            Instant createdAt) {

        public static Response from(AuditLog audit) {
            return new Response(
                    audit.getId(),
                    audit.getActor(),
                    audit.getAction(),
                    audit.getEntityType(),
                    audit.getEntityId(),
                    audit.getBeforeData(),
                    audit.getAfterData(),
                    audit.getReason(),
                    audit.getCorrelationKey(),
                    audit.getCreatedAt());
        }
    }
}
