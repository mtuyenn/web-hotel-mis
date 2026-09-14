ALTER TABLE technical_work_orders
    ADD COLUMN accepted_by VARCHAR(50) NULL AFTER acceptance_note,
    ADD COLUMN accepted_at DATETIME(6) NULL AFTER accepted_by;

ALTER TABLE approval_requests
    ADD COLUMN risk VARCHAR(20) NOT NULL DEFAULT 'LOW' AFTER reason,
    ADD COLUMN requested_at TIMESTAMP NULL AFTER risk;

UPDATE approval_requests
SET requested_at = DATE_SUB(expires_at, INTERVAL 24 HOUR)
WHERE requested_at IS NULL;

ALTER TABLE approval_requests
    MODIFY requested_at TIMESTAMP NOT NULL;

CREATE INDEX idx_approval_requests_queue
    ON approval_requests (status, risk, requested_at);

CREATE TABLE partner_debt_settlements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partner_debt_id BIGINT NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    settled_by VARCHAR(50) NOT NULL,
    settled_at DATETIME(6) NOT NULL,
    note VARCHAR(500),
    PRIMARY KEY (id),
    CONSTRAINT fk_partner_debt_settlement_debt FOREIGN KEY (partner_debt_id) REFERENCES partner_debts (id),
    CONSTRAINT chk_partner_debt_settlement_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_partner_debt_settlement_debt_time
    ON partner_debt_settlements (partner_debt_id, settled_at);

CREATE TABLE financial_ledger_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entry_type VARCHAR(40) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id VARCHAR(100) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    actor_id VARCHAR(50) NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    note VARCHAR(500),
    finalized BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT chk_financial_ledger_direction CHECK (direction IN ('DEBIT','CREDIT')),
    CONSTRAINT chk_financial_ledger_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_financial_ledger_time_type
    ON financial_ledger_entries (occurred_at, entry_type);
