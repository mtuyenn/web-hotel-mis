package com.hospitality.mis.entity.operations;

import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomEquipment;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "technical_work_orders")
public class TechnicalWorkOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id", nullable = false) private Room room;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "equipment_id") private RoomEquipment equipment;
    @Column(length = 10) private String assignee;
    @Column(nullable = false, length = 20) private String priority = "MEDIUM";
    @Column(name = "sla_due_at") private LocalDateTime slaDueAt;
    @Column(length = 1000) private String materials;
    @Column(name = "result_note", length = 1000) private String resultNote;
    @Column(name = "acceptance_note", length = 1000) private String acceptanceNote;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TechnicalWorkOrderStatus status = TechnicalWorkOrderStatus.NEW;
    @Column(name = "created_by", nullable = false, length = 10) private String createdBy;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; } public Room getRoom() { return room; } public RoomEquipment getEquipment() { return equipment; }
    public String getAssignee() { return assignee; } public String getPriority() { return priority; } public LocalDateTime getSlaDueAt() { return slaDueAt; }
    public String getMaterials() { return materials; } public String getResultNote() { return resultNote; } public String getAcceptanceNote() { return acceptanceNote; }
    public TechnicalWorkOrderStatus getStatus() { return status; } public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setRoom(Room value) { room = value; } public void setEquipment(RoomEquipment value) { equipment = value; }
    public void setAssignee(String value) { assignee = value; } public void setPriority(String value) { priority = value; }
    public void setSlaDueAt(LocalDateTime value) { slaDueAt = value; } public void setMaterials(String value) { materials = value; }
    public void setResultNote(String value) { resultNote = value; } public void setAcceptanceNote(String value) { acceptanceNote = value; }
    public void setStatus(TechnicalWorkOrderStatus value) { status = value; } public void setCreatedBy(String value) { createdBy = value; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
