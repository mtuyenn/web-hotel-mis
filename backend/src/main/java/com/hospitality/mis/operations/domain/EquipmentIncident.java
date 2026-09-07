package com.hospitality.mis.operations.domain;

import com.hospitality.mis.reservation.domain.Reservation;
import com.hospitality.mis.room.domain.Room;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_incidents")
public class EquipmentIncident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reservation_id") private Reservation reservation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id") private Room room;
    @Column(name = "equipment_name", nullable = false, length = 100) private String equipmentName;
    @Column(name = "original_value", nullable = false, precision = 14, scale = 2) private BigDecimal originalValue;
    @Column(name = "purchased_on", nullable = false) private LocalDate purchasedAt;
    @Column(nullable = false) private int quantity;
    @Column(name = "compensation", nullable = false, precision = 14, scale = 2) private BigDecimal compensation;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    protected EquipmentIncident() {}
    public EquipmentIncident(Reservation reservation, Room room, String equipmentName, BigDecimal originalValue,
                             LocalDate purchasedAt, int quantity, BigDecimal compensation) {
        this.reservation = reservation; this.room = room; this.equipmentName = equipmentName;
        this.originalValue = originalValue; this.purchasedAt = purchasedAt; this.quantity = quantity; this.compensation = compensation;
    }
    public Long getId() { return id; }
    public BigDecimal getCompensation() { return compensation; }
}
