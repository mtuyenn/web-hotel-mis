ALTER TABLE services
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_services_active_name ON services (active, name);
