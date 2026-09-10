package com.hospitality.mis.entity.operations;

import com.hospitality.mis.entity.billing.Service;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "inventory_movements")
public class InventoryMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "service_id", nullable = false) private Service service;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private MovementType type;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false, length = 50) private String actorId;
    @Column(nullable = false) private LocalDateTime occurredAt;
    @Column(length = 255) private String reason;
    public enum MovementType { RECEIPT, ISSUE, ADJUSTMENT }
    public Long getId() { return id; } public Service getService() { return service; } public void setService(Service v) { service = v; }
    public MovementType getType() { return type; } public void setType(MovementType v) { type = v; }
    public int getQuantity() { return quantity; } public void setQuantity(int v) { quantity = v; }
    public String getActorId() { return actorId; } public void setActorId(String v) { actorId = v; }
    public LocalDateTime getOccurredAt() { return occurredAt; } public void setOccurredAt(LocalDateTime v) { occurredAt = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
}
