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

/** Bảo vệ các quy tắc thanh toán P0: tính tiền, phê duyệt, hoàn tiền và chống lặp. */
class BillingP0CorrectnessTest {
    /** Repository giả lập để cô lập workflow khỏi DB nhưng vẫn quan sát khóa và việc ghi. */
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    /** Invoice giả lập; fixture được cấu hình để kiểm tra amount due và adjustment. */
    private final InvoiceRepository invoices = mock(InvoiceRepository.class);
    /** Sổ giao dịch giả lập, dùng để xác minh payment/refund append-only. */
    private final PaymentTransactionRepository transactions = mock(PaymentTransactionRepository.class);
    /** Kho receipt giả lập, bảo vệ việc tạo chứng từ khi thu tiền. */
    private final ReceiptRepository receipts = mock(ReceiptRepository.class);
    /** Kho sự cố giả lập để workflow tính compensation mà không chạm persistence. */
    private final EquipmentIncidentRepository incidents = mock(EquipmentIncidentRepository.class);
    /** Cổng approval dùng để kiểm tra adjustment/refund phải được duyệt và consume. */
    private final ApprovalService approvals = mock(ApprovalService.class);
    /** Audit giả lập; fixture này giữ trọng tâm ở kết quả billing. */
    private final AuditService audit = mock(AuditService.class);
    /** Kho adjustment giả lập để chứng minh điều chỉnh được thêm mới, không sửa dòng cũ. */
    private final InvoiceAdjustmentRepository adjustments = mock(InvoiceAdjustmentRepository.class);
    /** Chính sách giá cố định: tối thiểu 3 giờ, grace 20 phút, đơn vị 10. */
    private final PricingPolicy pricing = new PricingPolicy(3, 20, BigDecimal.TEN);

    /** Đặt actor front desk và các câu trả lời repository mặc định trước mỗi kịch bản. */
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

    /** Xóa SecurityContext để không rò actor giữa các test. */
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    /** Given hai phòng, extension, late checkout và service snapshot, When checkout, Then mỗi khoản được tính đúng một lần. */
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

    /** VIP dùng thời lượng đã đặt, kể cả thuê theo giờ, và chỉ cộng một lượt cho cả reservation. */
    @Test void vipStayUsesBookedWindowAndCountsOneForMultiRoomHourlyBooking() {
        LocalDateTime start = LocalDateTime.of(2031, 1, 1, 12, 0);
        Reservation reservation = checkedInReservation(start);
        reservation.setRentalType("HOURLY");
        addRoom(reservation, "101", new BigDecimal("2400000"), start, start.plusHours(24));
        addRoom(reservation, "102", new BigDecimal("2400000"), start, start.plusHours(24));
        when(reservations.findForUpdate(9L)).thenReturn(Optional.of(reservation));
        when(invoices.findByReservationId(9L)).thenReturn(Optional.empty());

        // Checkout thực tế sớm hơn 24 giờ; thời lượng booking vẫn đủ 24 giờ nên được tính một lượt.
        service().checkOut(9L, start.plusHours(20), PaymentMethod.CASH, "clerk");

        assertThat(reservation.getGuest().getCompletedStays()).isEqualTo(1);
    }

    /** Booking dưới 24 giờ không được tính VIP dù khách trả phòng thực tế sau hơn 24 giờ. */
    @Test void vipStayDoesNotUseActualCheckoutForShortBookedWindow() {
        LocalDateTime start = LocalDateTime.of(2031, 1, 1, 12, 0);
        Reservation reservation = checkedInReservation(start);
        addRoom(reservation, "101", new BigDecimal("2400000"), start, start.plusHours(23));
        when(reservations.findForUpdate(9L)).thenReturn(Optional.of(reservation));
        when(invoices.findByReservationId(9L)).thenReturn(Optional.empty());

        service().checkOut(9L, start.plusHours(25), PaymentMethod.CASH, "clerk");

        assertThat(reservation.getGuest().getCompletedStays()).isZero();
    }

    /** Given adjustment chưa dùng, When ghi adjustment, Then cần approval, append một dòng và đổi payable. */
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

    /** Given tiền cọc đã thu, When front desk hủy, Then refund bị ràng buộc bởi approval và consume đúng một lần. */
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

    /** Tạo service thật với các port giả lập để mọi test dùng cùng cấu hình billing. */
    private BillingService service() {
        return new BillingService(reservations, invoices, pricing, audit, incidents, approvals, transactions, receipts, adjustments);
    }

    /** Dựng reservation đã CHECKED_IN; id 9 là khóa liên kết với các stub invoice. */
    private Reservation checkedInReservation(LocalDateTime start) {
        Guest guest = new Guest(); guest.setId(1L);
        Employee employee = new Employee(); employee.setEmployeeId("clerk");
        Reservation reservation = new Reservation(); reservation.setGuest(guest); reservation.setEmployee(employee);
        reservation.setRentalType("PACKAGE"); reservation.transitionTo(ReservationStatus.CONFIRMED);
        reservation.setActualCheckIn(start); reservation.transitionTo(ReservationStatus.CHECKED_IN);
        ReflectionTestUtils.setField(reservation, "id", 9L);
        return reservation;
    }

    /** Dựng reservation có invoice tiền cọc 500000 để kiểm tra hoàn cọc. */
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

    /** Dựng giao dịch tiền cọc COMPLETED; các giá trị id/reference giúp kiểm tra liên kết refund. */
    private PaymentTransaction depositTransaction(Invoice invoice) {
        PaymentTransaction deposit = new PaymentTransaction(); ReflectionTestUtils.setField(deposit, "id", 80L);
        deposit.setInvoice(invoice); deposit.setAmount(new BigDecimal("500000")); deposit.setMethod(PaymentMethod.CASH);
        deposit.setType(PaymentTransaction.TransactionType.PAYMENT);
        deposit.setStatus(PaymentTransaction.TransactionStatus.COMPLETED); deposit.setReference("DEPOSIT:9");
        deposit.setOccurredAt(LocalDateTime.of(2031, 1, 1, 10, 0)); deposit.setActorId("clerk");
        deposit.setIdempotencyKey("deposit-key");
        return deposit;
    }

    /** Thêm một dòng phòng với original checkout để tách extension khỏi thời gian thuê gốc. */
    private void addRoom(Reservation reservation, String id, BigDecimal price,
                         LocalDateTime checkIn, LocalDateTime checkOut) {
        RoomType type = new RoomType(); type.setId("T" + id); type.setName("Type " + id); type.setDailyPrice(price);
        Room room = new Room(); room.setId(id); room.setRoomType(type); room.setStatus(RoomStatus.OCCUPIED);
        ReservationRoom line = new ReservationRoom(); line.setRoom(room); line.setCheckIn(checkIn);
        line.setCheckOut(checkOut); line.setOriginalCheckOut(checkOut.minusMinutes(reservation.getExtensionMinutes()));
        line.setStatus(RoomStatus.OCCUPIED); reservation.addRoom(line);
    }
}
