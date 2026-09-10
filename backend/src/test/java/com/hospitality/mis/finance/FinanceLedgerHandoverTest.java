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

class FinanceLedgerHandoverTest {
    private final CashShiftHandoverRepository handovers = mock(CashShiftHandoverRepository.class);
    private final PaymentTransactionRepository transactions = mock(PaymentTransactionRepository.class);
    private final AuditService audit = mock(AuditService.class);

    @BeforeEach void auth() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("cashier", "", "ROLE_ACCOUNTING"));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

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
