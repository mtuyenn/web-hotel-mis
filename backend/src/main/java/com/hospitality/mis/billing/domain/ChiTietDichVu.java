package com.hospitality.mis.billing.domain;
import com.hospitality.mis.reservation.domain.Reservation;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="service_usages") @IdClass(ChiTietDichVuId.class) @Access(AccessType.FIELD)
public class ChiTietDichVu {
    @Id @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="reservation_id") private Reservation reservation;
    @Id @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="service_id") private Service service;
    @Id @Column(name="used_on") private LocalDate usedOn;
    @Column(name="quantity", nullable=false) private Integer quantity=0;
    public ChiTietDichVu() {}
    public ChiTietDichVu(Reservation r, Service s, LocalDate d, Integer q){reservation=r;service=s;usedOn=d;quantity=q;}
    public Reservation getReservation(){return reservation;} public void setReservation(Reservation v){reservation=v;}
    public Service getService(){return service;} public void setService(Service v){service=v;}
    public LocalDate getUsedOn(){return usedOn;} public void setUsedOn(LocalDate v){usedOn=v;}
    public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
}
