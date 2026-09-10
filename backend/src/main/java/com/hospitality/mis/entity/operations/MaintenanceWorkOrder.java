package com.hospitality.mis.entity.operations;

import com.hospitality.mis.entity.room.Room;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * JPA entity representing a room maintenance work order.
 *
 * <p>Java property names use English so the schema and other modules share one
 * vocabulary. Business-facing translations belong in the presentation layer,
 * not in the domain model.</p>
 */
@Entity
@Table(name = "maintenance_work_orders")
public class MaintenanceWorkOrder {
    @Id
    @Column(name = "id", length = 10)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "maintenance_type", nullable = false)
    private String maintenanceType;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status = MaintenanceStatus.CHUA_XU_LY;

    @Column(name = "description")
    private String description;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public MaintenanceStatus getStatus() { return status; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
