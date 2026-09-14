package com.hospitality.mis.controller.billing;

import com.hospitality.mis.dto.billing.ReceiptDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.billing.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Cung cấp API tra cứu và phát hành biên lai cho hóa đơn.
 */
@RestController
@RequestMapping("/api/invoices/{invoiceId}/receipts")
public class ReceiptController {
    /** Dịch vụ tạo biên lai và kiểm tra quan hệ giữa biên lai, thanh toán và hóa đơn. */
    private final ReceiptService service;
    public ReceiptController(ReceiptService service) { this.service = service; }
    /**
     * Liệt kê biên lai của hóa đơn qua GET /api/invoices/{invoiceId}/receipts.
     * invoiceId là path parameter, không có query/header/body; trả danh sách biên lai để đối soát.
     * Chỉ BILLING_READ được phép; lỗi hóa đơn hoặc truy vấn do dịch vụ xử lý. Đây là thao tác đọc, không cần idempotency.
     */
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'BILLING_READ')")
    public List<ReceiptDtos.Response> list(@PathVariable Long invoiceId) { return service.listByInvoice(invoiceId); }
    /**
     * Phát hành biên lai qua POST /api/invoices/{invoiceId}/receipts.
     * invoiceId là path parameter, body tạo biên lai được {@code @Valid} kiểm tra, actor được lấy từ security context,
     * response là biên lai mới. Chỉ PAYMENT_WRITE được phép; không có idempotency key, còn trùng hoặc trạng thái không hợp lệ
     * do dịch vụ trả lỗi nghiệp vụ.
     */
    @PostMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'PAYMENT_WRITE')")
    public ReceiptDtos.Response issue(@PathVariable Long invoiceId, @Valid @RequestBody ReceiptDtos.CreateRequest request,
                                      @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        return service.issue(invoiceId, request, SecurityActor.currentActor(),
                key == null || key.isBlank() ? "receipt-" + request.receiptNumber() : key);
    }
}
