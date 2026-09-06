-- ==================== DỮ LIỆU MẪU ====================

-- Loại Phòng
INSERT INTO LoaiPhong (MaLoaiPhong, TenLoai, Gia, MoTa) VALUES
('LP001', 'Phòng Đơn', 500000, 'Phòng 1 giường đơn, view thành phố'),
('LP002', 'Phòng Đôi', 800000, 'Phòng 1 giường đôi lớn'),
('LP003', 'Phòng VIP', 1500000, 'Phòng suite cao cấp, view sông'),
('LP004', 'Phòng Gia Đình', 1200000, '2 giường đôi, phù hợp gia đình');

-- Phòng
INSERT INTO Phong (MaPhong, MaLoaiPhong, TinhTrang, MoTa) VALUES
-- Tầng 1 (thêm P103, P104, P105)
('P101', 'LP001', 'SAN_SANG', 'Tầng 1 - Phòng tiêu chuẩn'),
('P102', 'LP001', 'SAN_SANG', 'Tầng 1 - Phòng tiêu chuẩn'),
('P103', 'LP001', 'SAN_SANG', 'Tầng 1 - Phòng tiêu chuẩn'),
('P104', 'LP001', 'SAN_SANG', 'Tầng 1 - Phòng tiêu chuẩn'),
('P105', 'LP001', 'SAN_SANG', 'Tầng 1 - Phòng tiêu chuẩn'),
-- Tầng 2 (thêm P203, P204, P205)
('P201', 'LP002', 'DANG_O', 'Tầng 2 - Phòng đôi'),
('P202', 'LP002', 'DANG_DON_DEP', 'Tầng 2 - Phòng đôi'),
('P203', 'LP002', 'SAN_SANG', 'Tầng 2 - Phòng đôi'),
('P204', 'LP002', 'SAN_SANG', 'Tầng 2 - Phòng đôi'),
('P205', 'LP002', 'SAN_SANG', 'Tầng 2 - Phòng đôi'),
-- Tầng 3 (thêm P303, P304, P305)
('P301', 'LP003', 'DANG_O', 'Tầng 3 - Phòng VIP'),
('P302', 'LP003', 'BAO_TRI', 'Tầng 3 - Phòng VIP'),
('P303', 'LP003', 'SAN_SANG', 'Tầng 3 - Phòng VIP'),
('P304', 'LP003', 'SAN_SANG', 'Tầng 3 - Phòng VIP'),
('P305', 'LP003', 'SAN_SANG', 'Tầng 3 - Phòng VIP'),
-- Tầng 4 (thêm P403, P404, P405)
('P401', 'LP004', 'SAN_SANG', 'Tầng 4 - Phòng gia đình'),
('P402', 'LP004', 'SAN_SANG', 'Tầng 4 - Phòng gia đình'),
('P403', 'LP004', 'SAN_SANG', 'Tầng 4 - Phòng gia đình'),
('P404', 'LP004', 'SAN_SANG', 'Tầng 4 - Phòng gia đình'),
('P405', 'LP004', 'SAN_SANG', 'Tầng 4 - Phòng gia đình');

-- Khách Hàng
INSERT INTO KhachHang (TenKH, DiaChi, SoDienThoai, Email, CCCD) VALUES
('Nguyễn Văn An', 'Quận 1, TP.HCM', '0901234567', 'an.nguyen@gmail.com', '012345678901'),
('Trần Thị Bình', 'Quận 3, TP.HCM', '0912345678', 'binh.tran@yahoo.com', '098765432109'),
('Lê Hoàng Cường', 'Quận 7, TP.HCM', '0923456789', 'cuong.le@hotmail.com', '112233445566'),
('Phạm Minh Đức', 'Bình Thạnh, TP.HCM', '0934567890', 'duc.pham@gmail.com', '223344556677'),
('Hoàng Thị Lan', 'Quận 10, TP.HCM', '0945678901', 'lan.hoang@gmail.com', '334455667788');

-- Nhân Viên
INSERT IGNORE INTO NhanVien (MaNV, TenNV, Password, ChucVu, SoDienThoai) VALUES
('NV001', 'Admin', '123456', 'LE_TAN', '0901112222'),
('NV002', 'Trần Văn Lễ Tân', '123123', 'LE_TAN', '0912223333'),
('NV003', 'Phạm Thị Thu Ngân', '00000', 'THU_NGAN', '0923334444'),
('NV004', 'Nguyễn Văn A', '11111', 'QUAN_LY', '0123124124');

-- Đặt phòng
INSERT INTO DatPhong (MAKH, MANV, NGAYDAT, GIACOC) VALUES
(1, 'NV002', '2026-03-25 10:00:00', 200000),
(2, 'NV002', '2026-03-26 14:30:00', 300000),
(3, 'NV001', '2026-03-27 09:15:00', 500000);

-- Chi tiết đặt phòng
INSERT INTO ChiTietDatPhong (MADP, MAPHONG, NGAYNHAN, NGAYTRA, TINHTRANG) VALUES
(1, 'P101', '2026-03-26 14:00:00', '2026-03-28 12:00:00', 'DA_TRA'),
(2, 'P201', '2026-03-27 14:00:00', '2026-03-29 12:00:00', 'DANG_O'),
(3, 'P301', '2026-03-28 14:00:00', '2026-03-30 12:00:00', 'DANG_O');

INSERT INTO DichVu (MADV, TENDICHVU, GIADV) VALUES
('DV001', 'Nước suối', 10000),
('DV002', 'Bia', 30000),
('DV003', 'Mì gói', 15000),
('DV004', 'Giặt ủi', 50000),
('DV005', 'Ăn sáng', 80000);

INSERT INTO ChiTietDichVu (MADP, MADV, NGAYSUDUNG, SOLUONG) VALUES
(1, 'DV001', '2026-03-26', 2),
(1, 'DV003', '2026-03-27', 1),

(2, 'DV002', '2026-03-27', 3),
(2, 'DV005', '2026-03-28', 2),

(3, 'DV004', '2026-03-29', 1),
(3, 'DV001', '2026-03-29', 4);

INSERT INTO HoaDon 
(MADP, NGAYXUATHD, TONGTIENPHONG, TONGTIENDICHVU, GIAMGIA, TIENCODADONG, TONGTIENPHAITRA, PHUONGTHUCTHANHTOAN, TRANGTHAI)
VALUES
(1, '2026-03-28 12:30:00', 1000000, 35000, 50000, 200000, 785000, 'TIEN_MAT', 'DA_THANH_TOAN'),

(2, '2026-03-29 12:30:00', 1600000, 190000, 0, 300000, 1490000, 'CHUYEN_KHOAN', 'CHUA_THANH_TOAN'),

(3, '2026-03-30 12:30:00', 3000000, 90000, 100000, 500000, 2490000, 'TIEN_MAT', 'CHUA_THANH_TOAN');

INSERT INTO BaoTri (MABT, MAPHONG, LOAIBAOTRI, NGAYBAOTRI, TINHTRANGBTRI, MOTA) VALUES
('BT001', 'P302', 'Sửa điều hòa', '2026-03-25', 'CHUA_XU_LY', 'Điều hòa không mát'),
('BT002', 'P202', 'Dọn vệ sinh', '2026-03-26', 'DA_HOAN_THANH', 'Phòng cần dọn sau khi khách trả'),
('BT003', 'P301', 'Kiểm tra điện', '2026-03-27', 'DA_HOAN_THANH', 'Đèn chập chờn');