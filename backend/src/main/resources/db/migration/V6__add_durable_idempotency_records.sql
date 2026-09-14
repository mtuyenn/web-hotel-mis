CREATE TABLE idempotency_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    command_scope VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_type VARCHAR(255),
    response_json LONGTEXT,
    created_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6),
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_idempotency_scope_key UNIQUE (command_scope, idempotency_key)
);

CREATE INDEX idx_idempotency_created_at ON idempotency_records (created_at);

CREATE TABLE idempotency_lock_buckets (
    bucket_id SMALLINT NOT NULL,
    PRIMARY KEY (bucket_id)
);

INSERT INTO idempotency_lock_buckets (bucket_id) VALUES
    (0),(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),
    (16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31),
    (32),(33),(34),(35),(36),(37),(38),(39),(40),(41),(42),(43),(44),(45),(46),(47),
    (48),(49),(50),(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),(61),(62),(63);
