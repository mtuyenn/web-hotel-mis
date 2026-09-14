package com.hospitality.mis.dto.governance;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.governance.AuditLog;

import java.time.Instant;

/** DTO đọc nhật ký audit, dùng snake_case khi serialize ra API. */
public final class AuditDtos {
    /** Namespace cho response audit. */
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

        /** Ánh xạ thông tin truy vết từ entity audit sang response JSON. */
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
