package com.hospitality.mis.entity.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id", nullable = false) private Invoice invoice;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private PaymentMethod method;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TransactionType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TransactionStatus status = TransactionStatus.COMPLETED;
    @Column(length = 100) private String reference;
    @Column(name = "occurred_at", nullable = false) private LocalDateTime occurredAt;
    @Column(name = "actor_id", nullable = false, length = 50) private String actorId;
    @Column(name = "idempotency_key", length = 100, unique = true) private String idempotencyKey;
    public enum TransactionType { PAYMENT, REFUND }
    public enum TransactionStatus { COMPLETED, FAILED, VOIDED }
    public Long getId() { return id; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice value) { invoice = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod value) { method = value; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType value) { type = value; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus value) { status = value; }
    public String getReference() { return reference; }
    public void setReference(String value) { reference = value; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime value) { occurredAt = value; }
    public String getActorId() { return actorId; }
    public void setActorId(String value) { actorId = value; }
    public void setIdempotencyKey(String value) { idempotencyKey = value; }

    /** Stores the request binding in the existing idempotency column. */
    public static String storageIdempotencyKey(String key, String actor, BigDecimal amount,
                                               PaymentMethod method, TransactionType type, String reference) {
        if (key == null || key.isBlank()) return null;
        String canonical = key.trim() + "|" + actor + "|" + amount.toPlainString() + "|"
                + method.name() + "|" + type.name() + "|" + (reference == null ? "" : reference);
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            String stored = key.trim() + "." + hex;
            if (stored.length() > 100) throw new IllegalArgumentException("idempotency key is too long");
            return stored;
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    /** Returns the client key while retaining the bound digest in storage. */
    public String getIdempotencyKey() {
        if (idempotencyKey == null) return null;
        int separator = idempotencyKey.lastIndexOf('.');
        return separator > 0 && idempotencyKey.length() - separator - 1 == 64
                ? idempotencyKey.substring(0, separator) : idempotencyKey;
    }

    public String getStoredIdempotencyKey() { return idempotencyKey; }

    public String getSourceTransactionId() {
        if (reference == null || !reference.startsWith("REFUND_OF:")) return null;
        int end = reference.indexOf(':', "REFUND_OF:".length());
        return end < 0 ? reference.substring("REFUND_OF:".length())
                : reference.substring("REFUND_OF:".length(), end);
    }

    /** Ledger rows are append-only. Corrections are represented by a new refund row. */
    @PreUpdate
    void rejectUpdate() {
        throw new IllegalStateException("Payment transactions are immutable");
    }

    @PreRemove
    void rejectDelete() {
        throw new IllegalStateException("Payment transactions are immutable");
    }
}
