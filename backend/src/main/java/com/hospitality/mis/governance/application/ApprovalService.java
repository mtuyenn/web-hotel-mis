package com.hospitality.mis.governance.application;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.adapter.ApprovalRepository;
import com.hospitality.mis.governance.domain.ApprovalRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {
    private final ApprovalRepository approvals;
    private final AuditService audit;
    public ApprovalService(ApprovalRepository approvals, AuditService audit) { this.approvals = approvals; this.audit = audit; }

    @Transactional
    public ApprovalRequest request(String requester, String action, String targetId, String reason) {
        if (!java.util.Set.of("INVOICE_DELETE", "DEPOSIT_REFUND", "PRICE_OVERRIDE", "PAYMENT_REFUND", "BILLING_ADJUSTMENT").contains(action))
            throw new DomainException("UNSUPPORTED_APPROVAL", "Thao tác này không thuộc nhóm cần phê duyệt");
        var saved = approvals.save(new ApprovalRequest(requester, action, targetId, reason));
        audit.record(requester, "APPROVAL_REQUESTED", "APPROVAL", saved.getId().toString(), null, action, reason);
        return saved;
    }

    @Transactional
    public ApprovalRequest approve(Long id, String approver) {
        if (approver == null || approver.isBlank()) throw new DomainException("APPROVER_REQUIRED", "Thiếu người phê duyệt");
        var approval = approvals.findWithLockById(id).orElseThrow(() -> new DomainException("APPROVAL_NOT_FOUND", "Không tìm thấy yêu cầu phê duyệt"));
        if (!"PENDING".equals(approval.getStatus())) throw new DomainException("APPROVAL_ALREADY_DECIDED", "Yêu cầu đã được xử lý");
        approval.approve(approver); audit.record(approver, "APPROVAL_GRANTED", "APPROVAL", id.toString(), "PENDING", "APPROVED", null);
        return approval;
    }

    /** Enforces approval at the use-case boundary, not only at the HTTP route. */
    @Transactional(readOnly = true)
    public void requireApproved(String action, String targetId, String actor) {
        if (action == null || targetId == null || actor == null || actor.isBlank()
                || approvals.findFirstByActionAndTargetIdAndStatusAndRequesterNotOrderByIdDesc(action, targetId, "APPROVED", actor).isEmpty()) {
            throw new DomainException("APPROVAL_REQUIRED", "Thao tác cần được phê duyệt trước: " + action);
        }
    }
}
