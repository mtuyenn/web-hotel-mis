package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.ChiTietDatPhongDTO;
import com.hotelmanagement.model.enums.TinhTrangPhong;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface dùng để xử lý nghiệp vụ chi tiết đặt phòng:
 * bao gồm thêm, xóa phòng và cập nhật thông tin đặt phòng
 */
public interface ChiTietDatPhongService {

    // Thêm phòng vào đặt phòng
    public ChiTietDatPhongDTO themPhong(Long maDP, String maPhong, LocalDateTime ngayNhan, LocalDateTime ngayTra);

    // Xóa phòng khỏi đặt phòng
    public void xoaPhong(Long maDP, String maPhong);

    // Lấy danh sách chi tiết phòng theo mã đặt phòng
    public List<ChiTietDatPhongDTO> findByDatPhong(Long maDP);

    // Cập nhật thời gian nhận và trả phòng
    public ChiTietDatPhongDTO updateThoiGian(Long maDP, String maPhong, LocalDateTime ngayNhan, LocalDateTime ngayTra);

    // Cập nhật trạng thái đặt phòng
    public void capNhatTrangThaiDatPhong(Long maDP, TinhTrangPhong trangThaiMoi);

    // Cập nhật thời gian đặt phòng
    public void capNhatThoiGianDatPhong(Long maDP, String maPhong, LocalDateTime in, LocalDateTime out);
}