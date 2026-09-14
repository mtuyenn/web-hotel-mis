CREATE TABLE housekeeping_inspections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    inspection_type VARCHAR(20) NOT NULL,
    item VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    item_condition VARCHAR(20) NOT NULL,
    note VARCHAR(500),
    completed_by VARCHAR(10) NOT NULL,
    completed_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_housekeeping_inspection_task FOREIGN KEY (task_id) REFERENCES housekeeping_tasks(id),
    INDEX idx_housekeeping_inspection_task (task_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE equipment_incidents
    ADD COLUMN severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    ADD COLUMN handoff_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    ADD COLUMN handoff_note VARCHAR(500);
