package com.hospitality.mis.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 50) private String requester;
    @Column(nullable = false, length = 50) private String action;
    @Column(name = "target_id", nullable = false, length = 100) private String targetId;
    @Column(nullable = false, length = 500) private String reason;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    @Column(length = 50) private String approver;
    @Column(name = "decided_at") private Instant decidedAt;
    protected ApprovalRequest() {}
    public ApprovalRequest(String requester, String action, String targetId, String reason) {
        this.requester = requester; this.action = action; this.targetId = targetId; this.reason = reason;
    }
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public String getAction() { return action; }
    public void approve(String approver) { this.status = "APPROVED"; this.approver = approver; this.decidedAt = Instant.now(); }
}
