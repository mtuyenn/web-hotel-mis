CREATE TABLE notification_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    topic VARCHAR(100) NOT NULL,
    recipient_role VARCHAR(30) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    dedupe_key VARCHAR(150) NOT NULL,
    available_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    delivered_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_notification_outbox_dedupe UNIQUE (dedupe_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_notification_outbox_poll ON notification_outbox (status, available_at, id);
