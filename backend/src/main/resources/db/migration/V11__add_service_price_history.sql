CREATE TABLE service_price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_id VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    approval_id BIGINT,
    effective_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_service_price_history_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT fk_service_price_history_approval FOREIGN KEY (approval_id) REFERENCES approval_requests (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_service_price_history_service_time ON service_price_history (service_id, effective_at, id);
