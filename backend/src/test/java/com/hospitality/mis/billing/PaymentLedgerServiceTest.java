package com.hospitality.mis.billing;

import com.hospitality.mis.dao.billing.InvoiceRepository;
import com.hospitality.mis.dao.billing.PaymentTransactionRepository;
import com.hospitality.mis.dao.billing.ReceiptRepository;
import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dto.billing.PaymentTransactionDtos;
import com.hospitality.mis.entity.billing.Invoice;
import com.hospitality.mis.entity.billing.PaymentMethod;
import com.hospitality.mis.entity.billing.PaymentTransaction;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.service.billing.PaymentTransactionService;
import com.hospitality.mis.service.billing.BillingService;
import com.hospitality.mis.service.billing.PricingPolicy;
import com.hospitality.mis.service.governance.ApprovalService;
import com.hospitality.mis.service.governance.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentLedgerServiceTest {
    private final PaymentTransactionRepository transactions = mock(PaymentTransactionRepository.class);
    private final InvoiceRepository invoices = mock(InvoiceRepository.class);
    private final ApprovalService approvals = mock(ApprovalService.class);
    private final AuditService audit = mock(AuditService.class);
    private final PricingPolicy pricing = new PricingPolicy(3, 20, new BigDecimal("10"));
    private final List<PaymentTransaction> ledger = new ArrayList<>();
    private Invoice invoice;
    private PaymentTransactionService service;

    @BeforeEach
    void setUp() {
        Employee employee = new Employee(); employee.setEmployeeId("clerk");
        Reservation reservation = new Reservation(); reservation.setEmployee(employee);
        invoice = new Invoice(); ReflectionTestUtils.setField(invoice, "id", 7L); invoice.setReservation(reservation);
        invoice.setRoomTotal(new BigDecimal("1000.00"));
        when(invoices.findForUpdate(7L)).thenReturn(Optional.of(invoice));
        when(transactions.findByInvoiceIdAndStatus(eq(7L), any())).thenAnswer(invocation -> ledger);
        when(transactions.findByIdempotencyKeyPrefix(any())).thenAnswer(invocation -> ledger.stream()
                .filter(x -> x.getStoredIdempotencyKey() != null && x.getStoredIdempotencyKey().startsWith(invocation.getArgument(0, String.class)))
                .toList());
        when(transactions.saveAndFlush(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", (long) ledger.size() + 1);
            ledger.add(saved);
            return saved;
        });
        service = new PaymentTransactionService(transactions, invoices, approvals, audit, pricing);
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("clerk", "secret", "ROLE_FRONT_DESK"));
    }

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    @Test
    void partialThenFullPaymentReconcilesInvoiceFromLedger() {
        service.record(7L, request("400.00", PaymentTransaction.TransactionType.PAYMENT, "pay-1"), "clerk");
        assertThat(invoice.getAmountDue()).isEqualByComparingTo("600.00");
        service.record(7L, request("600.00", PaymentTransaction.TransactionType.PAYMENT, "pay-2"), "clerk");
        assertThat(invoice.getAmountDue()).isEqualByComparingTo("0.00");
        assertThat(invoice.getStatus().name()).isEqualTo("DA_THANH_TOAN");
        assertThat(ledger).hasSize(2);
    }

    @Test
    void invoiceReadAndCollectionUseTheSameRoundedFinalTotal() {
        invoice.setRoomTotal(new BigDecimal("1250500.00"));
        service.record(7L, request("1251000.00", PaymentTransaction.TransactionType.PAYMENT, "rounded"), "clerk");
        when(invoices.findByReservationId(9L)).thenReturn(Optional.of(invoice));
        ReflectionTestUtils.setField(invoice.getReservation(), "id", 9L);
        BillingService billing = new BillingService(mock(ReservationRepository.class), invoices, pricing, audit,
                mock(EquipmentIncidentRepository.class), approvals, transactions, mock(ReceiptRepository.class));
        assertThat(billing.getByReservation(9L).payable()).isEqualByComparingTo("0");
        assertThat(billing.getByReservation(9L).status().name()).isEqualTo("DA_THANH_TOAN");
    }

    @Test
    void paymentIdempotencyKeyCannotReplayAnotherInvoice() {
        service.record(7L, request("400.00", PaymentTransaction.TransactionType.PAYMENT, "cross-invoice"), "clerk");
        Invoice other = new Invoice(); ReflectionTestUtils.setField(other, "id", 8L);
        other.setReservation(invoice.getReservation()); other.setRoomTotal(new BigDecimal("1000"));
        when(invoices.findForUpdate(8L)).thenReturn(Optional.of(other));
        assertThatThrownBy(() -> service.record(8L, request("400.00", PaymentTransaction.TransactionType.PAYMENT, "cross-invoice"), "clerk"))
                .extracting("code").isEqualTo("IDEMPOTENCY_MISMATCH");
        assertThat(ledger).hasSize(1);
    }

    @Test
    void refundCreatesOneReversalWithSourceLinkAndPreservesTender() {
        service.record(7L, request("1000.00", PaymentTransaction.TransactionType.PAYMENT, "pay-1"), "clerk");
        PaymentTransactionDtos.CreateRequest refund = new PaymentTransactionDtos.CreateRequest(
                new BigDecimal("250.00"), PaymentMethod.CARD, PaymentTransaction.TransactionType.REFUND,
                "customer-request", "refund-1");
        service.record(7L, refund, "clerk");
        PaymentTransaction reversal = ledger.get(1);
        assertThat(reversal.getType()).isEqualTo(PaymentTransaction.TransactionType.REFUND);
        assertThat(reversal.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(reversal.getSourceTransactionId()).isEqualTo("1");
        assertThat(invoice.getAmountDue()).isEqualByComparingTo("250.00");
        verify(approvals).requireApproved(eq("PAYMENT_REFUND"), eq("7"), any(), eq(new BigDecimal("250.00")), eq("clerk"));
        verify(approvals).consumeApproved(eq("PAYMENT_REFUND"), eq("7"), any(), eq(new BigDecimal("250.00")), eq("clerk"));
    }

    @Test
    void duplicateRefundReplaysWithoutAppendingOrConsumingApprovalAgain() {
        service.record(7L, request("1000.00", PaymentTransaction.TransactionType.PAYMENT, "pay-1"), "clerk");
        PaymentTransactionDtos.CreateRequest refund = new PaymentTransactionDtos.CreateRequest(
                new BigDecimal("1000.00"), PaymentMethod.CASH, PaymentTransaction.TransactionType.REFUND,
                "duplicate-safe", "refund-1");
        service.record(7L, refund, "clerk");
        service.record(7L, refund, "clerk");
        assertThat(ledger).hasSize(2);
        verify(approvals).requireApproved(eq("PAYMENT_REFUND"), eq("7"), any(), eq(new BigDecimal("1000.00")), eq("clerk"));
        verify(approvals).consumeApproved(eq("PAYMENT_REFUND"), eq("7"), any(), eq(new BigDecimal("1000.00")), eq("clerk"));
    }

    @Test
    void sameIdempotencyKeyWithDifferentPayloadConflicts() {
        service.record(7L, request("400.00", PaymentTransaction.TransactionType.PAYMENT, "same-key"), "clerk");
        assertThatThrownBy(() -> service.record(7L,
                request("401.00", PaymentTransaction.TransactionType.PAYMENT, "same-key"), "clerk"))
                .isInstanceOf(com.hospitality.mis.common.exception.DomainException.class)
                .hasMessageContaining("different payload");
        assertThat(ledger).hasSize(1);
    }

    @Test
    void actorOutsideReservationScopeIsRejected() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("other", "secret", "ROLE_STAFF"));
        assertThatThrownBy(() -> service.record(7L, request("100.00", PaymentTransaction.TransactionType.PAYMENT, "scope"), "other"))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        assertThat(ledger).isEmpty();
    }

    @Test
    void storedIdempotencyBindingIncludesActorAndCanonicalPayload() {
        String one = PaymentTransaction.storageIdempotencyKey("key", "clerk", new BigDecimal("10.00"),
                PaymentMethod.CASH, PaymentTransaction.TransactionType.PAYMENT, "ref");
        String two = PaymentTransaction.storageIdempotencyKey("key", "other", new BigDecimal("10.00"),
                PaymentMethod.CASH, PaymentTransaction.TransactionType.PAYMENT, "ref");
        assertThat(one).isNotEqualTo(two);
        assertThat(one).startsWith("key.");
    }

    @Test
    void depositAppendsPaymentAndTenderReceiptTogether() {
        Reservation reservation = new Reservation();
        ReflectionTestUtils.setField(reservation, "id", 11L);
        reservation.setEmployee(employee("clerk"));
        reservation.setDepositAmount(new BigDecimal("500.00"));
        ReceiptRepository receiptRepository = mock(ReceiptRepository.class);
        when(invoices.saveAndFlush(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 21L);
            return saved;
        });
        BillingService billing = new BillingService(mock(ReservationRepository.class), invoices,
                new PricingPolicy(3, 20, new BigDecimal("10")), audit,
                mock(EquipmentIncidentRepository.class), approvals, transactions, receiptRepository);

        billing.registerDeposit(reservation);

        assertThat(ledger).singleElement().satisfies(payment -> {
            assertThat(payment.getType()).isEqualTo(PaymentTransaction.TransactionType.PAYMENT);
            assertThat(payment.getAmount()).isEqualByComparingTo("500.00");
            assertThat(payment.getReference()).isEqualTo("DEPOSIT:11");
        });
        verify(receiptRepository).save(any(com.hospitality.mis.entity.billing.Receipt.class));
    }

    private PaymentTransactionDtos.CreateRequest request(String amount, PaymentTransaction.TransactionType type, String key) {
        return new PaymentTransactionDtos.CreateRequest(new BigDecimal(amount), PaymentMethod.CASH, type, null, key);
    }

    private Employee employee(String id) {
        Employee employee = new Employee(); employee.setEmployeeId(id); return employee;
    }
}
