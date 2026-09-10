package com.hospitality.mis.controller.billing;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.dto.billing.InvoiceDtos;




import com.hospitality.mis.service.billing.BillingService;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import com.hospitality.mis.middleware.security.SecurityActor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


@RestController

@RequestMapping("/api/invoices")

public class InvoiceController {
    private final BillingService service;

    public InvoiceController(BillingService service) { this.service = service; }



    @GetMapping("/reservation/{reservationId}")


    @PreAuthorize("@departmentAccess.allows(authentication, 'BILLING_READ')")
    public InvoiceDtos.Response getByReservation(@PathVariable Long reservationId) { return service.getByReservation(reservationId); }
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record AdjustmentRequest(@NotNull BigDecimal delta, @NotBlank String reason) {}

    @PostMapping("/reservation/{reservationId}/deposit/refund")

    @PreAuthorize("@departmentAccess.allows(authentication, 'PAYMENT_WRITE')")
    public InvoiceDtos.Response refundDeposit(@PathVariable Long reservationId) { return service.refundDeposit(reservationId, SecurityActor.currentActor()); }

    @PostMapping("/{invoiceId}/adjust")

    @PreAuthorize("@departmentAccess.allows(authentication, 'BILLING_WRITE')")
    public InvoiceDtos.Response adjust(@PathVariable Long invoiceId, @Valid @RequestBody AdjustmentRequest request,
                                       @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.adjust(invoiceId, request.delta(), request.reason(), SecurityActor.currentActor(), idempotencyKey);
    }
}
