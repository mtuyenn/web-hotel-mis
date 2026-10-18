package com.hospitality.mis.service.finance;

import com.hospitality.mis.dto.operations.BackofficeDtos;
import com.hospitality.mis.service.operations.BackofficeService;
import org.springframework.stereotype.Service;

import java.util.List;

/** Financial use cases exposed by the finance controller. */
@Service
public class FinanceBackofficeService {
    private final BackofficeService backoffice;

    public FinanceBackofficeService(BackofficeService backoffice) { this.backoffice = backoffice; }
    public List<BackofficeDtos.OtaReconciliationResponse> ota() { return backoffice.ota(); }
    public BackofficeDtos.OtaReconciliationResponse updateOtaStatus(long id, String status) { return backoffice.updateOtaStatus(id, status); }
    public List<BackofficeDtos.VatInvoiceResponse> vatInvoices() { return backoffice.vatInvoices(); }
    public BackofficeDtos.VatInvoiceResponse createVat(BackofficeDtos.VatInvoiceRequest request, String actor) { return backoffice.createVat(request, actor); }
    public String exportVatXml(long id) { return backoffice.exportVatXml(id); }
}
