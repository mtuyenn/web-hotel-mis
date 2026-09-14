ALTER TABLE payment_transactions
    ADD COLUMN external_event_id VARCHAR(100);

CREATE UNIQUE INDEX uk_payment_transactions_external_event
    ON payment_transactions (external_event_id);
