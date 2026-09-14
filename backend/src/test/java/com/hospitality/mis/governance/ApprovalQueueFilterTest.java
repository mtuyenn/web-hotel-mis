package com.hospitality.mis.governance;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.governance.ApprovalRepository;
import com.hospitality.mis.entity.governance.ApprovalRequest;
import com.hospitality.mis.service.governance.ApprovalService;
import com.hospitality.mis.service.governance.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalQueueFilterTest {
    @Mock ApprovalRepository approvals;
    @Mock AuditService audit;
    private ApprovalService service;

    @BeforeEach
    void setUp() {
        service = new ApprovalService(approvals, audit);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "clock",
                Clock.fixed(Instant.parse("2026-09-14T00:00:00Z"), ZoneOffset.UTC));
        when(approvals.findByStatusAndExpiresAtLessThanEqual(eq(ApprovalRequest.PENDING), any()))
                .thenReturn(List.of());
    }

    @Test
    void queuePassesRiskAndRequestedDateToRepository() {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        when(approvals.searchAdvanced(eq("PENDING"), eq("PRICE_OVERRIDE"), eq("R1"), eq("requester"),
                eq("HIGH"), eq(from), eq(to), any(Pageable.class))).thenReturn(Page.empty());

        service.page(null, "PRICE_OVERRIDE", "R1", "requester", "high", from, to, 0, 20);

        verify(approvals).searchAdvanced(eq("PENDING"), eq("PRICE_OVERRIDE"), eq("R1"), eq("requester"),
                eq("HIGH"), eq(from), eq(to), any(Pageable.class));
    }

    @Test
    void queueRejectsUnknownRisk() {
        DomainException error = assertThrows(DomainException.class,
                () -> service.page(null, null, null, null, "urgent", null, null, 0, 20));
        assertThat(error.getCode()).isEqualTo("INVALID_APPROVAL_RISK");
    }
}
