package com.hospitality.mis.service.governance;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.governance.ApprovalRepository;
import com.hospitality.mis.entity.governance.ApprovalRequest;
import com.hospitality.mis.middleware.security.SecurityActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.Clock;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import com.hospitality.mis.service.reservation.IdempotencySupport;

/** Quản lý yêu cầu phê duyệt, ràng buộc payload và tiêu thụ nguyên tử một lần. */
@Service
public class ApprovalService {
    /** Tập action được phép đi qua quy trình phê duyệt hiện tại. */
    private static final Set<String> SUPPORTED_ACTIONS = Set.of(
            "INVOICE_DELETE", "DEPOSIT_REFUND", "PRICE_OVERRIDE", "PAYMENT_REFUND", "BILLING_ADJUSTMENT",
            "ROOM_TYPE_ACTIVATE", "SERVICE_PRICE_CHANGE");

    /** Kho phê duyệt; các quyết định và consume dùng bản ghi có khóa. */
    private final ApprovalRepository approvals;
    /** Ghi audit cho yêu cầu, quyết định, hết hạn và consume. */
    private final AuditService audit;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    private DurableIdempotencyService durableIdempotency;

    public ApprovalService(ApprovalRepository approvals, AuditService audit) {
        this.approvals = approvals;
        this.audit = audit;
    }

    @org.springframework.beans.factory.annotation.Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }

    @org.springframework.beans.factory.annotation.Autowired
    void setDurableIdempotency(DurableIdempotencyService durableIdempotency) { this.durableIdempotency = durableIdempotency; }

    /** Tạo yêu cầu phê duyệt gắn với actor, target, payload, số tiền và lý do. */
    @Transactional
    public ApprovalRequest request(String requester, String action, String targetId, String payload,
                                   BigDecimal amount, String reason, String correlationKey) {
        String actor = SecurityActor.requireBoundActor(requester);
        requireText(action, "INVALID_APPROVAL_REQUEST", "Approval phải có thao tác");
        if (!SUPPORTED_ACTIONS.contains(action)) {
            throw new DomainException("UNSUPPORTED_APPROVAL", "Thao tác này không thuộc nhóm cần phê duyệt");
        }
        requireText(targetId, "INVALID_APPROVAL_REQUEST", "Approval phải có target");
        requireText(payload, "INVALID_APPROVAL_REQUEST", "Approval phải có exact mutation payload");
        requireText(reason, "INVALID_APPROVAL_REQUEST", "Approval phải có lý do");
        if (amount != null && amount.signum() < 0) {
            throw new DomainException("INVALID_APPROVAL_REQUEST", "Amount không được âm");
        }
        String key = IdempotencySupport.requireKey(correlationKey);
        String requestHash = IdempotencySupport.fingerprint("APPROVAL_REQUEST|" + action + "|" + targetId + "|"
                + payload + "|" + amount + "|" + reason.trim());
        if (durableIdempotency == null) return requestOnce(actor, action, targetId, payload, amount, reason, key);
        return durableIdempotency.executeWithReplay("approval-request", key, actor, requestHash,
                () -> requestOnce(actor, action, targetId, payload, amount, reason, key),
                () -> approvals.findFirstByRequesterAndCorrelationKey(actor, key)
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy approval đã ghi")));
    }

    private ApprovalRequest requestOnce(String actor, String action, String targetId, String payload,
                                        BigDecimal amount, String reason, String key) {
        ApprovalRequest saved = approvals.save(new ApprovalRequest(actor, action, targetId, payload,
                fingerprintFor(payload), amount, reason, null, key, Instant.now(clock)));
        audit.record(actor, "APPROVAL_REQUESTED", "APPROVAL", String.valueOf(saved.getId()),
                null, ApprovalRequest.PENDING, reason, key);
        return saved;
    }

    /** Khóa và phê duyệt một yêu cầu còn hiệu lực bởi approver hợp lệ. */
    @Transactional
    public ApprovalRequest approve(Long id, String approver) {
        return approve(id, approver, "approval-approve-" + id);
    }

    @Transactional
    public ApprovalRequest approve(Long id, String approver, String key) {
        String actor = SecurityActor.requireBoundActor(approver);
        String fingerprint = IdempotencySupport.fingerprint("APPROVAL_APPROVE|" + id);
        if (durableIdempotency != null) return durableIdempotency.executeWithReplay(
                "approval-approve", key, actor, fingerprint, () -> approveOnce(id, actor),
                () -> approvals.findById(id).orElseThrow(() -> new DomainException("APPROVAL_NOT_FOUND", "Không tìm thấy yêu cầu phê duyệt")));
        return approveOnce(id, actor);
    }

    private ApprovalRequest approveOnce(Long id, String actor) {
        ApprovalRequest approval = locked(id);
        expireOrRejectIfExpired(approval, actor);
        requireApproverRole(approval);
        if (!ApprovalRequest.PENDING.equals(approval.getStatus())) {
            throw new DomainException("APPROVAL_ALREADY_DECIDED", "Yêu cầu đã được xử lý");
        }
        if (actor.equals(approval.getRequester())) {
            throw new DomainException("SELF_APPROVAL_FORBIDDEN", "Không được tự phê duyệt yêu cầu của mình");
        }
        String before = approval.getStatus();
        approval.approve(actor, Instant.now(clock));
        audit.record(actor, "APPROVAL_APPROVED", "APPROVAL", String.valueOf(id), before,
                approval.getStatus(), approval.getReason(), approval.getCorrelationKey());
        return approval;
    }

    /** Khóa và từ chối một yêu cầu còn hiệu lực, không cho requester tự xử lý. */
    @Transactional
    public ApprovalRequest reject(Long id, String approver) {
        return reject(id, approver, "approval-reject-" + id);
    }

    @Transactional
    public ApprovalRequest reject(Long id, String approver, String key) {
        String actor = SecurityActor.requireBoundActor(approver);
        String fingerprint = IdempotencySupport.fingerprint("APPROVAL_REJECT|" + id);
        if (durableIdempotency != null) return durableIdempotency.executeWithReplay(
                "approval-reject", key, actor, fingerprint, () -> rejectOnce(id, actor),
                () -> approvals.findById(id).orElseThrow(() -> new DomainException("APPROVAL_NOT_FOUND", "Không tìm thấy yêu cầu phê duyệt")));
        return rejectOnce(id, actor);
    }

    private ApprovalRequest rejectOnce(Long id, String actor) {
        ApprovalRequest approval = locked(id);
        expireOrRejectIfExpired(approval, actor);
        requireApproverRole(approval);
        if (!ApprovalRequest.PENDING.equals(approval.getStatus())) {
            throw new DomainException("APPROVAL_ALREADY_DECIDED", "Yêu cầu đã được xử lý");
        }
        if (actor.equals(approval.getRequester())) {
            throw new DomainException("SELF_APPROVAL_FORBIDDEN", "Không được tự xử lý yêu cầu của mình");
        }
        String before = approval.getStatus();
        approval.reject(actor, Instant.now(clock));
        audit.record(actor, "APPROVAL_REJECTED", "APPROVAL", String.valueOf(id), before,
                approval.getStatus(), approval.getReason(), approval.getCorrelationKey());
        return approval;
    }

    /** Hết hạn các yêu cầu pending quá hạn rồi trả danh sách theo trạng thái. */
    @Transactional
    public List<ApprovalRequest> list(String status) {
        expirePending(Instant.now(clock));
        String selected = status == null || status.isBlank() ? ApprovalRequest.PENDING : status.toUpperCase();
        return approvals.findByStatusOrderByIdDesc(selected);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ApprovalRequest> page(String status, String action, String targetId, int page, int size) {
        expirePending(Instant.now(clock));
        String selected = status == null || status.isBlank() ? ApprovalRequest.PENDING : status.toUpperCase();
        return approvals.search(selected, action, targetId, org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size))));
    }

    /** Kiểm tra ràng buộc chính xác mà không tiêu thụ phê duyệt. */
    @Transactional
    public void requireApproved(String action, String targetId, String payload, BigDecimal amount, String actor) {
        String principal = SecurityActor.requireBoundActor(actor);
        validateBinding(action, targetId, payload);
        ApprovalRequest approval = approvals.findApprovedForBinding(action, targetId, principal,
                        fingerprintFor(payload), amount)
                .orElseThrow(() -> new DomainException("APPROVAL_REQUIRED", "Thao tác cần được phê duyệt trước: " + action));
        if (approval.isExpired(Instant.now(clock))) {
            expireAndAudit(approval, principal);
            throw new DomainException("APPROVAL_EXPIRED", "Yêu cầu phê duyệt đã hết hạn");
        }
    }

    /** Khóa và tiêu thụ nguyên tử một phê duyệt chính xác; lời gọi thứ hai không thể tiêu thụ phê duyệt đó. */
    @Transactional
    public ApprovalRequest consumeApproved(String action, String targetId, String payload,
                                           BigDecimal amount, String actor) {
        String principal = SecurityActor.requireBoundActor(actor);
        validateBinding(action, targetId, payload);
        ApprovalRequest approval = approvals.findApprovedForBindingWithLock(action, targetId, principal,
                        fingerprintFor(payload), amount)
                .orElseThrow(() -> new DomainException("APPROVAL_REQUIRED", "Thao tác cần được phê duyệt trước: " + action));
        if (approval.isExpired(Instant.now(clock))) {
            expireAndAudit(approval, principal);
            throw new DomainException("APPROVAL_EXPIRED", "Yêu cầu phê duyệt đã hết hạn");
        }

        String before = approval.getStatus();
        approval.consume(Instant.now(clock));
        audit.record(principal, "APPROVAL_CONSUMED", "APPROVAL", String.valueOf(approval.getId()), before,
                approval.getStatus(), approval.getReason(), approval.getCorrelationKey());
        return approval;
    }

    /** Các luồng gọi thao tác thay đổi hiện có phải cung cấp ràng buộc trước khi tiêu thụ phê duyệt. */
    @Transactional
    public void requireApproved(String action, String targetId, String actor) {
        SecurityActor.requireBoundActor(actor);
        throw new DomainException("APPROVAL_PAYLOAD_REQUIRED", "Approval phải gắn với exact mutation payload");
    }

    /** Các luồng gọi thao tác thay đổi hiện có phải cung cấp ràng buộc trước khi tiêu thụ phê duyệt. */
    @Transactional
    public void consumeApproved(String action, String targetId, String actor) {
        SecurityActor.requireBoundActor(actor);
        throw new DomainException("APPROVAL_PAYLOAD_REQUIRED", "Approval phải gắn với exact mutation payload");
    }

    /** Băm payload để approval chỉ khớp đúng mutation đã xin phê duyệt. */
    public static String fingerprintFor(String payload) {
        if (payload == null || payload.isBlank()) throw new IllegalArgumentException("payload must not be blank");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required", exception);
        }
    }

    /** Tải yêu cầu với row lock để các quyết định cạnh tranh không cùng cập nhật. */
    private ApprovalRequest locked(Long id) {
        if (id == null) throw new DomainException("APPROVAL_NOT_FOUND", "Không tìm thấy yêu cầu phê duyệt");
        return approvals.findWithLockById(id)
                .orElseThrow(() -> new DomainException("APPROVAL_NOT_FOUND", "Không tìm thấy yêu cầu phê duyệt"));
    }

    /** Đánh dấu hết hạn và audit trước khi từ chối thao tác trên yêu cầu quá hạn. */
    private void expireOrRejectIfExpired(ApprovalRequest approval, String actor) {
        if (ApprovalRequest.PENDING.equals(approval.getStatus()) && approval.isExpired(Instant.now(clock))) {
            expireAndAudit(approval, actor);
            throw new DomainException("APPROVAL_EXPIRED", "Yêu cầu phê duyệt đã hết hạn");
        }
    }

    /** Quét và đóng các yêu cầu pending đã vượt thời hạn. */
    private void expirePending(Instant now) {
        approvals.findByStatusAndExpiresAtLessThanEqual(ApprovalRequest.PENDING, now)
                .forEach(approval -> expireAndAudit(approval, "SYSTEM"));
    }

    /** Chuyển một approval pending sang expired và ghi dấu actor thực hiện. */
    private void expireAndAudit(ApprovalRequest approval, String actor) {
        if (!ApprovalRequest.PENDING.equals(approval.getStatus())) return;
        String before = approval.getStatus();
        approval.expire(Instant.now(clock));
        audit.record(actor, "APPROVAL_EXPIRED", "APPROVAL", String.valueOf(approval.getId()), before,
                approval.getStatus(), approval.getReason(), approval.getCorrelationKey());
    }

    private static void validateBinding(String action, String targetId, String payload) {
        requireText(action, "APPROVAL_REQUIRED", "Approval action không hợp lệ");
        requireText(targetId, "APPROVAL_REQUIRED", "Approval target không hợp lệ");
        requireText(payload, "APPROVAL_REQUIRED", "Approval payload không hợp lệ");
    }

    private static void requireText(String value, String code, String message) {
        if (value == null || value.isBlank()) throw new DomainException(code, message);
    }

    /** Áp chính sách vai trò: refund/deposit refund chỉ DIRECTOR được duyệt. */
    private void requireApproverRole(ApprovalRequest approval) {
        if (!Set.of("PAYMENT_REFUND", "DEPOSIT_REFUND").contains(approval.getAction())) return;
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean director = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DIRECTOR"));
        if (!director) throw new DomainException("DIRECTOR_APPROVAL_REQUIRED", "Chỉ DIRECTOR được phê duyệt hoàn tiền");
    }
}
