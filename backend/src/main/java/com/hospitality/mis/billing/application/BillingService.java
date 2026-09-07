package com.hospitality.mis.billing.application;
import com.hospitality.mis.billing.adapter.InvoiceRepository;
import com.hospitality.mis.billing.api.InvoiceDtos;
import com.hospitality.mis.billing.domain.*;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.application.*;
import com.hospitality.mis.operations.adapter.EquipmentIncidentRepository;
import com.hospitality.mis.reservation.adapter.ReservationRepository;
import com.hospitality.mis.reservation.domain.*;
import com.hospitality.mis.room.domain.RoomStatus;
import com.hospitality.mis.guest.domain.MembershipTier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BillingService {
    private final ReservationRepository reservations; private final InvoiceRepository invoices; private final PricingPolicy pricing;
    private final AuditService audit; private final BigDecimal vipSpendThreshold; private final EquipmentIncidentRepository incidents; private final ApprovalService approvals;
    public BillingService(ReservationRepository reservations, InvoiceRepository invoices, PricingPolicy pricing, AuditService audit,
                          @Value("${hotel.policy.vip-spend-threshold:10000000}") BigDecimal vipSpendThreshold,
                          EquipmentIncidentRepository incidents, ApprovalService approvals) {
        this.reservations=reservations;this.invoices=invoices;this.pricing=pricing;this.audit=audit;this.vipSpendThreshold=vipSpendThreshold;this.incidents=incidents;this.approvals=approvals;
    }
    @Transactional
    public InvoiceDtos.Response checkOut(Long reservationId, LocalDateTime checkoutAt, PaymentMethod method, String actor) {
        var r=reservations.findForUpdate(reservationId).orElseThrow(()->new DomainException("RESERVATION_NOT_FOUND","Không tìm thấy đặt phòng"));
        if(r.getStatus()!=ReservationStatus.CHECKED_IN) throw new DomainException("INVALID_STATE","Chỉ được check-out đặt phòng đang ở");
        if(method==null) throw new DomainException("PAYMENT_METHOD_REQUIRED","Phải chọn phương thức thanh toán");
        var i=invoices.findByReservationId(reservationId).orElseGet(Invoice::new);
        if(i.getStatus()==PaymentStatus.DA_THANH_TOAN) throw new DomainException("ALREADY_PAID","Đặt phòng đã được thanh toán");
        BigDecimal room=BigDecimal.ZERO;
        for(var line:r.getRooms()) room=room.add(pricing.roomCharge(line.getRoom().getRoomType().getDailyPrice(),line.getCheckIn(),checkoutAt,"HOURLY".equals(r.getRentalType())));
        BigDecimal services=r.getServiceUsages()==null?BigDecimal.ZERO:r.getServiceUsages().stream().map(x->x.getService().getPrice().multiply(BigDecimal.valueOf(x.getQuantity()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal compensation=incidents.findByReservationId(reservationId).stream().map(x->x.getCompensation()).reduce(BigDecimal.ZERO,BigDecimal::add);
        var planned=r.getRooms().stream().map(ReservationRoom::getCheckOut).max(LocalDateTime::compareTo).orElse(checkoutAt);
        BigDecimal late=pricing.lateSurcharge(room,planned,checkoutAt);
        BigDecimal extension=pricing.extensionCharge(r.getRooms().isEmpty()?BigDecimal.ZERO:r.getRooms().get(0).getRoom().getRoomType().getDailyPrice(),r.getExtensionMinutes());
        BigDecimal subtotal=room.add(services).add(late).add(extension).add(compensation);
        BigDecimal discount=pricing.vipDiscount(subtotal,r.getGuest().getMembershipTier()==MembershipTier.VIP);
        BigDecimal deposit=r.getDepositAmount()==null?BigDecimal.ZERO:r.getDepositAmount(); BigDecimal due=subtotal.subtract(discount).subtract(deposit).max(BigDecimal.ZERO);
        i.setReservation(r);i.setIssuedAt(checkoutAt);i.setRoomTotal(room);i.setServiceTotal(services);i.setSurcharge(late);i.setExtensionFee(extension);i.setCompensation(compensation);i.setDiscount(discount);i.setDepositPaid(deposit);i.setAmountDue(due);i.setPaymentMethod(method);i.setStatus(PaymentStatus.DA_THANH_TOAN);invoices.save(i);
        r.setActualCheckOut(checkoutAt);r.transitionTo(ReservationStatus.CHECKED_OUT);
        r.getRooms().forEach(x->{x.setCheckOut(checkoutAt);x.setStatus(RoomStatus.RETURNED);x.getRoom().setStatus(RoomStatus.CLEANING);});
        r.getGuest().addSpend(due); if(r.getGuest().getTotalSpend().compareTo(vipSpendThreshold)>=0) r.getGuest().setMembershipTier(MembershipTier.VIP);
        audit.record(actor,"INVOICE_PAID","INVOICE",String.valueOf(i.getId()),null,due.toPlainString(),null); return toResponse(i);
    }
    @Transactional(readOnly=true) public InvoiceDtos.Response getByReservation(Long id){return invoices.findByReservationId(id).map(this::toResponse).orElseThrow(()->new DomainException("INVOICE_NOT_FOUND","Không tìm thấy hóa đơn"));}
    @Transactional public InvoiceDtos.Response refundDeposit(Long reservationId,String actor){var i=invoices.findByReservationId(reservationId).orElseThrow(()->new DomainException("INVOICE_NOT_FOUND","Không tìm thấy hóa đơn"));approvals.requireApproved("DEPOSIT_REFUND",String.valueOf(i.getId()),actor);var amount=i.getDepositPaid();i.setDepositPaid(BigDecimal.ZERO);i.setAmountDue(i.getAmountDue().add(amount));audit.record(actor,"DEPOSIT_REFUNDED","INVOICE",String.valueOf(i.getId()),amount.toPlainString(),"0",null);return toResponse(i);}
    @Transactional public InvoiceDtos.Response adjust(Long id,BigDecimal delta,String reason,String actor){if(delta==null||delta.signum()==0||reason==null||reason.isBlank())throw new DomainException("INVALID_ADJUSTMENT","Điều chỉnh phải có số tiền và lý do");var i=invoices.findById(id).orElseThrow(()->new DomainException("INVOICE_NOT_FOUND","Không tìm thấy hóa đơn"));approvals.requireApproved("BILLING_ADJUSTMENT",String.valueOf(id),actor);var before=i.getAmountDue();var after=before.add(delta);if(after.signum()<0)throw new DomainException("INVALID_ADJUSTMENT","Số tiền phải trả không thể âm");i.setAmountDue(after);audit.record(actor,"BILLING_ADJUSTED","INVOICE",String.valueOf(id),before.toPlainString(),after.toPlainString(),reason);return toResponse(i);}
    public InvoiceDtos.Response toResponse(Invoice i){return new InvoiceDtos.Response(i.getId(),i.getReservation().getId(),i.getIssuedAt(),i.getRoomTotal(),i.getServiceTotal(),i.getSurcharge(),i.getCompensation(),i.getExtensionFee(),i.getDiscount(),i.getDepositPaid(),i.getAmountDue(),i.getPaymentMethod(),i.getStatus());}
}
