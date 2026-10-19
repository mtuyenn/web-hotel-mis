package com.hospitality.mis.service.finance;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.finance.FinanceBackofficeDatabase;
import com.hospitality.mis.dto.finance.FinanceBackofficeDtos;
import com.hospitality.mis.persistence.VietnameseCodeConverters;
import com.hospitality.mis.service.governance.AuditService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FinanceBackofficeService {
    private static final BigDecimal VAT_RATE = new BigDecimal("8.00");
    private static final VietnameseCodeConverters.OtaReconciliationStatusConverter OTA_STATUS = new VietnameseCodeConverters.OtaReconciliationStatusConverter();
    private static final VietnameseCodeConverters.VatCustomerTypeConverter VAT_CUSTOMER_TYPE = new VietnameseCodeConverters.VatCustomerTypeConverter();
    private final FinanceBackofficeDatabase database;
    private final AuditService audit;

    public FinanceBackofficeService(FinanceBackofficeDatabase database, AuditService audit) {
        this.database = database;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<FinanceBackofficeDtos.OtaReconciliationResponse> ota() {
        return database.ota();
    }

    @Transactional
    public FinanceBackofficeDtos.OtaReconciliationResponse updateOtaStatus(long reservationId, String status) {
        if(!java.util.Set.of("PENDING","MATCHED","DISPUTED").contains(status))
            throw new DomainException("INVALID_OTA_STATUS","Trạng thái đối soát OTA không hợp lệ");
        var before=ota().stream().filter(row->row.reservationId().equals(reservationId)).findFirst()
            .orElseThrow(()->new DomainException("OTA_RESERVATION_NOT_FOUND","Không tìm thấy booking OTA"));
        if(before.status().equals(status))return before;
        database.updateOta(reservationId,OTA_STATUS.convertToDatabaseColumn(code(status)));
        audit.record(com.hospitality.mis.middleware.security.SecurityActor.currentActor(),"OTA_RECONCILIATION_UPDATED","OTA_RESERVATION",String.valueOf(reservationId),before.status(),status,null);
        return ota().stream().filter(row -> row.reservationId().equals(reservationId)).findFirst()
                .orElseThrow(() -> new DomainException("OTA_RESERVATION_NOT_FOUND", "Không tìm thấy booking OTA"));
    }

    @Transactional(readOnly = true)
    public List<FinanceBackofficeDtos.VatInvoiceResponse> vatInvoices() {
        return database.vatInvoices();
    }

    @Transactional
    public FinanceBackofficeDtos.VatInvoiceResponse createVat(FinanceBackofficeDtos.VatInvoiceRequest request, String actor) {
        String principal=com.hospitality.mis.middleware.security.SecurityActor.requireBoundActor(actor);
        if("COMPANY".equals(request.customerType())&&(request.taxCode()==null||request.taxCode().isBlank()||request.companyName()==null||request.companyName().isBlank()||request.companyAddress()==null||request.companyAddress().isBlank()))
            throw new DomainException("VAT_COMPANY_DETAILS_REQUIRED","Hóa đơn doanh nghiệp cần mã số thuế, tên và địa chỉ công ty");
        String number = "VAT-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        long id = database.createVat(request,number,VAT_RATE,VAT_CUSTOMER_TYPE.convertToDatabaseColumn(code(request.customerType())),principal);
        audit.record(principal,"VAT_INVOICE_ISSUED","VAT_INVOICE",String.valueOf(id),null,String.valueOf(request.invoiceId()),null);
        return vatInvoices().stream().filter(row -> row.id().equals(id)).findFirst().orElseThrow();
    }

    public String exportVatXml(long id) {
        var row = database.vatInvoice(id).stream().findFirst().orElse(null);
        if (row == null) throw new DomainException("VAT_NOT_FOUND", "Không tìm thấy hóa đơn VAT");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Invoice><InvoiceNumber>" + esc(row.vatInvoiceNumber()) + "</InvoiceNumber>" +
                "<TaxRate>" + row.taxRate() + "</TaxRate><TaxableAmount>" + row.taxableAmount() + "</TaxableAmount>" +
                "<TaxAmount>" + row.taxAmount() + "</TaxAmount><TotalAmount>" + row.totalAmount() + "</TotalAmount>" +
                "<Customer><Type>" + esc(row.customerType()) + "</Type><Name>" + esc(row.customerName()) + "</Name>" +
                "<TaxCode>" + esc(row.taxCode()) + "</TaxCode><CompanyName>" + esc(row.companyName()) + "</CompanyName><Address>" + esc(row.companyAddress()) + "</Address></Customer></Invoice>";
        database.saveVatXml(id,xml);
        return xml;
    }

    private static String code(String value) { return value == null ? null : value.trim().toUpperCase(java.util.Locale.ROOT); }
    private static String esc(String value) { if (value == null) return ""; return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;"); }
}
