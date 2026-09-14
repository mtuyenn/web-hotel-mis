CREATE TABLE room_type_price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_type_id VARCHAR(10) NOT NULL,
    daily_price DECIMAL(12,2) NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    approval_id BIGINT,
    effective_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_type_price_history_type FOREIGN KEY (room_type_id) REFERENCES room_types (id),
    CONSTRAINT fk_room_type_price_history_approval FOREIGN KEY (approval_id) REFERENCES approval_requests (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_room_type_price_history_type_time
    ON room_type_price_history (room_type_id, effective_at, id);
