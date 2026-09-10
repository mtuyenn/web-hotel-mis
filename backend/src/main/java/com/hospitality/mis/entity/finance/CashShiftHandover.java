package com.hospitality.mis.entity.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "cash_shift_handovers")
public class CashShiftHandover {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "shift_code", nullable = false, length = 30) private String shiftCode;
    @Column(name = "from_actor", nullable = false, length = 50) private String fromActor;
    @Column(name = "to_actor", nullable = false, length = 50) private String toActor;
    @Column(name = "expected_amount", nullable = false, precision = 14, scale = 2) private BigDecimal expectedAmount;
    @Column(name = "actual_amount", nullable = false, precision = 14, scale = 2) private BigDecimal actualAmount;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal variance;
    @Column(name = "handed_over_at", nullable = false) private LocalDateTime handedOverAt;
    @Column(length = 500) private String note;
    public Long getId() { return id; }
    public String getShiftCode() { return shiftCode; } public void setShiftCode(String v) { shiftCode = v; }
    public String getFromActor() { return fromActor; } public void setFromActor(String v) { fromActor = v; }
    public String getToActor() { return toActor; } public void setToActor(String v) { toActor = v; }
    public BigDecimal getExpectedAmount() { return expectedAmount; } public void setExpectedAmount(BigDecimal v) { expectedAmount = v; }
    public BigDecimal getActualAmount() { return actualAmount; } public void setActualAmount(BigDecimal v) { actualAmount = v; }
    public BigDecimal getVariance() { return variance; } public void setVariance(BigDecimal v) { variance = v; }
    public LocalDateTime getHandedOverAt() { return handedOverAt; } public void setHandedOverAt(LocalDateTime v) { handedOverAt = v; }
    public String getNote() { return note; } public void setNote(String v) { note = v; }
}
