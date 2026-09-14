CREATE TABLE housekeeping_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id VARCHAR(10) NOT NULL,
    assignee VARCHAR(10),
    status VARCHAR(30) NOT NULL,
    checklist_complete BOOLEAN NOT NULL DEFAULT FALSE,
    blocking_incident BOOLEAN NOT NULL DEFAULT FALSE,
    note VARCHAR(500),
    assigned_by VARCHAR(10),
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_housekeeping_task_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_housekeeping_task_status CHECK (status IN ('NEEDS_CLEANING','IN_PROGRESS','CLEANED','READY','WAITING_TECHNICAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_housekeeping_task_room_status ON housekeeping_tasks (room_id, status);
CREATE INDEX idx_housekeeping_task_assignee_status ON housekeeping_tasks (assignee, status);
