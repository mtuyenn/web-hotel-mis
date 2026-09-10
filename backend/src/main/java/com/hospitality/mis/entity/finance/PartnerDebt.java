package com.hospitality.mis.entity.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "partner_debts")
public class PartnerDebt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "partner_name", nullable = false, length = 150) private String partnerName;
    @Column(name = "reference_code", nullable = false, unique = true, length = 80) private String referenceCode;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal settledAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DebtStatus status = DebtStatus.OPEN;
    @Column(name = "recorded_at", nullable = false) private LocalDateTime recordedAt;
    public enum DebtStatus { OPEN, PARTIALLY_SETTLED, SETTLED, VOIDED }
    public Long getId() { return id; } public String getPartnerName() { return partnerName; } public void setPartnerName(String v) { partnerName = v; }
    public String getReferenceCode() { return referenceCode; } public void setReferenceCode(String v) { referenceCode = v; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount = v; }
    public BigDecimal getSettledAmount() { return settledAmount; } public void setSettledAmount(BigDecimal v) { settledAmount = v; }
    public DebtStatus getStatus() { return status; } public void setStatus(DebtStatus v) { status = v; }
    public LocalDateTime getRecordedAt() { return recordedAt; } public void setRecordedAt(LocalDateTime v) { recordedAt = v; }
}
