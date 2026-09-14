package com.hospitality.mis.entity.operations;

import com.hospitality.mis.entity.room.Room;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Task dọn phòng có trạng thái, người phụ trách và cờ checklist/blocking rõ ràng. */
@Entity
@Table(name = "housekeeping_tasks")
public class HousekeepingTask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    @Column(length = 10) private String assignee;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private HousekeepingTaskStatus status = HousekeepingTaskStatus.NEEDS_CLEANING;
    @Column(name = "checklist_complete", nullable = false) private boolean checklistComplete;
    @Column(name = "blocking_incident", nullable = false) private boolean blockingIncident;
    @Column(length = 500) private String note;
    @Column(name = "assigned_by", length = 10) private String assignedBy;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Room getRoom() { return room; }
    public String getAssignee() { return assignee; }
    public HousekeepingTaskStatus getStatus() { return status; }
    public boolean isChecklistComplete() { return checklistComplete; }
    public boolean isBlockingIncident() { return blockingIncident; }
    public String getNote() { return note; }
    public String getAssignedBy() { return assignedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setRoom(Room room) { this.room = room; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public void setStatus(HousekeepingTaskStatus status) { this.status = status; }
    public void setChecklistComplete(boolean value) { checklistComplete = value; }
    public void setBlockingIncident(boolean value) { blockingIncident = value; }
    public void setNote(String note) { this.note = note; }
    public void setAssignedBy(String value) { assignedBy = value; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
