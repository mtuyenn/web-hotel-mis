package com.hospitality.mis.governance;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.governance.ApprovalRepository;
import com.hospitality.mis.entity.governance.ApprovalRequest;
import com.hospitality.mis.service.governance.ApprovalService;
import com.hospitality.mis.service.governance.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {
    @Mock ApprovalRepository approvals;
    @Mock AuditService audit;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void expiredPendingApprovalIsMarkedExpiredAndCannotBeApproved() {
        ApprovalRequest approval = request(Instant.now().minusSeconds(1));
        when(approvals.findWithLockById(1L)).thenReturn(Optional.of(approval));
        authenticateAs("manager");

        assertThatThrownBy(() -> service().approve(1L, "manager"))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo("APPROVAL_EXPIRED");

        assertThat(approval.getStatus()).isEqualTo(ApprovalRequest.EXPIRED);
        verify(audit).record(eq("manager"), eq("APPROVAL_EXPIRED"), eq("APPROVAL"), eq("1"),
                eq(ApprovalRequest.PENDING), eq(ApprovalRequest.EXPIRED), any(), any());
    }

    @Test
    void requesterCannotApproveOwnRequest() {
        ApprovalRequest approval = request(Instant.now().plusSeconds(60));
        when(approvals.findWithLockById(1L)).thenReturn(Optional.of(approval));
        authenticateAs("requester");

        assertThatThrownBy(() -> service().approve(1L, "requester"))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo("SELF_APPROVAL_FORBIDDEN");
        assertThat(approval.getStatus()).isEqualTo(ApprovalRequest.PENDING);
    }

    @Test
    void wrongActionTargetPayloadOrAmountCannotConsumeApproval() {
        ApprovalRequest approval = request(Instant.now().plusSeconds(60));
        approval.approve("manager", Instant.now());
        when(approvals.findApprovedForBindingWithLock(eq("PRICE_OVERRIDE"), eq("room-1"), eq("executor"),
                eq(ApprovalService.fingerprintFor("{\"price\":10}")), eq(new BigDecimal("11"))))
                .thenReturn(Optional.empty());
        authenticateAs("executor");

        assertThatThrownBy(() -> service().consumeApproved("PRICE_OVERRIDE", "room-1", "{\"price\":10}",
                new BigDecimal("11"), "executor"))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo("APPROVAL_REQUIRED");
    }

    @Test
    void consumeIsOnceOnlyAndRecordsConsumedState() {
        ApprovalRequest approval = request(Instant.now().plusSeconds(60));
        approval.approve("manager", Instant.now());
        when(approvals.findApprovedForBindingWithLock(eq("PRICE_OVERRIDE"), eq("room-1"), eq("executor"),
                eq(ApprovalService.fingerprintFor("{\"price\":10}")), eq(new BigDecimal("10"))))
                .thenReturn(Optional.of(approval), Optional.empty());
        authenticateAs("executor");
        ApprovalService service = service();

        service.consumeApproved("PRICE_OVERRIDE", "room-1", "{\"price\":10}", new BigDecimal("10"), "executor");
        assertThat(approval.getStatus()).isEqualTo(ApprovalRequest.CONSUMED);
        assertThat(approval.getConsumedAt()).isNotNull();

        assertThatThrownBy(() -> service.consumeApproved("PRICE_OVERRIDE", "room-1", "{\"price\":10}",
                new BigDecimal("10"), "executor"))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo("APPROVAL_REQUIRED");
        verify(approvals, org.mockito.Mockito.times(2)).findApprovedForBindingWithLock(
                eq("PRICE_OVERRIDE"), eq("room-1"), eq("executor"),
                eq(ApprovalService.fingerprintFor("{\"price\":10}")), eq(new BigDecimal("10")));
    }

    @Test
    void onlyDirectorCanApproveRefund() {
        ApprovalRequest refund = new ApprovalRequest("clerk", "PAYMENT_REFUND", "invoice-1", "{}",
                ApprovalService.fingerprintFor("{}"), new BigDecimal("100"), "refund", Instant.now().plusSeconds(60), "refund-1");
        ReflectionTestUtils.setField(refund, "id", 2L);
        when(approvals.findWithLockById(2L)).thenReturn(Optional.of(refund));
        authenticateAs("manager");
        assertThatThrownBy(() -> service().approve(2L, "manager"))
                .extracting("code").isEqualTo("DIRECTOR_APPROVAL_REQUIRED");
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("director", "n/a", "ROLE_DIRECTOR"));
        service().approve(2L, "director");
        assertThat(refund.getStatus()).isEqualTo(ApprovalRequest.APPROVED);
        assertThat(refund.getApprover()).isEqualTo("director");
    }

    private ApprovalService service() {
        return new ApprovalService(approvals, audit);
    }

    private static ApprovalRequest request(Instant expiresAt) {
        ApprovalRequest request = new ApprovalRequest("requester", "PRICE_OVERRIDE", "room-1",
                "{\"price\":10}", ApprovalService.fingerprintFor("{\"price\":10}"),
                new BigDecimal("10"), "price change", expiresAt, "approval-test");
        ReflectionTestUtils.setField(request, "id", 1L);
        return request;
    }

    private static void authenticateAs(String actor) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(actor, "n/a", "ROLE_MANAGER"));
    }
}
