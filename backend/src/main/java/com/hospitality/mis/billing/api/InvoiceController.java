package com.hospitality.mis.billing.api;

import com.hospitality.mis.billing.application.BillingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hospitality.mis.security.SecurityActor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final BillingService service;
    public InvoiceController(BillingService service) { this.service = service; }
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTING', 'FRONT_DESK')")
    @GetMapping("/reservation/{reservationId}")
    public InvoiceDtos.Response getByReservation(@PathVariable Long reservationId) { return service.getByReservation(reservationId); }
    public record AdjustmentRequest(@NotNull BigDecimal delta, @NotBlank String reason) {}
    @PreAuthorize("hasAnyRole('MANAGER','ACCOUNTING')")
    @PostMapping("/reservation/{reservationId}/deposit/refund")
    public InvoiceDtos.Response refundDeposit(@PathVariable Long reservationId) { return service.refundDeposit(reservationId, SecurityActor.currentActor()); }
    @PreAuthorize("hasAnyRole('MANAGER','ACCOUNTING')")
    @PostMapping("/{invoiceId}/adjust")
    public InvoiceDtos.Response adjust(@PathVariable Long invoiceId, @Valid @RequestBody AdjustmentRequest request) {
        return service.adjust(invoiceId, request.delta(), request.reason(), SecurityActor.currentActor());
    }
}
