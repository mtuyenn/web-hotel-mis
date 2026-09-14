package com.hospitality.mis.entity.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_debt_settlements")
public class PartnerDebtSettlement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "partner_debt_id", nullable = false)
    private PartnerDebt partnerDebt;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(name = "settled_by", nullable = false, length = 50) private String settledBy;
    @Column(name = "settled_at", nullable = false) private LocalDateTime settledAt;
    @Column(length = 500) private String note;
    public Long getId() { return id; } public PartnerDebt getPartnerDebt() { return partnerDebt; }
    public BigDecimal getAmount() { return amount; } public String getSettledBy() { return settledBy; }
    public LocalDateTime getSettledAt() { return settledAt; } public String getNote() { return note; }
    public void setPartnerDebt(PartnerDebt v) { partnerDebt = v; } public void setAmount(BigDecimal v) { amount = v; }
    public void setSettledBy(String v) { settledBy = v; } public void setSettledAt(LocalDateTime v) { settledAt = v; }
    public void setNote(String v) { note = v; }
    @PreUpdate @PreRemove void rejectMutation() { throw new IllegalStateException("Debt settlements are append-only"); }
}
