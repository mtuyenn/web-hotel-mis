package com.hospitality.mis.controller.billing;

import com.hospitality.mis.dto.billing.ReceiptDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.billing.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/invoices/{invoiceId}/receipts")
public class ReceiptController {
    private final ReceiptService service;
    public ReceiptController(ReceiptService service) { this.service = service; }
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'BILLING_READ')")
    public List<ReceiptDtos.Response> list(@PathVariable Long invoiceId) { return service.listByInvoice(invoiceId); }
    @PostMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'PAYMENT_WRITE')")
    public ReceiptDtos.Response issue(@PathVariable Long invoiceId, @Valid @RequestBody ReceiptDtos.CreateRequest request) { return service.issue(invoiceId, request, SecurityActor.currentActor()); }
}
