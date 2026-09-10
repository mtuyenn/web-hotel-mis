package com.hospitality.mis.dto.guest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.guest.MembershipTier;
import java.time.LocalDateTime;

public final class MembershipHistoryDtos {
    private MembershipHistoryDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long guestId, MembershipTier fromTier, MembershipTier toTier,
                           String reason, LocalDateTime changedAt) {}
}
