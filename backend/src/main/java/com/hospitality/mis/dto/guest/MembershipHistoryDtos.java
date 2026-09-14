package com.hospitality.mis.dto.guest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.guest.MembershipTier;
import java.time.LocalDateTime;

/** DTO lịch sử thay đổi hạng thành viên của khách. */
public final class MembershipHistoryDtos {
    /** Namespace cho response lịch sử. */
    private MembershipHistoryDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(
                           /** Khóa bản ghi lịch sử. */
                           Long id,
                           /** Khách được thay đổi hạng. */
                           Long guestId,
                           /** Hạng trước thay đổi. */
                           MembershipTier fromTier,
                           /** Hạng sau thay đổi. */
                           MembershipTier toTier,
                           /** Lý do chuyển hạng. */
                           String reason,
                           /** Thời điểm thay đổi. */
                           LocalDateTime changedAt) {}
}
