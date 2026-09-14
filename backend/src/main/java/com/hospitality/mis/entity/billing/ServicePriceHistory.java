package com.hospitality.mis.entity.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_price_history")
public class ServicePriceHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_id", nullable = false) private Service service;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(name = "changed_by", nullable = false, length = 50) private String changedBy;
    @Column(name = "approval_id") private Long approvalId;
    @Column(name = "effective_at", nullable = false) private LocalDateTime effectiveAt;
    protected ServicePriceHistory() {}
    public ServicePriceHistory(Service service, BigDecimal price, String changedBy, Long approvalId, LocalDateTime effectiveAt) {
        this.service = service; this.price = price; this.changedBy = changedBy; this.approvalId = approvalId; this.effectiveAt = effectiveAt;
    }
    public Long getId() { return id; } public Service getService() { return service; } public BigDecimal getPrice() { return price; }
    public String getChangedBy() { return changedBy; } public Long getApprovalId() { return approvalId; } public LocalDateTime getEffectiveAt() { return effectiveAt; }
}
