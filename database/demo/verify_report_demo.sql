-- Chạy trước COMMIT của refresh_report_demo.ps1; kiểm tra các tình huống để demo.
SET NOCOUNT ON;
DECLARE @reportDay DATE=(SELECT TRY_CONVERT(DATE,JSON_VALUE(duLieuSau,'$.report_date'))
 FROM dbo.NhatKyKiemSoat WHERE khoaLienKet=N'REPORT-DEMO-COMPLETE');
IF @reportDay IS NULL THROW 51100,N'Thiếu mốc ngày báo cáo.',1;
IF (SELECT COUNT(*) FROM dbo.PhieuDatPhong b JOIN dbo.ChiTietDatPhong c ON c.maPhieuDatPhong=b.maPhieuDatPhong
 WHERE b.khoaChongTrung LIKE N'OPS-DEMO-RES-%' AND b.trangThai=N'Đã thanh toán cọc'
 AND CAST(c.thoiDiemNhanPhong AS DATE)=@reportDay)<8
 THROW 51101,N'Ngày báo cáo phải có ít nhất tám lượt đến đã thu cọc.',1;
IF (SELECT COUNT(*) FROM dbo.PhieuDatPhong b JOIN dbo.ChiTietDatPhong c ON c.maPhieuDatPhong=b.maPhieuDatPhong
 WHERE b.khoaChongTrung LIKE N'OPS-DEMO-RES-%' AND b.trangThai=N'Đã nhận phòng'
 AND CAST(c.thoiDiemTraPhong AS DATE)=@reportDay)<10
 THROW 51102,N'Ngày báo cáo phải có ít nhất mười khách đang ở dự kiến trả phòng.',1;
IF (SELECT COUNT(*) FROM dbo.PhieuDatPhong b JOIN dbo.ChiTietDatPhong c ON c.maPhieuDatPhong=b.maPhieuDatPhong
 WHERE b.khoaChongTrung LIKE N'OPS-DEMO-RES-%' AND b.trangThai=N'Đã nhận phòng'
 AND c.thoiDiemTraPhong>=CAST(@reportDay AS DATETIME2))<>20
 THROW 51103,N'Phải giữ đủ hai mươi lượt đang ở còn hiệu lực đến ngày báo cáo.',1;
IF EXISTS(SELECT 1 FROM (VALUES(N'Cần dọn phòng'),(N'Đang thực hiện'),(N'Đã dọn xong'),(N'Chờ kỹ thuật'),(N'Sẵn sàng')) v(status)
 WHERE NOT EXISTS(SELECT 1 FROM dbo.NhiemVuBuongPhong h WHERE h.nguoiDuocPhanCong=N'HOUSEKEEP' AND h.trangThai=v.status))
 THROW 51104,N'Tài khoản HOUSEKEEP chưa có đủ năm trạng thái để demo.',1;
IF EXISTS(SELECT 1 FROM dbo.NhiemVuBuongPhong h CROSS JOIN dbo.MauChecklistBuongPhong t
 WHERE h.ghiChu LIKE N'OPS-DEMO:%' AND t.dangHoatDong=1 AND NOT EXISTS(
 SELECT 1 FROM dbo.KetQuaChecklistBuongPhong c WHERE c.maNhiemVuBuongPhong=h.maNhiemVuBuongPhong AND c.hangMuc=t.ten))
 THROW 51105,N'Thiếu checklist cho nhiệm vụ buồng phòng.',1;
IF EXISTS(SELECT 1 FROM (VALUES(N'FRONTDESK'),(N'HOUSEKEEP')) e(id)
 CROSS JOIN (VALUES(N'Ca sáng'),(N'Ca chiều'),(N'Ca đêm')) s(code)
 WHERE NOT EXISTS(SELECT 1 FROM dbo.CaLamViecNhanVien c WHERE c.maNhanVien=e.id AND c.maCa=s.code
 AND c.ngayLamCa=@reportDay AND c.trangThai<>N'Đã hủy'))
 THROW 51106,N'Lễ tân hoặc buồng phòng thiếu ca ngày báo cáo.',1;
IF EXISTS(SELECT 1 FROM dbo.TonKhoBuongPhong h OUTER APPLY(
 SELECT SUM(CASE WHEN r.loai=N'Cấp hàng' THEN r.soLuong ELSE -r.soLuong END) qty
 FROM dbo.YeuCauKho r WHERE r.maDichVu=h.maDichVu AND r.trangThai=N'Đã duyệt') movements
 WHERE h.soLuongTon<>COALESCE(movements.qty,0))
 THROW 51107,N'Tồn kho bộ phận không khớp các phiếu cấp/trả/hỏng đã duyệt.',1;
IF EXISTS(SELECT 1 FROM (VALUES(N'PILLOW'),(N'TOWELCLEAN'),(N'LINENKING'),(N'LINENTWIN')) l(id)
 WHERE NOT EXISTS(SELECT 1 FROM dbo.TonKhoBuongPhong h WHERE h.maDichVu=l.id AND h.soLuongTon>=20))
 THROW 51108,N'Kho buồng phòng chưa nhận đủ đồ vải để demo.',1;
IF EXISTS(SELECT 1 FROM dbo.CaLamViecNhanVien a JOIN dbo.CaLamViecNhanVien b
 ON a.maNhanVien=b.maNhanVien AND a.maCaLamViecNhanVien<b.maCaLamViecNhanVien
 WHERE a.trangThai<>N'Đã hủy' AND b.trangThai<>N'Đã hủy'
 AND a.thoiDiemBatDau<b.thoiDiemKetThuc AND b.thoiDiemBatDau<a.thoiDiemKetThuc)
 THROW 51109,N'Lịch ca bị chồng thời gian.',1;
SELECT @reportDay ReportDate;
SELECT h.trangThai HousekeepingStatus,COUNT(*) Qty FROM dbo.NhiemVuBuongPhong h
 WHERE h.nguoiDuocPhanCong=N'HOUSEKEEP' GROUP BY h.trangThai;
PRINT N'PASS: ngày báo cáo có khách đến/trả, khách đang ở, đủ trạng thái buồng phòng, checklist, đồ vải và ba ca.';
