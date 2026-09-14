package com.hospitality.mis.entity.guest;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Lịch sử thay đổi hạng thành viên của một khách. */
@Entity @Table(name = "membership_history")
public class MembershipHistory {
    /** ID bản ghi lịch sử do cơ sở dữ liệu sinh. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "guest_id", nullable = false) private Guest guest;
    /** Hạng trước khi thay đổi. */
    @Enumerated(EnumType.STRING) @Column(name = "from_tier", nullable = false, length = 20) private MembershipTier fromTier;
    /** Hạng sau khi thay đổi. */
    @Enumerated(EnumType.STRING) @Column(name = "to_tier", nullable = false, length = 20) private MembershipTier toTier;
    @Column(nullable = false, length = 255) private String reason;
    @Column(name = "changed_at", nullable = false) private LocalDateTime changedAt;
    public Long getId() { return id; } public Guest getGuest() { return guest; } public void setGuest(Guest v) { guest = v; }
    public MembershipTier getFromTier() { return fromTier; } public void setFromTier(MembershipTier v) { fromTier = v; }
    public MembershipTier getToTier() { return toTier; } public void setToTier(MembershipTier v) { toTier = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public LocalDateTime getChangedAt() { return changedAt; } public void setChangedAt(LocalDateTime v) { changedAt = v; }
}
