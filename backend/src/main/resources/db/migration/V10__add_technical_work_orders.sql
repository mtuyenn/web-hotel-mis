CREATE TABLE technical_work_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id VARCHAR(10) NOT NULL,
    equipment_id BIGINT,
    assignee VARCHAR(10),
    priority VARCHAR(20) NOT NULL,
    sla_due_at DATETIME(6),
    materials VARCHAR(1000),
    result_note VARCHAR(1000),
    acceptance_note VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    created_by VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_technical_work_order_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_technical_work_order_equipment FOREIGN KEY (equipment_id) REFERENCES room_equipment (id),
    CONSTRAINT chk_technical_work_order_status CHECK (status IN ('NEW','ACKNOWLEDGED','IN_PROGRESS','WAITING_ACCEPTANCE','COMPLETED','ROOM_RELEASED')),
    CONSTRAINT chk_technical_work_order_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_technical_work_order_room_status ON technical_work_orders (room_id, status);
CREATE INDEX idx_technical_work_order_assignee_status ON technical_work_orders (assignee, status);
