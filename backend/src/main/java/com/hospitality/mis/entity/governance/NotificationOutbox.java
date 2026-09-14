package com.hospitality.mis.entity.governance;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {
    public enum Status { PENDING, DELIVERED, FAILED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String topic;
    @Column(name = "recipient_role", nullable = false, length = 30) private String recipientRole;
    @Lob @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status = Status.PENDING;
    @Column(name = "dedupe_key", nullable = false, unique = true, length = 150) private String dedupeKey;
    @Column(name = "available_at", nullable = false) private LocalDateTime availableAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
    public Long getId() { return id; } public String getTopic() { return topic; } public String getRecipientRole() { return recipientRole; }
    public String getPayload() { return payload; } public Status getStatus() { return status; } public String getDedupeKey() { return dedupeKey; }
    public LocalDateTime getAvailableAt() { return availableAt; } public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setTopic(String v) { topic = v; } public void setRecipientRole(String v) { recipientRole = v; } public void setPayload(String v) { payload = v; }
    public void setStatus(Status v) { status = v; } public void setDedupeKey(String v) { dedupeKey = v; } public void setAvailableAt(LocalDateTime v) { availableAt = v; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; } public void setDeliveredAt(LocalDateTime v) { deliveredAt = v; }
}
