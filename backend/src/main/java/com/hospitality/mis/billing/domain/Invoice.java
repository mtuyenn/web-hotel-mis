package com.hospitality.mis.billing.domain;

import com.hospitality.mis.reservation.domain.Reservation;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Access(AccessType.FIELD)
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true) private Reservation reservation;
    @Column(name = "issued_at", nullable = false) private LocalDateTime issuedAt = LocalDateTime.now();
    @Column(name = "discount", precision = 12, scale = 2, nullable = false) private BigDecimal discount = BigDecimal.ZERO;
    @Column(name = "deposit_paid", precision = 12, scale = 2, nullable = false) private BigDecimal depositPaid = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(name = "payment_method") private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) private PaymentStatus status = PaymentStatus.CHUA_THANH_TOAN;
    @Column(name = "room_total", precision = 12, scale = 2, nullable = false) private BigDecimal roomTotal = BigDecimal.ZERO;
    @Column(name = "service_total", precision = 12, scale = 2, nullable = false) private BigDecimal serviceTotal = BigDecimal.ZERO;
    @Column(name = "amount_due", precision = 12, scale = 2, nullable = false) private BigDecimal amountDue = BigDecimal.ZERO;
    @Column(name = "surcharge", precision = 12, scale = 2, nullable = false) private BigDecimal surcharge = BigDecimal.ZERO;
    @Column(name = "compensation", precision = 12, scale = 2, nullable = false) private BigDecimal compensation = BigDecimal.ZERO;
    @Column(name = "extension_fee", precision = 12, scale = 2, nullable = false) private BigDecimal extensionFee = BigDecimal.ZERO;
    @Version @Column(name = "version", nullable = false) private long version;
    public Invoice() {}
    public Long getId() { return id; }
    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation value) { reservation = value; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime value) { issuedAt = value; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal value) { discount = value; }
    public BigDecimal getDepositPaid() { return depositPaid; }
    public void setDepositPaid(BigDecimal value) { depositPaid = value; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod value) { paymentMethod = value; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus value) { status = value; }
    public BigDecimal getRoomTotal() { return roomTotal; }
    public void setRoomTotal(BigDecimal value) { roomTotal = value; }
    public BigDecimal getServiceTotal() { return serviceTotal; }
    public void setServiceTotal(BigDecimal value) { serviceTotal = value; }
    public BigDecimal getAmountDue() { return amountDue; }
    public void setAmountDue(BigDecimal value) { amountDue = value; }
    public BigDecimal getSurcharge() { return surcharge; }
    public void setSurcharge(BigDecimal value) { surcharge = value; }
    public BigDecimal getCompensation() { return compensation; }
    public void setCompensation(BigDecimal value) { compensation = value; }
    public BigDecimal getExtensionFee() { return extensionFee; }
    public void setExtensionFee(BigDecimal value) { extensionFee = value; }
}
