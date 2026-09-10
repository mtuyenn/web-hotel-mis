package com.hospitality.mis.service.billing;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.billing.InvoiceRepository;
import com.hospitality.mis.dao.billing.PaymentTransactionRepository;
import com.hospitality.mis.dao.billing.ReceiptRepository;
import com.hospitality.mis.dto.billing.ReceiptDtos;
import com.hospitality.mis.entity.billing.PaymentTransaction;
import com.hospitality.mis.entity.billing.Receipt;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReceiptService {
    private final ReceiptRepository receipts;
    private final InvoiceRepository invoices;
    private final PaymentTransactionRepository transactions;
    private final AuditService audit;

    public ReceiptService(ReceiptRepository receipts, InvoiceRepository invoices,
                          PaymentTransactionRepository transactions, AuditService audit) {
        this.receipts = receipts; this.invoices = invoices; this.transactions = transactions; this.audit = audit;
    }

    @Transactional
    public ReceiptDtos.Response issue(Long invoiceId, ReceiptDtos.CreateRequest request, String actor) {
        if (request == null || request.receiptNumber() == null || request.receiptNumber().isBlank()
                || request.amount() == null || request.amount().signum() <= 0 || request.method() == null)
            throw new DomainException("INVALID_RECEIPT", "Biên lai phải có số, số tiền và phương thức");
        String boundActor = SecurityActor.requireBoundActor(actor);
        if (receipts.findByReceiptNumber(request.receiptNumber()).isPresent())
            throw new DomainException("RECEIPT_EXISTS", "Số biên lai đã tồn tại");
        var invoice = invoices.findForUpdate(invoiceId)
                .orElseThrow(() -> new DomainException("INVOICE_NOT_FOUND", "Không tìm thấy hóa đơn"));
        requireScope(invoice, boundActor);
        BigDecimal paid = transactions.findByInvoiceIdAndStatus(invoiceId, PaymentTransaction.TransactionStatus.COMPLETED).stream()
                .filter(x -> x.getMethod() == request.method())
                .map(x -> x.getType() == PaymentTransaction.TransactionType.PAYMENT ? x.getAmount() : x.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal issued = receipts.findByInvoiceIdOrderByIssuedAtAsc(invoiceId).stream()
                .filter(x -> x.getMethod() == request.method()).map(Receipt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (request.amount().compareTo(paid.subtract(issued)) > 0)
            throw new DomainException("RECEIPT_EXCEEDS_PAYMENT", "Biên lai vượt số tiền đã thu chưa lập biên lai");
        Receipt receipt = new Receipt(); receipt.setReceiptNumber(request.receiptNumber()); receipt.setInvoice(invoice);
        receipt.setAmount(request.amount()); receipt.setMethod(request.method()); receipt.setIssuedAt(LocalDateTime.now());
        receipt.setIssuedBy(boundActor);
        Receipt saved = receipts.save(receipt);
        audit.record(boundActor, "RECEIPT_ISSUED", "RECEIPT", String.valueOf(saved.getId()), null,
                request.amount().toPlainString(), "TENDER:" + request.method().name());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReceiptDtos.Response> listByInvoice(Long invoiceId) {
        var invoice = invoices.findById(invoiceId)
                .orElseThrow(() -> new DomainException("INVOICE_NOT_FOUND", "Không tìm thấy hóa đơn"));
        requireScope(invoice, SecurityActor.currentActor());
        return receipts.findByInvoiceIdOrderByIssuedAtAsc(invoiceId).stream().map(this::toResponse).toList();
    }

    private void requireScope(com.hospitality.mis.entity.billing.Invoice invoice, String actor) {
        var reservation = invoice.getReservation();
        if (reservation == null || reservation.getEmployee() == null) throw new AccessDeniedException("Thiếu phạm vi đặt phòng");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean global = auth != null && auth.getAuthorities().stream().map(x -> x.getAuthority())
                .anyMatch(x -> x.equals("ROLE_ADMIN") || x.equals("ROLE_DIRECTOR") || x.equals("ROLE_MANAGER") || x.equals("ROLE_ACCOUNTING") || x.equals("ROLE_FRONT_DESK"));
        if (!global && !actor.equals(reservation.getEmployee().getEmployeeId()))
            throw new AccessDeniedException("Không được phép thao tác ngoài phạm vi đặt phòng");
    }

    private ReceiptDtos.Response toResponse(Receipt r) {
        return new ReceiptDtos.Response(r.getId(), r.getReceiptNumber(), r.getInvoice().getId(), r.getAmount(),
                r.getMethod(), r.getIssuedAt(), r.getIssuedBy());
    }
}
