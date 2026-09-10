package com.hospitality.mis.controller.billing;

import com.hospitality.mis.dto.billing.PaymentTransactionDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.billing.PaymentTransactionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/invoices/{invoiceId}/payments")
public class PaymentController {
    private final PaymentTransactionService service;
    public PaymentController(PaymentTransactionService service) { this.service = service; }
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'BILLING_READ')")
    public List<PaymentTransactionDtos.Response> list(@PathVariable Long invoiceId) { return service.listByInvoice(invoiceId); }
    @PostMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'PAYMENT_WRITE')")
    public PaymentTransactionDtos.Response record(@PathVariable Long invoiceId, @Valid @RequestBody PaymentTransactionDtos.CreateRequest request) { return service.record(invoiceId, request, SecurityActor.currentActor()); }
}
