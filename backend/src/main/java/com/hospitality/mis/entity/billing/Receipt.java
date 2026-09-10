package com.hospitality.mis.entity.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "receipts")
public class Receipt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "receipt_number", nullable = false, unique = true, length = 40) private String receiptNumber;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id", nullable = false) private Invoice invoice;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private PaymentMethod method;
    @Column(name = "issued_at", nullable = false) private LocalDateTime issuedAt;
    @Column(name = "issued_by", nullable = false, length = 50) private String issuedBy;
    public Long getId() { return id; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String value) { receiptNumber = value; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice value) { invoice = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod value) { method = value; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime value) { issuedAt = value; }
    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String value) { issuedBy = value; }
}
