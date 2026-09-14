package com.hospitality.mis.entity.room;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Append-only giá đã được active trong catalog. */
@Entity
@Table(name = "room_type_price_history")
public class RoomTypePriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_room_type_price_history_type"))
    private RoomType roomType;

    @Column(name = "daily_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyPrice;

    @Column(name = "changed_by", nullable = false, length = 50)
    private String changedBy;

    @Column(name = "approval_id")
    private Long approvalId;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    protected RoomTypePriceHistory() {}

    public RoomTypePriceHistory(RoomType roomType, BigDecimal dailyPrice, String changedBy,
                                Long approvalId, LocalDateTime effectiveAt) {
        this.roomType = roomType;
        this.dailyPrice = dailyPrice;
        this.changedBy = changedBy;
        this.approvalId = approvalId;
        this.effectiveAt = effectiveAt;
    }

    public Long getId() { return id; }
    public RoomType getRoomType() { return roomType; }
    public BigDecimal getDailyPrice() { return dailyPrice; }
    public String getChangedBy() { return changedBy; }
    public Long getApprovalId() { return approvalId; }
    public LocalDateTime getEffectiveAt() { return effectiveAt; }
}
