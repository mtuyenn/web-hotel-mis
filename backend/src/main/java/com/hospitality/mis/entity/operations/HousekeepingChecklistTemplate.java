package com.hospitality.mis.entity.operations;
import jakarta.persistence.*;
@Entity @Table(name = "housekeeping_checklist_templates")
public class HousekeepingChecklistTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 100) private String name;
    @Column(nullable = false) private boolean active = true;
    public Long getId() { return id; } public String getName() { return name; } public boolean isActive() { return active; }
    public void setName(String v) { name = v; } public void setActive(boolean v) { active = v; }
}
