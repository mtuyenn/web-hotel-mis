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
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

@Service
public class ApprovalService {
    private static final Set<String> SUPPORTED_ACTIONS = Set.of(
            "INVOICE_DELETE", "DEPOSIT_REFUND", "PRICE_OVERRIDE", "PAYMENT_REFUND", "BILLING_ADJUSTMENT");

    private final ApprovalRepository approvals;
    private final AuditService audit;

    public ApprovalService(ApprovalRepository approvals, AuditService audit) {
        this.approvals = approvals;
        this.audit = audit;
    }

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

        ApprovalRequest saved = approvals.save(new ApprovalRequest(actor, action, targetId, payload,
                fingerprintFor(payload), amount, reason, null, correlationKey));
        audit.record(actor, "APPROVAL_REQUESTED", "APPROVAL", String.valueOf(saved.getId()),
                null, ApprovalRequest.PENDING, reason, correlationKey);
        return saved;
    }

    @Transactional
    public ApprovalRequest approve(Long id, String approver) {
        String actor = SecurityActor.requireBoundActor(approver);
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
        approval.approve(actor, Instant.now());
        audit.record(actor, "APPROVAL_APPROVED", "APPROVAL", String.valueOf(id), before,
                approval.getStatus(), approval.getReason(), approval.getCorrelationKey());
        return approval;
    }

    @Transactional
    public ApprovalRequest reject(Long id, String approver) {
        String actor = SecurityActor.requireBoundActor(approver);
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
        approval.reject(actor, Instant.now());
        audit.record(actor, "APPROVAL_REJECTED", "APPROVAL", String.valueOf(id), before,
                approval.getStatus(), approval.getReason(), approval.getCorrelationKey());
        return approval;
    }

    @Transactional
    public List<ApprovalRequest> list(String status) {
        expirePending(Instant.now());
        String selected = status == null || status.isBlank() ? ApprovalRequest.PENDING : status.toUpperCase();
        return approvals.findByStatusOrderByIdDesc(selected);
    }

    /** Checks the exact binding without consuming it. */
    @Transactional
    public void requireApproved(String action, String targetId, String payload, BigDecimal amount, String actor) {
        String principal = SecurityActor.requireBoundActor(actor);
        validateBinding(action, targetId, payload);
        ApprovalRequest approval = approvals.findApprovedForBinding(action, targetId, principal,
                        fingerprintFor(payload), amount)
                .orElseThrow(() -> new DomainException("APPROVAL_REQUIRED", "Thao tác cần được phê duyệt trước: " + action));
        if (approval.isExpired(Instant.now())) {
            expireAndAudit(approval, principal);
            throw new DomainException("APPROVAL_EXPIRED", "Yêu cầu phê duyệt đã hết hạn");
        }
    }

    /** Atomically locks and consumes one exact approval; a second caller cannot consume it. */
    @Transactional
    public ApprovalRequest consumeApproved(String action, String targetId, String payload,
                                           BigDecimal amount, String actor) {
        String principal = SecurityActor.requireBoundActor(actor);
        validateBinding(action, targetId, payload);
        ApprovalRequest approval = approvals.findApprovedForBindingWithLock(action, targetId, principal,
                        fingerprintFor(payload), amount)
                .orElseThrow(() -> new DomainException("APPROVAL_REQUIRED", "Thao tác cần được phê duyệt trước: " + action));
        if (approval.isExpired(Instant.now())) {
            expireAndAudit(approval, principal);
            throw new DomainException("APPROVAL_EXPIRED", "Yêu cầu phê duyệt đã hết hạn");
        }

        String before = approval.getStatus();
        approval.consume(Instant.now());
        audit.record(principal, "APPROVAL_CONSUMED", "APPROVAL", String.valueOf(approval.getId()), before,
                approval.getStatus(), approval.getReason(), approval.getCorrelationKey());
        return approval;
    }

    /** Existing mutation callers must provide the binding before consuming an approval. */
    @Transactional
    public void requireApproved(String action, String targetId, String actor) {
        SecurityActor.requireBoundActor(actor);
        throw new DomainException("APPROVAL_PAYLOAD_REQUIRED", "Approval phải gắn với exact mutation payload");
    }

    /** Existing mutation callers must provide the binding before consuming an approval. */
    @Transactional
    public void consumeApproved(String action, String targetId, String actor) {
        SecurityActor.requireBoundActor(actor);
        throw new DomainException("APPROVAL_PAYLOAD_REQUIRED", "Approval phải gắn với exact mutation payload");
    }

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

    private ApprovalRequest locked(Long id) {
        if (id == null) throw new DomainException("APPROVAL_NOT_FOUND", "Không tìm thấy yêu cầu phê duyệt");
        return approvals.findWithLockById(id)
                .orElseThrow(() -> new DomainException("APPROVAL_NOT_FOUND", "Không tìm thấy yêu cầu phê duyệt"));
    }

    private void expireOrRejectIfExpired(ApprovalRequest approval, String actor) {
        if (ApprovalRequest.PENDING.equals(approval.getStatus()) && approval.isExpired(Instant.now())) {
            expireAndAudit(approval, actor);
            throw new DomainException("APPROVAL_EXPIRED", "Yêu cầu phê duyệt đã hết hạn");
        }
    }

    private void expirePending(Instant now) {
        approvals.findByStatusAndExpiresAtLessThanEqual(ApprovalRequest.PENDING, now)
                .forEach(approval -> expireAndAudit(approval, "SYSTEM"));
    }

    private void expireAndAudit(ApprovalRequest approval, String actor) {
        if (!ApprovalRequest.PENDING.equals(approval.getStatus())) return;
        String before = approval.getStatus();
        approval.expire(Instant.now());
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

    private void requireApproverRole(ApprovalRequest approval) {
        if (!Set.of("PAYMENT_REFUND", "DEPOSIT_REFUND").contains(approval.getAction())) return;
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean director = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DIRECTOR"));
        if (!director) throw new DomainException("DIRECTOR_APPROVAL_REQUIRED", "Chỉ DIRECTOR được phê duyệt hoàn tiền");
    }
}
