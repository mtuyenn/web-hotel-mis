package com.hotelmanagement.controller;

import com.hotelmanagement.model.dto.DatPhongDTO;
import com.hotelmanagement.service.DatPhongService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.hotelmanagement.service.impl.DatPhongServiceImpl;

/**
 * Lớp DatPhongController dùng để xử lý các chức năng liên quan
 * đến việc đặt phòng trong hệ thống.
 * 
 * Lớp này đóng vai trò trung gian, nhận dữ liệu từ giao diện
 * và gọi Service để xử lý nghiệp vụ.
 */
public class DatPhongController {

    // Khởi tạo service xử lý nghiệp vụ
    private final DatPhongService datPhongService = new DatPhongServiceImpl();

    /**
     * Tạo đơn đặt phòng từ DTO
     */
    public DatPhongDTO taoDatPhong(DatPhongDTO dto) {
        return datPhongService.taoDatPhong(dto);
    }

    // ==================== ĐẶT PHÒNG ====================

    /**
     * Chức năng đặt phòng nhanh (gộp nhiều bước)
     * - Tạo đơn đặt phòng
     * - Thêm phòng vào đơn
     */
    public DatPhongDTO datPhongNhanh(
            Long maKH,
            String maNV,
            String maPhong,
            LocalDateTime ngayNhan,
            LocalDateTime ngayTra,
            BigDecimal tienCoc) {

        // Bước 1: tạo đơn đặt phòng
        DatPhongDTO dp = datPhongService.taoDatPhongMoi(maKH, maNV, tienCoc);

        // Bước 2: thêm phòng vào đơn vừa tạo
        datPhongService.themPhongVaoDatPhong(
                dp.getMaDP(),
                maPhong,
                ngayNhan,
                ngayTra);

        return dp;
    }

    /**
     * Lấy thông tin chi tiết của một đơn đặt phòng
     */
    public DatPhongDTO layDatPhongChiTiet(Long maDP) {
        return datPhongService.layDatPhongChiTietDTO(maDP);
    }

    /**
     * Lấy danh sách các đơn đặt phòng chưa thanh toán
     */
    public List<DatPhongDTO> layDatPhongChuaThanhToan() {
        return datPhongService.layTatCaDatPhongChuaThanhToanDTO();
    }

    /**
     * Lấy lịch sử đặt phòng của một khách hàng
     */
    public List<DatPhongDTO> layLichSuDatPhongKhach(Long maKH) {
        return datPhongService.layLichSuDatPhongCuaKhachDTO(maKH);
    }

    /**
     * Lấy toàn bộ danh sách đặt phòng
     */
    public List<DatPhongDTO> getAllDatPhong() {
        return datPhongService.layTatCaDatPhongDTO();
    }

    /**
     * Hủy một đơn đặt phòng
     */
    public void huyDatPhong(Long maDP) {
        datPhongService.huyDatPhong(maDP);
    }

    /**
     * Thực hiện check-in cho khách
     */
    public void checkIn(Long maDP, String maPhong, LocalDateTime thoiGianCheckIn) {
        datPhongService.checkIn(maDP, maPhong, thoiGianCheckIn);
    }

    /**
     * Kiểm tra đơn đặt phòng có hợp lệ hay không
     */
    public boolean kiemTraDatPhongHopLe(Long maDP) {
        return datPhongService.isDatPhongHopLe(maDP);
    }

    /**
     * Cập nhật lại thời gian nhận/trả phòng
     */
    public void capNhatThoiGianDatPhong(Long maDP, String maPhong, LocalDateTime in, LocalDateTime out) {
        datPhongService.capNhatThoiGianDatPhong(maDP, maPhong, in, out);
    }

    /**
     * Hủy riêng một phòng trong đơn đặt phòng
     */
    public void huyPhongDat(Long maDP, String maPhong) {
        datPhongService.huyPhongDat(maDP, maPhong);
    }
}