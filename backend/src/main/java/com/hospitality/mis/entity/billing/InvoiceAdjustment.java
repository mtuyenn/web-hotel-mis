package com.hospitality.mis.entity.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Append-only signed correction to an invoice charge total. */
@Entity
@Table(name = "invoice_adjustments")
public class InvoiceAdjustment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false) private Invoice invoice;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal delta;
    @Column(nullable = false, length = 500) private String reason;
    @Column(name = "actor_id", nullable = false, length = 50) private String actorId;
    @Column(name = "occurred_at", nullable = false) private LocalDateTime occurredAt;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100) private String idempotencyKey;

    public Long getId() { return id; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice value) { invoice = value; }
    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal value) { delta = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public String getActorId() { return actorId; }
    public void setActorId(String value) { actorId = value; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime value) { occurredAt = value; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String value) { idempotencyKey = value; }

    @PreUpdate @PreRemove
    void rejectMutation() { throw new IllegalStateException("Invoice adjustments are immutable"); }
}
