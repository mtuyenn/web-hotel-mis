package com.hospitality.mis.service.billing;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.billing.InvoiceRepository;
import com.hospitality.mis.dao.billing.PaymentTransactionRepository;
import com.hospitality.mis.dto.billing.PaymentTransactionDtos;
import com.hospitality.mis.entity.billing.Invoice;
import com.hospitality.mis.entity.billing.PaymentStatus;
import com.hospitality.mis.entity.billing.PaymentTransaction;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.governance.ApprovalService;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PaymentTransactionService {
    private final PaymentTransactionRepository transactions;
    private final InvoiceRepository invoices;
    private final ApprovalService approvals;
    private final AuditService audit;
    private final PricingPolicy pricing;

    public PaymentTransactionService(PaymentTransactionRepository transactions, InvoiceRepository invoices,
                                     ApprovalService approvals, AuditService audit, PricingPolicy pricing) {
        this.transactions = transactions; this.invoices = invoices; this.approvals = approvals; this.audit = audit;
        this.pricing = pricing;
    }

    @Transactional
    public PaymentTransactionDtos.Response record(Long invoiceId, PaymentTransactionDtos.CreateRequest request, String actor) {
        if (request == null || request.amount() == null || request.amount().signum() <= 0 || request.method() == null
                || request.type() == null) throw error("INVALID_TRANSACTION", "Giao dịch phải có số tiền, phương thức và loại");
        String boundActor = SecurityActor.requireBoundActor(actor);
        if (request.idempotencyKey() == null || request.idempotencyKey().isBlank() || request.idempotencyKey().trim().length() > 35)
            throw error("INVALID_IDEMPOTENCY_KEY", "idempotency_key phải có từ 1 đến 35 ký tự");
        Invoice invoice = invoices.findForUpdate(invoiceId)
                .orElseThrow(() -> error("INVOICE_NOT_FOUND", "Không tìm thấy hóa đơn"));
        requireScope(invoice, boundActor);

        String key = request.idempotencyKey() == null || request.idempotencyKey().isBlank()
                ? null : request.idempotencyKey().trim();
        String storedKey = PaymentTransaction.storageIdempotencyKey(key, boundActor, request.amount(), request.method(),
                request.type(), request.reference());
        if (key != null) {
            List<PaymentTransaction> prior = transactions.findByIdempotencyKeyPrefix(key + ".");
            if (!prior.isEmpty()) {
                PaymentTransaction found = prior.get(0);
                if (!invoiceId.equals(found.getInvoice().getId()) || !storedKey.equals(found.getStoredIdempotencyKey()))
                    throw error("IDEMPOTENCY_MISMATCH", "Idempotency key đã được dùng cho payload khác");
                return toResponse(found);
            }
        }

        boolean needsApproval = request.type() == PaymentTransaction.TransactionType.REFUND;
        // Bind approval to the exact request, including the retry key, amount and reference.
        String approvalPayload = request.approvalPayload(invoiceId);
        if (needsApproval)
            approvals.requireApproved("PAYMENT_REFUND", String.valueOf(invoiceId), approvalPayload, request.amount(), boundActor);
        BigDecimal total = pricing.roundFinalTotal(invoice.getRoomTotal().add(invoice.getServiceTotal()).add(invoice.getSurcharge())
                .add(invoice.getCompensation()).add(invoice.getExtensionFee()).add(invoice.getAdjustmentTotal())
                .subtract(invoice.getDiscount()).max(BigDecimal.ZERO));
        BigDecimal netPaid = netPaid(invoice);
        PaymentTransaction source = null;
        if (request.type() == PaymentTransaction.TransactionType.PAYMENT) {
            if (request.amount().compareTo(total.subtract(netPaid).max(BigDecimal.ZERO)) > 0)
                throw error("PAYMENT_EXCEEDS_BALANCE", "Số tiền thu vượt số dư hóa đơn");
        } else {
            if (request.amount().compareTo(netPaid) > 0)
                throw error("REFUND_EXCEEDS_PAID", "Số tiền hoàn vượt số tiền đã thu");
            source = sourceForRefund(invoice, request.amount());
        }

        PaymentTransaction tx = new PaymentTransaction(); tx.setInvoice(invoice); tx.setAmount(request.amount());
        tx.setMethod(source == null ? request.method() : source.getMethod()); tx.setType(request.type());
        tx.setStatus(PaymentTransaction.TransactionStatus.COMPLETED); tx.setOccurredAt(LocalDateTime.now());
        tx.setActorId(boundActor); tx.setIdempotencyKey(storedKey);
        tx.setReference(source == null ? request.reference() : "REFUND_OF:" + source.getId() + ":"
                + (request.reference() == null || request.reference().isBlank() ? "PAYMENT_REFUND" : request.reference()));
        PaymentTransaction saved = transactions.saveAndFlush(tx);
        reconcile(invoice);
        if (needsApproval)
            approvals.consumeApproved("PAYMENT_REFUND", String.valueOf(invoiceId), approvalPayload, request.amount(), boundActor);
        audit.record(boundActor, request.type() == PaymentTransaction.TransactionType.PAYMENT
                        ? "PAYMENT_RECORDED" : "PAYMENT_REFUNDED", "PAYMENT_TRANSACTION", String.valueOf(saved.getId()),
                null, request.amount().toPlainString(), saved.getReference());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDtos.Response> listByInvoice(Long invoiceId) {
        Invoice invoice = invoices.findById(invoiceId).orElseThrow(() -> error("INVOICE_NOT_FOUND", "Không tìm thấy hóa đơn"));
        requireScope(invoice, SecurityActor.currentActor());
        return transactions.findByInvoiceIdOrderByOccurredAtAsc(invoiceId).stream().map(this::toResponse).toList();
    }

    private PaymentTransaction sourceForRefund(Invoice invoice, BigDecimal amount) {
        return transactions.findByInvoiceIdAndStatus(invoice.getId(), PaymentTransaction.TransactionStatus.COMPLETED).stream()
                .filter(x -> x.getType() == PaymentTransaction.TransactionType.PAYMENT)
                .sorted(Comparator.comparing(PaymentTransaction::getOccurredAt))
                .filter(x -> remaining(x, invoice).compareTo(amount) >= 0)
                .findFirst().orElseThrow(() -> error("REFUND_SOURCE_NOT_FOUND", "Không tìm thấy giao dịch gốc để hoàn tiền"));
    }

    private BigDecimal remaining(PaymentTransaction source, Invoice invoice) {
        BigDecimal refunded = transactions.findByInvoiceIdAndStatus(invoice.getId(), PaymentTransaction.TransactionStatus.COMPLETED).stream()
                .filter(x -> x.getType() == PaymentTransaction.TransactionType.REFUND && x.getReference() != null
                        && x.getReference().startsWith("REFUND_OF:" + source.getId() + ":"))
                .map(PaymentTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return source.getAmount().subtract(refunded);
    }

    private BigDecimal netPaid(Invoice invoice) {
        var completed = transactions.findByInvoiceIdAndStatus(invoice.getId(), PaymentTransaction.TransactionStatus.COMPLETED);
        BigDecimal paid = completed.stream().filter(x -> x.getType() == PaymentTransaction.TransactionType.PAYMENT)
                .map(PaymentTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refunded = completed.stream().filter(x -> x.getType() == PaymentTransaction.TransactionType.REFUND)
                .map(PaymentTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return paid.subtract(refunded);
    }

    private void reconcile(Invoice invoice) {
        BigDecimal total = pricing.roundFinalTotal(invoice.getRoomTotal().add(invoice.getServiceTotal()).add(invoice.getSurcharge())
                .add(invoice.getCompensation()).add(invoice.getExtensionFee()).add(invoice.getAdjustmentTotal())
                .subtract(invoice.getDiscount()).max(BigDecimal.ZERO));
        BigDecimal balance = total.subtract(netPaid(invoice)).max(BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
        invoice.setAmountDue(balance);
        invoice.setStatus(total.signum() == 0 && invoice.getStatus() == PaymentStatus.DU_KIEN ? PaymentStatus.DU_KIEN
                : balance.signum() == 0 ? PaymentStatus.DA_THANH_TOAN : PaymentStatus.CHUA_THANH_TOAN);
        invoices.save(invoice);
    }

    private void requireScope(Invoice invoice, String actor) {
        var reservation = invoice.getReservation();
        if (reservation == null || reservation.getEmployee() == null) throw new AccessDeniedException("Thiếu phạm vi đặt phòng");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean global = auth != null && auth.getAuthorities().stream().map(x -> x.getAuthority())
                .anyMatch(x -> x.equals("ROLE_ADMIN") || x.equals("ROLE_DIRECTOR") || x.equals("ROLE_MANAGER") || x.equals("ROLE_ACCOUNTING") || x.equals("ROLE_FRONT_DESK"));
        if (!global && !actor.equals(reservation.getEmployee().getEmployeeId()))
            throw new AccessDeniedException("Không được phép thao tác ngoài phạm vi đặt phòng");
    }

    private PaymentTransactionDtos.Response toResponse(PaymentTransaction tx) {
        return new PaymentTransactionDtos.Response(tx.getId(), tx.getInvoice().getId(), tx.getAmount(), tx.getMethod(),
                tx.getType(), tx.getStatus(), tx.getReference(), tx.getOccurredAt(), tx.getActorId());
    }

    private DomainException error(String code, String message) { return new DomainException(code, message); }
}
