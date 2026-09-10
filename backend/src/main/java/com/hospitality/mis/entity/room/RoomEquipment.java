package com.hospitality.mis.entity.room;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "room_equipment")
public class RoomEquipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "room_id", nullable = false) private Room room;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "original_value", nullable = false, precision = 14, scale = 2) private BigDecimal originalValue;
    @Column(name = "purchased_on", nullable = false) private LocalDate purchasedOn;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false) private boolean active = true;
    public Long getId() { return id; } public Room getRoom() { return room; } public void setRoom(Room v) { room = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public BigDecimal getOriginalValue() { return originalValue; } public void setOriginalValue(BigDecimal v) { originalValue = v; }
    public LocalDate getPurchasedOn() { return purchasedOn; } public void setPurchasedOn(LocalDate v) { purchasedOn = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { quantity = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
}
