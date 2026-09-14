package com.hospitality.mis.entity.governance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

/** Durable command result used to make retryable mutations safe across restarts. */
@Entity
@Table(name = "idempotency_records", uniqueConstraints =
        @UniqueConstraint(name = "uk_idempotency_scope_key", columnNames = {"command_scope", "idempotency_key"}))
public class IdempotencyRecord {
    public enum Status { PROCESSING, COMPLETED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_scope", nullable = false, length = 100)
    private String scope;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String key;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "response_type", length = 255)
    private String responseType;

    @Column(name = "response_json", columnDefinition = "LONGTEXT")
    private String responseJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected IdempotencyRecord() {}

    public IdempotencyRecord(String scope, String key, String actor, String requestHash, LocalDateTime createdAt) {
        this.scope = scope;
        this.key = key;
        this.actor = actor;
        this.requestHash = requestHash;
        this.createdAt = createdAt;
        this.status = Status.PROCESSING;
    }

    public void complete(String responseType, String responseJson, LocalDateTime completedAt) {
        this.responseType = responseType;
        this.responseJson = responseJson;
        this.completedAt = completedAt;
        this.status = Status.COMPLETED;
    }

    public String getActor() { return actor; }
    public String getRequestHash() { return requestHash; }
    public Status getStatus() { return status; }
    public String getResponseType() { return responseType; }
    public String getResponseJson() { return responseJson; }
}
