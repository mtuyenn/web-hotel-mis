ALTER TABLE reservations
    ADD COLUMN deposit_payment_code VARCHAR(40),
    ADD COLUMN deposit_payment_expires_at DATETIME(6),
    ADD COLUMN deposit_payment_status VARCHAR(20) NOT NULL DEFAULT 'NOT_REQUIRED';

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_customer_account
    FOREIGN KEY (customer_account_id) REFERENCES customer_accounts (id);

CREATE UNIQUE INDEX uk_reservations_deposit_payment_code
    ON reservations (deposit_payment_code);

CREATE INDEX idx_reservations_customer_booked_at
    ON reservations (customer_account_id, booked_at);
