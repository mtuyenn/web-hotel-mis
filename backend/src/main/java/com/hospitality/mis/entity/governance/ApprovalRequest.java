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

/** Một quyết định phê duyệt duy nhất cho một thay đổi cụ thể. */
@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {
    /** Tập hằng trạng thái điều khiển vòng đời một yêu cầu phê duyệt. */
    public static final String PENDING = "PENDING";
    /** Đã được chấp thuận nhưng chưa tiêu thụ cho thao tác đích. */
    public static final String APPROVED = "APPROVED";
    /** Đã bị từ chối. */
    public static final String REJECTED = "REJECTED";
    /** Đã hết thời hạn mà chưa có quyết định. */
    public static final String EXPIRED = "EXPIRED";
    /** Đã dùng cho đúng một thay đổi nghiệp vụ. */
    public static final String CONSUMED = "CONSUMED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    /** Chủ thể yêu cầu phê duyệt. */
    private String requester;

    @Column(nullable = false, length = 50)
    /** Tên hành động nhạy cảm cần phê duyệt. */
    private String action;

    @Column(name = "target_id", nullable = false, length = 100)
    /** ID đối tượng mà hành động sẽ thay đổi. */
    private String targetId;

    @Lob
    @Column(name = "mutation_payload", nullable = false, columnDefinition = "TEXT")
    /** Nội dung thay đổi đã yêu cầu, được thực thi sau phê duyệt. */
    private String mutationPayload;

    @Column(name = "payload_fingerprint", nullable = false, length = 64)
    /** Dấu vân tay payload để phê duyệt không bị dùng cho payload khác. */
    private String payloadFingerprint;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, length = 20)
    /** Trạng thái vòng đời; chỉ các phương thức chuyển trạng thái được phép thay đổi. */
    private String status = PENDING;

    @Column(nullable = false, length = 20)
    private String risk = "LOW";

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(length = 50)
    private String approver;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "expires_at", nullable = false)
    /** Thời điểm hết hạn; quá thời điểm này yêu cầu không còn được dùng. */
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "correlation_key", length = 100)
    /** Khóa liên kết yêu cầu với chuỗi thao tác hoặc audit bên ngoài. */
    private String correlationKey;

    /** Constructor rỗng dành cho JPA. */
    protected ApprovalRequest() {
    }

    /** Tạo yêu cầu chờ duyệt và chuẩn hóa giá trị tùy chọn. */
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
        this.requestedAt = Instant.now();
        this.correlationKey = blankToNull(correlationKey);
    }

    public ApprovalRequest(String requester, String action, String targetId, String mutationPayload,
                           String payloadFingerprint, BigDecimal amount, String reason,
                           Instant expiresAt, String correlationKey, Instant now) {
        this(requester, action, targetId, mutationPayload, payloadFingerprint, amount, reason,
                expiresAt == null ? now.plus(Duration.ofHours(24)) : expiresAt, correlationKey);
        this.requestedAt = now;
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
    public String getRisk() { return risk; }
    public Instant getRequestedAt() { return requestedAt; }
    public String getApprover() { return approver; }
    public Instant getDecidedAt() { return decidedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public String getCorrelationKey() { return correlationKey; }
    public void setRisk(String risk) { this.risk = required(risk, "risk"); }

    public boolean isExpired(Instant now) {
        // Hết hạn tại đúng thời điểm expiresAt, không chỉ sau thời điểm đó.
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    /** Chuyển PENDING sang APPROVED; yêu cầu đã quyết định không thể quyết định lại. */
    public void approve(String approver, Instant now) {
        requirePending();
        this.status = APPROVED;
        this.approver = required(approver, "approver");
        this.decidedAt = now;
    }

    /** Chuyển PENDING sang REJECTED và ghi người, thời điểm quyết định. */
    public void reject(String approver, Instant now) {
        requirePending();
        this.status = REJECTED;
        this.approver = required(approver, "approver");
        this.decidedAt = now;
    }

    /** Đánh dấu hết hạn nếu yêu cầu vẫn còn chờ, thao tác này có tính lũy đẳng. */
    public void expire(Instant now) {
        if (PENDING.equals(status)) {
            this.status = EXPIRED;
            this.decidedAt = now;
        }
    }

    /** Tiêu thụ một phê duyệt APPROVED đúng một lần cho thay đổi đã kiểm tra. */
    public void consume(Instant now) {
        if (!APPROVED.equals(status) || consumedAt != null) {
            throw new IllegalStateException("Only an unused approved request can be consumed");
        }
        this.status = CONSUMED;
        this.consumedAt = now;
    }

    /** Bảo vệ chuyển trạng thái quyết định khỏi yêu cầu đã xử lý. */
    private void requirePending() {
        if (!PENDING.equals(status)) throw new IllegalStateException("Approval request is not pending");
    }

    /** Từ chối chuỗi bắt buộc rỗng để entity không chứa yêu cầu không hợp lệ. */
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }

    /** Chuẩn hóa trường tùy chọn rỗng thành null để lưu trữ nhất quán. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
