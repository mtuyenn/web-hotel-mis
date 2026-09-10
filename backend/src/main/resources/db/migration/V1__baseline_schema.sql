-- Clean, deterministic schema for the web hotel MIS.
-- This is a hard cut: the database must be empty before Flyway runs V1.
-- Do not add compatibility aliases, conditional DDL, or legacy table names here.

CREATE TABLE room_types (
    id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    daily_price DECIMAL(12, 2) NOT NULL,
    description VARCHAR(500),
    CONSTRAINT pk_room_types PRIMARY KEY (id),
    CONSTRAINT chk_room_types_daily_price CHECK (daily_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE rooms (
    id VARCHAR(10) NOT NULL,
    room_type_id VARCHAR(10) NOT NULL,
    name VARCHAR(100),
    floor INT,
    description VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'SAN_SANG',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_rooms PRIMARY KEY (id),
    CONSTRAINT fk_rooms_room_type FOREIGN KEY (room_type_id) REFERENCES room_types (id),
    CONSTRAINT chk_rooms_floor CHECK (floor IS NULL OR floor >= 0),
    CONSTRAINT chk_rooms_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_rooms_room_type ON rooms (room_type_id);
CREATE INDEX idx_rooms_status ON rooms (status);

CREATE TABLE guests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    identity_number VARCHAR(12) NOT NULL,
    birth_year INT,
    membership_tier VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    total_spend DECIMAL(14, 2) NOT NULL DEFAULT 0,
    late_cancellation_count INT NOT NULL DEFAULT 0,
    completed_stays INT NOT NULL DEFAULT 0,
    late_checkout_count INT NOT NULL DEFAULT 0,
    booking_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_guests PRIMARY KEY (id),
    CONSTRAINT uk_guests_phone UNIQUE (phone),
    CONSTRAINT uk_guests_email UNIQUE (email),
    CONSTRAINT uk_guests_identity_number UNIQUE (identity_number),
    CONSTRAINT chk_guests_birth_year CHECK (birth_year IS NULL OR birth_year BETWEEN 1900 AND 2100),
    CONSTRAINT chk_guests_total_spend CHECK (total_spend >= 0),
    CONSTRAINT chk_guests_late_cancellations CHECK (late_cancellation_count >= 0),
    CONSTRAINT chk_guests_completed_stays CHECK (completed_stays >= 0),
    CONSTRAINT chk_guests_late_checkouts CHECK (late_checkout_count >= 0),
    CONSTRAINT chk_guests_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_guests_search ON guests (full_name, phone, identity_number);

CREATE TABLE employees (
    id VARCHAR(10) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    position VARCHAR(30) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(15) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_failed_login_at DATETIME(6),
    last_login_at DATETIME(6),
    CONSTRAINT pk_employees PRIMARY KEY (id),
    CONSTRAINT uk_employees_phone UNIQUE (phone),
    CONSTRAINT chk_employees_failed_login_attempts CHECK (failed_login_attempts >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    guest_id BIGINT NOT NULL,
    employee_id VARCHAR(10),
    customer_account_id BIGINT,
    booked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deposit_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    rental_type VARCHAR(20) NOT NULL DEFAULT 'PACKAGE',
    actual_check_in DATETIME,
    actual_check_out DATETIME,
    extension_minutes INT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_reservations PRIMARY KEY (id),
    CONSTRAINT uk_reservations_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_reservations_guest FOREIGN KEY (guest_id) REFERENCES guests (id),
    CONSTRAINT fk_reservations_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT chk_reservations_deposit CHECK (deposit_amount >= 0),
    CONSTRAINT chk_reservations_extension CHECK (extension_minutes >= 0),
    CONSTRAINT chk_reservations_version CHECK (version >= 0),
    CONSTRAINT chk_reservations_actual_times CHECK (
        actual_check_in IS NULL OR actual_check_out IS NULL OR actual_check_out >= actual_check_in
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_reservations_guest_booked_at ON reservations (guest_id, booked_at);
CREATE INDEX idx_reservations_employee_booked_at ON reservations (employee_id, booked_at);
CREATE INDEX idx_reservations_status ON reservations (status);

CREATE TABLE reservation_rooms (
    reservation_id BIGINT NOT NULL,
    room_id VARCHAR(10) NOT NULL,
    check_in DATETIME NOT NULL,
    check_out DATETIME NOT NULL,
    original_check_out DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DA_DAT',
    transfer_count INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_reservation_rooms PRIMARY KEY (reservation_id, room_id),
    CONSTRAINT fk_reservation_rooms_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_rooms_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_reservation_rooms_times CHECK (check_out > check_in),
    CONSTRAINT chk_reservation_rooms_original_checkout CHECK (original_check_out >= check_in AND check_out >= original_check_out),
    CONSTRAINT chk_reservation_rooms_transfer_count CHECK (transfer_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_reservation_rooms_room_time ON reservation_rooms (room_id, check_in, check_out);

CREATE TABLE room_transfers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    from_room_id VARCHAR(10) NOT NULL,
    to_room_id VARCHAR(10) NOT NULL,
    transferred_at DATETIME,
    reason VARCHAR(255),
    CONSTRAINT pk_room_transfers PRIMARY KEY (id),
    CONSTRAINT fk_room_transfers_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_room_transfers_from_room FOREIGN KEY (from_room_id) REFERENCES rooms (id),
    CONSTRAINT fk_room_transfers_to_room FOREIGN KEY (to_room_id) REFERENCES rooms (id),
    CONSTRAINT chk_room_transfers_distinct_rooms CHECK (from_room_id <> to_room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_room_transfers_reservation ON room_transfers (reservation_id);
CREATE INDEX idx_room_transfers_from_room ON room_transfers (from_room_id);
CREATE INDEX idx_room_transfers_to_room ON room_transfers (to_room_id);

CREATE TABLE services (
    id VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'TIME',
    stock_quantity INT NOT NULL DEFAULT 0,
    safety_threshold INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_services PRIMARY KEY (id),
    CONSTRAINT chk_services_price CHECK (price >= 0),
    CONSTRAINT chk_services_stock_quantity CHECK (stock_quantity >= 0),
    CONSTRAINT chk_services_safety_threshold CHECK (safety_threshold >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE service_usages (
    reservation_id BIGINT NOT NULL,
    service_id VARCHAR(10) NOT NULL,
    used_on DATE NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12, 2) NOT NULL,
    CONSTRAINT pk_service_usages PRIMARY KEY (reservation_id, service_id, used_on),
    CONSTRAINT fk_service_usages_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_service_usages_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT chk_service_usages_quantity CHECK (quantity > 0),
    CONSTRAINT chk_service_usages_unit_price CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_service_usages_service_date ON service_usages (service_id, used_on);

CREATE TABLE invoices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    deposit_paid DECIMAL(12, 2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    room_total DECIMAL(12, 2) NOT NULL DEFAULT 0,
    service_total DECIMAL(12, 2) NOT NULL DEFAULT 0,
    amount_due DECIMAL(12, 2) NOT NULL DEFAULT 0,
    surcharge DECIMAL(12, 2) NOT NULL DEFAULT 0,
    compensation DECIMAL(12, 2) NOT NULL DEFAULT 0,
    extension_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    adjustment_total DECIMAL(12, 2) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_invoices PRIMARY KEY (id),
    CONSTRAINT uk_invoices_reservation UNIQUE (reservation_id),
    CONSTRAINT fk_invoices_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT chk_invoices_discount CHECK (discount >= 0),
    CONSTRAINT chk_invoices_deposit_paid CHECK (deposit_paid >= 0),
    CONSTRAINT chk_invoices_room_total CHECK (room_total >= 0),
    CONSTRAINT chk_invoices_service_total CHECK (service_total >= 0),
    CONSTRAINT chk_invoices_amount_due CHECK (amount_due >= 0),
    CONSTRAINT chk_invoices_surcharge CHECK (surcharge >= 0),
    CONSTRAINT chk_invoices_compensation CHECK (compensation >= 0),
    CONSTRAINT chk_invoices_extension_fee CHECK (extension_fee >= 0),
    CONSTRAINT chk_invoices_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_invoices_status_issued_at ON invoices (status, issued_at);

CREATE TABLE invoice_adjustments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    delta DECIMAL(12, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    actor_id VARCHAR(50) NOT NULL,
    occurred_at DATETIME NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    CONSTRAINT pk_invoice_adjustments PRIMARY KEY (id),
    CONSTRAINT uk_invoice_adjustments_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_invoice_adjustments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    CONSTRAINT chk_invoice_adjustments_delta CHECK (delta <> 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_invoice_adjustments_invoice ON invoice_adjustments (invoice_id, occurred_at);

CREATE TABLE maintenance_work_orders (
    id VARCHAR(10) NOT NULL,
    room_id VARCHAR(10) NOT NULL,
    maintenance_type VARCHAR(100) NOT NULL,
    scheduled_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CHUA_XU_LY',
    description VARCHAR(255),
    CONSTRAINT pk_maintenance_work_orders PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_work_orders_room FOREIGN KEY (room_id) REFERENCES rooms (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_maintenance_work_orders_room_status ON maintenance_work_orders (room_id, status);

CREATE TABLE equipment_incidents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    room_id VARCHAR(10) NOT NULL,
    equipment_name VARCHAR(100) NOT NULL,
    original_value DECIMAL(14, 2) NOT NULL,
    purchased_on DATE NOT NULL,
    quantity INT NOT NULL,
    compensation DECIMAL(14, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_equipment_incidents PRIMARY KEY (id),
    CONSTRAINT fk_equipment_incidents_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
    CONSTRAINT fk_equipment_incidents_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_equipment_incidents_original_value CHECK (original_value >= 0),
    CONSTRAINT chk_equipment_incidents_quantity CHECK (quantity > 0),
    CONSTRAINT chk_equipment_incidents_compensation CHECK (compensation >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_equipment_incidents_reservation ON equipment_incidents (reservation_id);
CREATE INDEX idx_equipment_incidents_room ON equipment_incidents (room_id);

CREATE TABLE approval_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requester VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    mutation_payload TEXT NOT NULL,
    payload_fingerprint VARCHAR(64) NOT NULL,
    amount DECIMAL(19, 4),
    correlation_key VARCHAR(100),
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approver VARCHAR(50),
    decided_at TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP NULL,
    CONSTRAINT pk_approval_requests PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_approval_requests_status ON approval_requests (status);
CREATE INDEX idx_approval_requests_target ON approval_requests (target_id);

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    before_data TEXT,
    after_data TEXT,
    reason VARCHAR(500),
    correlation_key VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_logs_actor_time ON audit_logs (actor, created_at);
CREATE INDEX idx_audit_logs_action_time ON audit_logs (action, created_at);

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id VARCHAR(10),
    customer_account_id BIGINT,
    family_id VARCHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME,
    replaced_by_hash VARCHAR(64),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT chk_refresh_tokens_owner CHECK ((employee_id IS NOT NULL AND customer_account_id IS NULL) OR (employee_id IS NULL AND customer_account_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_refresh_tokens_employee ON refresh_tokens (employee_id);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_revoked_expiry ON refresh_tokens (revoked_at, expires_at);
CREATE INDEX idx_refresh_tokens_customer ON refresh_tokens (customer_account_id);

CREATE TABLE customer_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    guest_id BIGINT NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_customer_accounts PRIMARY KEY (id),
    CONSTRAINT uk_customer_accounts_guest UNIQUE (guest_id),
    CONSTRAINT uk_customer_accounts_phone UNIQUE (phone),
    CONSTRAINT fk_customer_accounts_guest FOREIGN KEY (guest_id) REFERENCES guests (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(30) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    reference VARCHAR(100),
    occurred_at DATETIME NOT NULL,
    actor_id VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(100),
    CONSTRAINT pk_payment_transactions PRIMARY KEY (id),
    CONSTRAINT fk_payment_transactions_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    CONSTRAINT chk_payment_transactions_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uk_payment_transactions_idempotency_key ON payment_transactions (idempotency_key);

CREATE TABLE receipts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    receipt_number VARCHAR(40) NOT NULL,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(30) NOT NULL,
    issued_at DATETIME NOT NULL,
    issued_by VARCHAR(50) NOT NULL,
    CONSTRAINT pk_receipts PRIMARY KEY (id),
    CONSTRAINT uk_receipts_number UNIQUE (receipt_number),
    CONSTRAINT fk_receipts_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    CONSTRAINT chk_receipts_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cash_shift_handovers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shift_code VARCHAR(30) NOT NULL,
    from_actor VARCHAR(50) NOT NULL,
    to_actor VARCHAR(50) NOT NULL,
    expected_amount DECIMAL(14,2) NOT NULL,
    actual_amount DECIMAL(14,2) NOT NULL,
    variance DECIMAL(14,2) NOT NULL,
    handed_over_at DATETIME NOT NULL,
    note VARCHAR(500),
    CONSTRAINT pk_cash_shift_handovers PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    paid_by VARCHAR(50) NOT NULL,
    paid_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECORDED',
    CONSTRAINT pk_expenses PRIMARY KEY (id),
    CONSTRAINT chk_expenses_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE partner_debts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partner_name VARCHAR(150) NOT NULL,
    reference_code VARCHAR(80) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    settled_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    recorded_at DATETIME NOT NULL,
    CONSTRAINT pk_partner_debts PRIMARY KEY (id),
    CONSTRAINT uk_partner_debts_reference UNIQUE (reference_code),
    CONSTRAINT chk_partner_debts_amount CHECK (amount >= 0),
    CONSTRAINT chk_partner_debts_settled CHECK (settled_amount >= 0 AND settled_amount <= amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    service_id VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    actor_id VARCHAR(50) NOT NULL,
    occurred_at DATETIME NOT NULL,
    reason VARCHAR(255),
    CONSTRAINT pk_inventory_movements PRIMARY KEY (id),
    CONSTRAINT fk_inventory_movements_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT chk_inventory_movements_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE room_equipment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    original_value DECIMAL(14,2) NOT NULL,
    purchased_on DATE NOT NULL,
    quantity INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_room_equipment PRIMARY KEY (id),
    CONSTRAINT fk_room_equipment_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_room_equipment_value CHECK (original_value >= 0),
    CONSTRAINT chk_room_equipment_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE membership_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    guest_id BIGINT NOT NULL,
    from_tier VARCHAR(20) NOT NULL,
    to_tier VARCHAR(20) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    changed_at DATETIME NOT NULL,
    CONSTRAINT pk_membership_history PRIMARY KEY (id),
    CONSTRAINT fk_membership_history_guest FOREIGN KEY (guest_id) REFERENCES guests (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_payment_transactions_invoice ON payment_transactions (invoice_id, occurred_at);
CREATE INDEX idx_receipts_invoice ON receipts (invoice_id, issued_at);
CREATE INDEX idx_inventory_movements_service ON inventory_movements (service_id, occurred_at);
CREATE INDEX idx_room_equipment_room ON room_equipment (room_id, active);
CREATE INDEX idx_membership_history_guest ON membership_history (guest_id, changed_at);
