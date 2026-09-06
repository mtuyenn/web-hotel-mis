package com.hotelmanagement.controller;

import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dto.ChiTietDatPhongDTO;
import com.hotelmanagement.service.ChiTietDatPhongService;

import java.time.LocalDateTime;
import java.util.List;
import com.hotelmanagement.service.impl.ChiTietDatPhongServiceImpl;

/**
 * Lớp ChiTietDatPhongController dùng để xử lý các yêu cầu liên quan
 * đến chi tiết đặt phòng từ phía người dùng.
 * 
 * Lớp này thực hiện kiểm tra dữ liệu đầu vào và gọi các phương thức
 * tương ứng từ tầng Service để xử lý logic nghiệp vụ.
 */
public class ChiTietDatPhongController {

    // Khởi tạo service để xử lý nghiệp vụ
    private final ChiTietDatPhongService service = new ChiTietDatPhongServiceImpl();

    /**
     * Thêm phòng vào đơn đặt phòng
     */
    public ChiTietDatPhongDTO themPhong(Long maDP, String maPhong,
            LocalDateTime ngayNhan, LocalDateTime ngayTra) {

        // Kiểm tra dữ liệu đầu vào
        if (maDP == null || maPhong == null)
            throw new ValidationException("Mã đặt phòng và mã phòng không được để trống");

        // Gọi service để thêm phòng
        return service.themPhong(maDP, maPhong, ngayNhan, ngayTra);
    }

    /**
     * Xóa phòng khỏi đơn đặt phòng
     */
    public void xoaPhong(Long maDP, String maPhong) {

        // Kiểm tra dữ liệu đầu vào
        if (maDP == null || maPhong == null)
            throw new ValidationException("Mã đặt phòng và mã phòng không được để trống");

        // Gọi service để xóa phòng
        service.xoaPhong(maDP, maPhong);
    }

    /**
     * Lấy danh sách phòng theo mã đặt phòng
     */
    public List<ChiTietDatPhongDTO> layDanhSachPhong(Long maDP) {

        // Kiểm tra dữ liệu đầu vào
        if (maDP == null)
            throw new ValidationException("Mã đặt phòng không được để trống");

        // Gọi service để lấy danh sách
        return service.findByDatPhong(maDP);
    }

    /**
     * Cập nhật thời gian nhận/trả phòng
     */
    public ChiTietDatPhongDTO capNhatThoiGian(Long maDP, String maPhong,
            LocalDateTime ngayNhan, LocalDateTime ngayTra) {

        // Kiểm tra dữ liệu đầu vào
        if (maDP == null || maPhong == null)
            throw new ValidationException("Mã đặt phòng và mã phòng không được để trống");

        // Gọi service để cập nhật
        return service.updateThoiGian(maDP, maPhong, ngayNhan, ngayTra);
    }
}