-- Manual demo reset. Run only after Flyway has completed V1.
-- This script never drops the database and intentionally does not seed employees.

START TRANSACTION;

DELETE FROM refresh_tokens;
DELETE FROM equipment_incidents;
DELETE FROM approval_requests;
DELETE FROM audit_logs;
DELETE FROM service_usages;
DELETE FROM invoices;
DELETE FROM room_transfers;
DELETE FROM reservation_rooms;
DELETE FROM reservations;
DELETE FROM maintenance_work_orders;
DELETE FROM services;
DELETE FROM rooms;
DELETE FROM guests;
DELETE FROM room_types;

ALTER TABLE guests AUTO_INCREMENT = 1;
ALTER TABLE reservations AUTO_INCREMENT = 1;
ALTER TABLE room_transfers AUTO_INCREMENT = 1;
ALTER TABLE invoices AUTO_INCREMENT = 1;
ALTER TABLE equipment_incidents AUTO_INCREMENT = 1;
ALTER TABLE approval_requests AUTO_INCREMENT = 1;
ALTER TABLE audit_logs AUTO_INCREMENT = 1;
ALTER TABLE refresh_tokens AUTO_INCREMENT = 1;

INSERT INTO room_types (id, name, daily_price, description) VALUES
('RT001', 'Phong Don', 500000.00, 'Phong 1 giuong don, view thanh pho'),
('RT002', 'Phong Doi', 800000.00, 'Phong 1 giuong doi lon'),
('RT003', 'Phong VIP', 1500000.00, 'Phong suite cao cap, view song'),
('RT004', 'Phong Gia Dinh', 1200000.00, '2 giuong doi, phu hop gia dinh');

INSERT INTO rooms (id, room_type_id, status, description, name, floor, version) VALUES
('R101', 'RT001', 'SAN_SANG', 'Tang 1 - Phong tieu chuan', '101', 1, 0),
('R102', 'RT001', 'SAN_SANG', 'Tang 1 - Phong tieu chuan', '102', 1, 0),
('R201', 'RT002', 'SAN_SANG', 'Tang 2 - Phong doi', '201', 2, 0),
('R202', 'RT002', 'DANG_DON_DEP', 'Tang 2 - Phong doi', '202', 2, 0),
('R301', 'RT003', 'SAN_SANG', 'Tang 3 - Phong VIP', '301', 3, 0),
('R302', 'RT003', 'BAO_TRI', 'Tang 3 - Phong VIP', '302', 3, 0),
('R401', 'RT004', 'SAN_SANG', 'Tang 4 - Phong gia dinh', '401', 4, 0),
('R402', 'RT004', 'SAN_SANG', 'Tang 4 - Phong gia dinh', '402', 4, 0);

INSERT INTO guests
(full_name, address, phone, email, identity_number, birth_year, membership_tier,
 total_spend, late_cancellation_count, booking_blocked, version)
VALUES
('Nguyen Van An', 'Quan 1, TP.HCM', '0901234567', 'an.nguyen@example.test', '012345678901',
 1990, 'STANDARD', 0.00, 0, FALSE, 0),
('Tran Thi Binh', 'Quan 3, TP.HCM', '0912345678', 'binh.tran@example.test', '098765432109',
 1988, 'VIP', 12000000.00, 0, FALSE, 0),
('Le Hoang Cuong', 'Quan 7, TP.HCM', '0923456789', 'cuong.le@example.test', '112233445566',
 1995, 'STANDARD', 500000.00, 1, FALSE, 0);

INSERT INTO services (id, name, price, unit, stock_quantity, safety_threshold) VALUES
('SV001', 'Nuoc suoi', 10000.00, 'CHAI', 200, 30),
('SV002', 'Bia', 30000.00, 'LON', 120, 20),
('SV003', 'Mi goi', 15000.00, 'GOI', 150, 25),
('SV004', 'Giat ui', 50000.00, 'LAN', 999, 0),
('SV005', 'An sang', 80000.00, 'SUAT', 999, 0);

INSERT INTO maintenance_work_orders
(id, room_id, maintenance_type, scheduled_date, status, description) VALUES
('WO001', 'R302', 'Sua dieu hoa', '2026-03-25', 'CHUA_XU_LY', 'Dieu hoa khong mat'),
('WO002', 'R202', 'Don ve sinh', '2026-03-26', 'DA_HOAN_THANH', 'Phong can don sau khi khach tra');

COMMIT;
