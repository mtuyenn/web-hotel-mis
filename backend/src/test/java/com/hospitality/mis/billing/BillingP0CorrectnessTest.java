package com.hospitality.mis.billing;

import com.hospitality.mis.dao.billing.*;
import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.entity.billing.*;
import com.hospitality.mis.entity.guest.Guest;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.reservation.*;
import com.hospitality.mis.entity.room.*;
import com.hospitality.mis.service.billing.*;
import com.hospitality.mis.service.governance.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BillingP0CorrectnessTest {
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    private final InvoiceRepository invoices = mock(InvoiceRepository.class);
    private final PaymentTransactionRepository transactions = mock(PaymentTransactionRepository.class);
    private final ReceiptRepository receipts = mock(ReceiptRepository.class);
    private final EquipmentIncidentRepository incidents = mock(EquipmentIncidentRepository.class);
    private final ApprovalService approvals = mock(ApprovalService.class);
    private final AuditService audit = mock(AuditService.class);
    private final InvoiceAdjustmentRepository adjustments = mock(InvoiceAdjustmentRepository.class);
    private final PricingPolicy pricing = new PricingPolicy(3, 20, BigDecimal.TEN);

    @BeforeEach void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("clerk", "", "ROLE_FRONT_DESK"));
        when(transactions.findByInvoiceIdAndStatus(anyLong(), any())).thenReturn(List.of());
        when(incidents.findByReservationId(anyLong())).thenReturn(List.of());
        when(invoices.saveAndFlush(any())).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            ReflectionTestUtils.setField(invoice, "id", 70L);
            return invoice;
        });
    }

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void twoRoomsExtensionLateFeeAndServiceSnapshotAreCalculatedOnce() {
        LocalDateTime start = LocalDateTime.of(2031, 1, 1, 12, 0);
        Reservation reservation = checkedInReservation(start);
        reservation.setExtensionMinutes(120);
        addRoom(reservation, "101", new BigDecimal("2400000"), start, start.plusHours(26));
        addRoom(reservation, "102", new BigDecimal("2400000"), start, start.plusHours(26));
        Service catalog = new Service(); catalog.setId("SPA"); catalog.setPrice(new BigDecimal("200"));
        ServiceUsage usage = new ServiceUsage(reservation, catalog, LocalDate.of(2031, 1, 1), 2);
        usage.setUnitPrice(new BigDecimal("100"));
        ReflectionTestUtils.setField(reservation, "serviceUsages", Set.of(usage));
        when(reservations.findForUpdate(9L)).thenReturn(Optional.of(reservation));
        when(invoices.findByReservationId(9L)).thenReturn(Optional.empty());

        var result = service().checkOut(9L, start.plusHours(26).plusMinutes(21), PaymentMethod.CASH, "clerk");

        assertThat(result.roomTotal()).isEqualByComparingTo("4800000");
        assertThat(result.extensionTotal()).isEqualByComparingTo("400000");
        assertThat(result.lateSurcharge()).isEqualByComparingTo("960000");
        assertThat(result.serviceTotal()).isEqualByComparingTo("200");
        assertThat(result.payable()).isEqualByComparingTo("6160000");
    }

    @Test void adjustmentIsAppendOnlyApprovedIdempotentAndChangesPayable() {
        Employee employee = new Employee(); employee.setEmployeeId("clerk");
        Reservation reservation = new Reservation(); reservation.setEmployee(employee);
        Invoice invoice = new Invoice(); ReflectionTestUtils.setField(invoice, "id", 7L);
        invoice.setReservation(reservation); invoice.setRoomTotal(new BigDecimal("1000000"));
        when(invoices.findForUpdate(7L)).thenReturn(Optional.of(invoice));
        when(adjustments.findByIdempotencyKeyStartingWith("adjust-1.")).thenReturn(List.of());
        when(adjustments.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service().adjust(7L, new BigDecimal("125000"), "manual correction", "clerk", "adjust-1");

        assertThat(response.adjustmentTotal()).isEqualByComparingTo("125000");
        assertThat(response.payable()).isEqualByComparingTo("1125000");
        verify(approvals).requireApproved(eq("BILLING_ADJUSTMENT"), eq("7"), any(), eq(new BigDecimal("125000")), eq("clerk"));
        verify(approvals).consumeApproved(eq("BILLING_ADJUSTMENT"), eq("7"), any(), eq(new BigDecimal("125000")), eq("clerk"));
        verify(adjustments).saveAndFlush(argThat(row -> row.getDelta().compareTo(new BigDecimal("125000")) == 0));
    }

    @Test void cancellationDepositRefundRequiresAndConsumesApprovalForFrontDesk() {
        Reservation reservation = reservationWithDepositInvoice();
        Invoice invoice = invoices.findByReservationId(9L).orElseThrow();
        PaymentTransaction deposit = depositTransaction(invoice);
        when(transactions.findByInvoiceIdAndStatus(invoice.getId(), PaymentTransaction.TransactionStatus.COMPLETED))
                .thenReturn(List.of(deposit));
        when(transactions.findByIdempotencyKeyPrefix("CANCELLATION_DEPOSIT:9.")).thenReturn(List.of());
        when(transactions.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service().settleCancellationDeposit(reservation, "clerk", false);

        verify(approvals).requireApproved(eq("DEPOSIT_REFUND"), eq("70"), any(),
                eq(new BigDecimal("500000")), eq("clerk"));
        verify(approvals).consumeApproved(eq("DEPOSIT_REFUND"), eq("70"), any(),
                eq(new BigDecimal("500000")), eq("clerk"));
        verify(transactions).saveAndFlush(argThat(tx -> tx.getType() == PaymentTransaction.TransactionType.REFUND));
    }

    private BillingService service() {
        return new BillingService(reservations, invoices, pricing, audit, incidents, approvals, transactions, receipts, adjustments);
    }

    private Reservation checkedInReservation(LocalDateTime start) {
        Guest guest = new Guest(); guest.setId(1L);
        Employee employee = new Employee(); employee.setEmployeeId("clerk");
        Reservation reservation = new Reservation(); reservation.setGuest(guest); reservation.setEmployee(employee);
        reservation.setRentalType("PACKAGE"); reservation.transitionTo(ReservationStatus.CONFIRMED);
        reservation.setActualCheckIn(start); reservation.transitionTo(ReservationStatus.CHECKED_IN);
        ReflectionTestUtils.setField(reservation, "id", 9L);
        return reservation;
    }

    private Reservation reservationWithDepositInvoice() {
        Guest guest = new Guest(); guest.setId(1L);
        Employee employee = new Employee(); employee.setEmployeeId("clerk");
        Reservation reservation = new Reservation(); reservation.setGuest(guest); reservation.setEmployee(employee);
        reservation.setDepositAmount(new BigDecimal("500000")); ReflectionTestUtils.setField(reservation, "id", 9L);
        Invoice invoice = new Invoice(); ReflectionTestUtils.setField(invoice, "id", 70L);
        invoice.setReservation(reservation); invoice.setDepositPaid(new BigDecimal("500000"));
        when(invoices.findByReservationId(9L)).thenReturn(Optional.of(invoice));
        return reservation;
    }

    private PaymentTransaction depositTransaction(Invoice invoice) {
        PaymentTransaction deposit = new PaymentTransaction(); ReflectionTestUtils.setField(deposit, "id", 80L);
        deposit.setInvoice(invoice); deposit.setAmount(new BigDecimal("500000")); deposit.setMethod(PaymentMethod.CASH);
        deposit.setType(PaymentTransaction.TransactionType.PAYMENT);
        deposit.setStatus(PaymentTransaction.TransactionStatus.COMPLETED); deposit.setReference("DEPOSIT:9");
        deposit.setOccurredAt(LocalDateTime.of(2031, 1, 1, 10, 0)); deposit.setActorId("clerk");
        deposit.setIdempotencyKey("deposit-key");
        return deposit;
    }

    private void addRoom(Reservation reservation, String id, BigDecimal price,
                         LocalDateTime checkIn, LocalDateTime checkOut) {
        RoomType type = new RoomType(); type.setId("T" + id); type.setName("Type " + id); type.setDailyPrice(price);
        Room room = new Room(); room.setId(id); room.setRoomType(type); room.setStatus(RoomStatus.OCCUPIED);
        ReservationRoom line = new ReservationRoom(); line.setRoom(room); line.setCheckIn(checkIn);
        line.setCheckOut(checkOut); line.setOriginalCheckOut(checkOut.minusMinutes(reservation.getExtensionMinutes()));
        line.setStatus(RoomStatus.OCCUPIED); reservation.addRoom(line);
    }
}
