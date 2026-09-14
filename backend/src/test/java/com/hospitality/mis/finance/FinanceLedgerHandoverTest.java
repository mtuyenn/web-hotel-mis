package com.hospitality.mis.finance;

import com.hospitality.mis.dao.billing.PaymentTransactionRepository;
import com.hospitality.mis.dao.finance.*;
import com.hospitality.mis.dto.finance.FinanceDtos;
import com.hospitality.mis.entity.finance.CashShiftHandover;
import com.hospitality.mis.service.finance.FinanceService;
import com.hospitality.mis.service.governance.AuditService;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Bảo vệ bàn giao ca: expected phải lấy từ ledger tiền mặt, không nhận từ client. */
class FinanceLedgerHandoverTest {
    /** Kho bàn giao giả lập để kiểm tra mốc ca trước và row mới. */
    private final CashShiftHandoverRepository handovers = mock(CashShiftHandoverRepository.class);
    /** Ledger payment giả lập; net cash 950000 là expected authoritative. */
    private final PaymentTransactionRepository transactions = mock(PaymentTransactionRepository.class);
    /** Audit giả lập vì test tập trung vào số tiền bàn giao. */
    private final AuditService audit = mock(AuditService.class);

    /** Đăng nhập actor accounting, đúng role được phép bàn giao tiền. */
    @BeforeEach void auth() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("cashier", "", "ROLE_ACCOUNTING"));
    }
    /** Dọn security context sau test. */
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    /** Given ledger 950000 nhưng client khai 940000, When handover, Then variance là -10000. */
    @Test void expectedAmountComesFromUnhandedCashLedgerAndClientCannotSupplyIt() {
        LocalDateTime priorAt = LocalDateTime.of(2031, 1, 1, 8, 0);
        CashShiftHandover prior = new CashShiftHandover(); prior.setHandedOverAt(priorAt);
        when(handovers.findFirstByFromActorOrderByHandedOverAtDesc("cashier")).thenReturn(Optional.of(prior));
        when(transactions.netCashByActorBetween(eq("cashier"), eq(priorAt), any())).thenReturn(new BigDecimal("950000"));
        when(handovers.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        FinanceService service = new FinanceService(handovers, mock(ExpenseRepository.class),
                mock(PartnerDebtRepository.class), audit, transactions);

        var result = service.handover(new FinanceDtos.CashHandoverRequest(
                "SHIFT-2", "cashier", "next", new BigDecimal("940000"), "counted"), "cashier");

        assertThat(result.expectedAmount()).isEqualByComparingTo("950000");
        assertThat(result.variance()).isEqualByComparingTo("-10000");
    }
}
