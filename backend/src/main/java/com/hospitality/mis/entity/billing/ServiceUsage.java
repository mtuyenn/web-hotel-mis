package com.hospitality.mis.entity.billing;

import com.hospitality.mis.entity.reservation.Reservation;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One service usage line attached to a reservation.
 *
 * <p>The composite key consists of the reservation, service, and usage date.
 * The unit price is captured at usage time so invoice history remains stable
 * when the service catalog changes later.</p>
 */
@Entity
@Table(name = "service_usages")
@IdClass(ServiceUsageId.class)
@Access(AccessType.FIELD)
public class ServiceUsage {
    @Id @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reservation_id")
    private Reservation reservation;
    @Id @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_id")
    private Service service;
    @Id @Column(name = "used_on") private LocalDate usedOn;
    @Column(name = "quantity", nullable = false) private Integer quantity = 0;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2) private BigDecimal unitPrice;

    public ServiceUsage() {}
    public ServiceUsage(Reservation reservation, Service service, LocalDate usedOn, Integer quantity) {
        this.reservation = reservation; this.service = service; this.usedOn = usedOn;
        this.quantity = quantity; this.unitPrice = service.getPrice();
    }
    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation value) { reservation = value; }
    public Service getService() { return service; }
    public void setService(Service value) { service = value; }
    public LocalDate getUsedOn() { return usedOn; }
    public void setUsedOn(LocalDate value) { usedOn = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { unitPrice = value; }
}
