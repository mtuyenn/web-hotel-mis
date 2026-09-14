ALTER TABLE room_types
    ADD COLUMN catalog_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN catalog_updated_by VARCHAR(50),
    ADD COLUMN catalog_approved_by VARCHAR(50),
    ADD COLUMN catalog_updated_at DATETIME(6),
    ADD COLUMN catalog_approved_at DATETIME(6);

ALTER TABLE room_types
    ADD CONSTRAINT chk_room_types_catalog_status
    CHECK (catalog_status IN ('DRAFT', 'ACTIVE', 'REJECTED'));

CREATE INDEX idx_room_types_catalog_status ON room_types (catalog_status, id);
