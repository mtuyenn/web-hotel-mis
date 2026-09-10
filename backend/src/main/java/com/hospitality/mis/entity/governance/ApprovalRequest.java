package com.hospitality.mis.entity.governance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/** A single authorization decision for one exact mutation. */
@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String EXPIRED = "EXPIRED";
    public static final String CONSUMED = "CONSUMED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String requester;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "target_id", nullable = false, length = 100)
    private String targetId;

    @Lob
    @Column(name = "mutation_payload", nullable = false, columnDefinition = "TEXT")
    private String mutationPayload;

    @Column(name = "payload_fingerprint", nullable = false, length = 64)
    private String payloadFingerprint;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status = PENDING;

    @Column(length = 50)
    private String approver;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "correlation_key", length = 100)
    private String correlationKey;

    protected ApprovalRequest() {
    }

    public ApprovalRequest(String requester, String action, String targetId, String mutationPayload,
                           String payloadFingerprint, BigDecimal amount, String reason,
                           Instant expiresAt, String correlationKey) {
        this.requester = required(requester, "requester");
        this.action = required(action, "action");
        this.targetId = required(targetId, "targetId");
        this.mutationPayload = required(mutationPayload, "mutationPayload");
        this.payloadFingerprint = required(payloadFingerprint, "payloadFingerprint");
        this.amount = amount;
        this.reason = required(reason, "reason");
        this.expiresAt = expiresAt == null ? Instant.now().plus(Duration.ofHours(24)) : expiresAt;
        this.correlationKey = blankToNull(correlationKey);
    }

    public Long getId() { return id; }
    public String getRequester() { return requester; }
    public String getAction() { return action; }
    public String getTargetId() { return targetId; }
    public String getMutationPayload() { return mutationPayload; }
    public String getPayloadFingerprint() { return payloadFingerprint; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getApprover() { return approver; }
    public Instant getDecidedAt() { return decidedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public String getCorrelationKey() { return correlationKey; }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public void approve(String approver, Instant now) {
        requirePending();
        this.status = APPROVED;
        this.approver = required(approver, "approver");
        this.decidedAt = now;
    }

    public void reject(String approver, Instant now) {
        requirePending();
        this.status = REJECTED;
        this.approver = required(approver, "approver");
        this.decidedAt = now;
    }

    public void expire(Instant now) {
        if (PENDING.equals(status)) {
            this.status = EXPIRED;
            this.decidedAt = now;
        }
    }

    public void consume(Instant now) {
        if (!APPROVED.equals(status) || consumedAt != null) {
            throw new IllegalStateException("Only an unused approved request can be consumed");
        }
        this.status = CONSUMED;
        this.consumedAt = now;
    }

    private void requirePending() {
        if (!PENDING.equals(status)) throw new IllegalStateException("Approval request is not pending");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
