package com.hospitality.mis.finance;

import com.hospitality.mis.dao.billing.PaymentTransactionRepository;
import com.hospitality.mis.dao.finance.*;
import com.hospitality.mis.dto.finance.FinanceDtos;
import com.hospitality.mis.entity.finance.*;
import com.hospitality.mis.service.finance.FinanceService;
import com.hospitality.mis.service.governance.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceSettlementLedgerTest {
    @Mock CashShiftHandoverRepository handovers;
    @Mock ExpenseRepository expenses;
    @Mock PartnerDebtRepository debts;
    @Mock AuditService audit;
    @Mock PaymentTransactionRepository transactions;
    @Mock PartnerDebtSettlementRepository settlements;
    @Mock FinancialLedgerEntryRepository ledger;
    private FinanceService service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("accounting", "test", java.util.List.of()));
        service = new FinanceService(handovers, expenses, debts, audit, transactions);
        ReflectionTestUtils.setField(service, "debtSettlements", settlements);
        ReflectionTestUtils.setField(service, "ledger", ledger);
        ReflectionTestUtils.setField(service, "clock",
                Clock.fixed(Instant.parse("2026-09-14T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }

    @Test
    void settlementCreatesImmutableHistoryAndFinalizedLedgerEntry() {
        PartnerDebt debt = new PartnerDebt(); ReflectionTestUtils.setField(debt, "id", 8L);
        debt.setAmount(new BigDecimal("1000")); debt.setSettledAmount(BigDecimal.ZERO);
        when(debts.findForUpdate(8L)).thenReturn(Optional.of(debt));

        service.settleDebt(8L, new FinanceDtos.DebtSettlementRequest(new BigDecimal("250"), "bank"), "accounting");

        assertThat(debt.getSettledAmount()).isEqualByComparingTo("250");
        verify(settlements).save(argThat(x -> x.getAmount().compareTo(new BigDecimal("250")) == 0
                && x.getSettledBy().equals("accounting")));
        verify(ledger).save(argThat(x -> x.isFinalized()
                && x.getEntryType().equals("PARTNER_DEBT_SETTLEMENT")
                && x.getAmount().compareTo(new BigDecimal("250")) == 0));
    }
}
