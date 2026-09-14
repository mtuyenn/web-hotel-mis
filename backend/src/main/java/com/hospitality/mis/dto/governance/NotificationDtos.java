package com.hospitality.mis.dto.governance;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

public final class NotificationDtos {
    private NotificationDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, String topic, String recipientRole, String payload, String status,
                           String dedupeKey, LocalDateTime availableAt, LocalDateTime createdAt, LocalDateTime deliveredAt) {}
}
