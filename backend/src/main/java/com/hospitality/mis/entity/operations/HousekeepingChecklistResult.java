package com.hospitality.mis.entity.operations;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "housekeeping_checklist_results")
public class HousekeepingChecklistResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "task_id", nullable = false) private HousekeepingTask task;
    @Column(nullable = false, length = 200) private String item;
    @Column(nullable = false) private boolean passed;
    @Column(length = 500) private String note;
    @Column(name = "completed_by", nullable = false, length = 10) private String completedBy;
    @Column(name = "completed_at", nullable = false) private LocalDateTime completedAt;
    public Long getId() { return id; } public HousekeepingTask getTask() { return task; } public String getItem() { return item; } public boolean isPassed() { return passed; }
    public String getNote() { return note; } public String getCompletedBy() { return completedBy; } public LocalDateTime getCompletedAt() { return completedAt; }
    public void setTask(HousekeepingTask v) { task = v; } public void setItem(String v) { item = v; } public void setPassed(boolean v) { passed = v; }
    public void setNote(String v) { note = v; } public void setCompletedBy(String v) { completedBy = v; } public void setCompletedAt(LocalDateTime v) { completedAt = v; }
}
