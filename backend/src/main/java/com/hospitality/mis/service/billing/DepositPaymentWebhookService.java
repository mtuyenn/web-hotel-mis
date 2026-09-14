package com.hospitality.mis.service.billing;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.billing.InvoiceRepository;
import com.hospitality.mis.dao.billing.PaymentTransactionRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dto.billing.DepositPaymentWebhookDtos;
import com.hospitality.mis.entity.billing.Invoice;
import com.hospitality.mis.entity.billing.PaymentMethod;
import com.hospitality.mis.entity.billing.PaymentStatus;
import com.hospitality.mis.entity.billing.PaymentTransaction;
import com.hospitality.mis.entity.reservation.DepositPaymentStatus;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.HexFormat;

/** Xác minh callback cọc, ghi ledger một lần và xác nhận booking. */
@Service
public class DepositPaymentWebhookService {
    private final ReservationRepository reservations;
    private final InvoiceRepository invoices;
    private final PaymentTransactionRepository transactions;
    private final AuditService audit;
    private final String secret;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

    public DepositPaymentWebhookService(ReservationRepository reservations, InvoiceRepository invoices,
                                        PaymentTransactionRepository transactions, AuditService audit,
                                        @Value("${hotel.payment.webhook-secret:}") String secret) {
        this.reservations = reservations;
        this.invoices = invoices;
        this.transactions = transactions;
        this.audit = audit;
        this.secret = secret;
    }

    @org.springframework.beans.factory.annotation.Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }

    @Transactional
    public DepositPaymentWebhookDtos.Response accept(DepositPaymentWebhookDtos.Request request, String signature) {
        if (request == null || !"SUCCESS".equalsIgnoreCase(request.status().trim()))
            throw error("PAYMENT_NOT_SUCCESS", "Callback cọc không ở trạng thái thành công");
        verifySignature(request, signature);
        String eventId = request.providerEventId().trim();
        var prior = transactions.findByExternalEventId(eventId);
        if (prior.isPresent()) return response(prior.get().getInvoice().getReservation(), prior.get());

        Reservation reservation = reservations.findByDepositPaymentCodeForUpdate(request.paymentCode().trim())
                .orElseThrow(() -> error("PAYMENT_CODE_NOT_FOUND", "Không tìm thấy mã thanh toán cọc"));
        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.NO_SHOW)
            throw error("RESERVATION_NOT_PAYABLE", "Booking không còn nhận thanh toán cọc");
        if (reservation.getDepositPaymentStatus() == DepositPaymentStatus.PAID)
            throw error("DEPOSIT_ALREADY_PAID", "Tiền cọc đã được xác nhận");
        if (reservation.getDepositPaymentExpiresAt() != null && !reservation.getDepositPaymentExpiresAt().isAfter(LocalDateTime.now(clock)))
            throw error("PAYMENT_CODE_EXPIRED", "Mã thanh toán cọc đã hết hạn");
        if (request.amount().compareTo(reservation.getDepositAmount()) != 0)
            throw error("PAYMENT_AMOUNT_MISMATCH", "Số tiền cọc không khớp booking");

        Invoice invoice = invoices.findByReservationId(reservation.getId()).orElseGet(() -> {
            Invoice created = new Invoice();
            created.setReservation(reservation);
            created.setIssuedAt(LocalDateTime.now(clock));
            return created;
        });
        invoice.setDepositPaid(request.amount());
        invoice.setAmountDue(BigDecimal.ZERO);
        invoice.setStatus(PaymentStatus.DU_KIEN);
        invoice.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        invoice = invoices.saveAndFlush(invoice);

        PaymentTransaction payment = new PaymentTransaction();
        payment.setInvoice(invoice);
        payment.setAmount(request.amount());
        payment.setMethod(PaymentMethod.BANK_TRANSFER);
        payment.setType(PaymentTransaction.TransactionType.PAYMENT);
        payment.setStatus(PaymentTransaction.TransactionStatus.COMPLETED);
        payment.setReference(request.reference().trim());
        payment.setOccurredAt(LocalDateTime.now(clock));
        payment.setActorId("PAYMENT_GATEWAY");
        payment.setExternalEventId(eventId);
        payment.setIdempotencyKey(PaymentTransaction.storageIdempotencyKey("GATEWAY:" + eventId,
                "PAYMENT_GATEWAY", request.amount(), payment.getMethod(), payment.getType(), payment.getReference()));
        payment = transactions.saveAndFlush(payment);

        reservation.setDepositPaymentStatus(DepositPaymentStatus.PAID);
        if (reservation.getStatus() == ReservationStatus.DRAFT) reservation.transitionTo(ReservationStatus.DEPOSIT_PAID);
        reservations.saveAndFlush(reservation);
        audit.record("PAYMENT_GATEWAY", "DEPOSIT_PAYMENT_CONFIRMED", "RESERVATION",
                reservation.getId().toString(), "PENDING", "PAID", eventId);
        return response(reservation, payment);
    }

    private void verifySignature(DepositPaymentWebhookDtos.Request request, String signature) {
        if (secret == null || secret.isBlank()) throw error("PAYMENT_WEBHOOK_NOT_CONFIGURED", "Payment webhook chưa được cấu hình");
        if (signature == null || signature.isBlank()) throw error("PAYMENT_SIGNATURE_INVALID", "Thiếu chữ ký payment callback");
        String canonical = request.providerEventId().trim() + "|" + request.paymentCode().trim() + "|"
                + request.amount().toPlainString() + "|" + request.status().trim().toUpperCase() + "|" + request.reference().trim();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), signature.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII)))
                throw error("PAYMENT_SIGNATURE_INVALID", "Chữ ký payment callback không hợp lệ");
        } catch (java.security.GeneralSecurityException exception) {
            throw new DomainException("PAYMENT_SIGNATURE_INVALID", "Không thể xác minh chữ ký payment callback");
        }
    }

    private DepositPaymentWebhookDtos.Response response(Reservation reservation, PaymentTransaction payment) {
        return new DepositPaymentWebhookDtos.Response(true, reservation.getId(), reservation.getStatus().name(),
                payment.getStatus().name());
    }

    private DomainException error(String code, String message) { return new DomainException(code, message); }
}
