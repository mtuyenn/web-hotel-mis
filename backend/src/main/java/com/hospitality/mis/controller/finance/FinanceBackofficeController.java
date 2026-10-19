package com.hospitality.mis.controller.finance;

import com.hospitality.mis.dto.finance.FinanceBackofficeDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.finance.FinanceBackofficeService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Financial back-office endpoints: OTA reconciliation and VAT documents. */
@RestController
public class FinanceBackofficeController {
    private final FinanceBackofficeService service;
    public FinanceBackofficeController(FinanceBackofficeService service) { this.service = service; }

    @GetMapping("/api/finance/ota-reconciliation")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')")
    public java.util.List<FinanceBackofficeDtos.OtaReconciliationResponse> ota() { return service.ota(); }

    @PatchMapping("/api/finance/ota-reconciliation/{reservationId}")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')")
    public FinanceBackofficeDtos.OtaReconciliationResponse otaStatus(@PathVariable long reservationId, @Valid @RequestBody FinanceBackofficeDtos.OtaStatusRequest request) { return service.updateOtaStatus(reservationId, request.status()); }

    @GetMapping("/api/finance/vat-invoices")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')")
    public java.util.List<FinanceBackofficeDtos.VatInvoiceResponse> vat() { return service.vatInvoices(); }

    @PostMapping("/api/finance/vat-invoices")
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_WRITE')")
    public FinanceBackofficeDtos.VatInvoiceResponse createVat(@Valid @RequestBody FinanceBackofficeDtos.VatInvoiceRequest request) { return service.createVat(request, SecurityActor.currentActor()); }

    @GetMapping(value = "/api/finance/vat-invoices/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("@departmentAccess.allows(authentication, 'FINANCE_READ')")
    public ResponseEntity<String> vatXml(@PathVariable long id) { return ResponseEntity.ok(service.exportVatXml(id)); }
}
