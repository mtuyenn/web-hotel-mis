package com.hotelmanagement.controller;

import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.entity.LoaiPhong;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.service.PhongService;

import java.time.LocalDateTime;
import java.util.List;
import com.hotelmanagement.service.impl.PhongServiceImpl;

/**
 * Lớp PhongController dùng để xử lý các chức năng liên quan
 * đến quản lý phòng trong hệ thống khách sạn.
 * 
 * Lớp này kiểm tra dữ liệu đầu vào và gọi Service
 * để thực hiện các nghiệp vụ như tìm phòng, tạo phòng,
 * cập nhật trạng thái phòng.
 */
public class PhongController {

    // Khởi tạo service xử lý nghiệp vụ phòng
    private final PhongService phongService = new PhongServiceImpl();

    /**
     * Lấy danh sách phòng trống theo khoảng thời gian và loại phòng (nếu có)
     */
    public List<PhongDTO> layPhongTrong(String maLoaiPhong, LocalDateTime ngayNhan, LocalDateTime ngayTra) {

        // Kiểm tra dữ liệu đầu vào
        if (ngayNhan == null || ngayTra == null) {
            throw new ValidationException("Ngày nhận và ngày trả không được để trống");
        }

        // Nếu có mã loại phòng thì tạo đối tượng LoaiPhong để lọc
        LoaiPhong loaiPhong = null;
        if (maLoaiPhong != null && !maLoaiPhong.isBlank()) {
            loaiPhong = new LoaiPhong();
            loaiPhong.setMaLoaiPhong(maLoaiPhong);
        }

        // Gọi service để tìm phòng trống
        return phongService.timPhongTrong(ngayNhan, ngayTra, loaiPhong);
    }

    /**
     * Lấy toàn bộ danh sách phòng
     */
    public List<PhongDTO> getAllPhong() {
        return phongService.timTatCaPhongDTO();
    }

    /**
     * Tạo mới một phòng
     */
    public PhongDTO taoPhong(PhongDTO dto) {

        // Kiểm tra dữ liệu đầu vào
        if (dto == null || dto.getMaPhong() == null || dto.getMaPhong().isBlank()) {
            throw new ValidationException("Mã phòng không được để trống");
        }

        return phongService.taoPhong(dto);
    }

    /**
     * Cập nhật trạng thái phòng
     */
    public PhongDTO capNhatTrangThaiPhong(String maPhong, String tinhTrangMoi) {

        // Kiểm tra mã phòng
        if (maPhong == null || maPhong.isBlank()) {
            throw new ValidationException("Mã phòng không được để trống");
        }

        TinhTrangPhong trangThaiEnum;

        try {
            // Chuyển String sang enum
            trangThaiEnum = TinhTrangPhong.valueOf(tinhTrangMoi.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Xử lý khi trạng thái không hợp lệ
            throw new ValidationException("Trạng thái phòng không hợp lệ: " + tinhTrangMoi);
        }

        // Gọi service cập nhật trạng thái
        phongService.capNhatTrangThaiPhong(maPhong, trangThaiEnum);

        // Trả về thông tin phòng sau khi cập nhật
        return phongService.timPhongTheoMaPhongDTO(maPhong);
    }

    /**
     * Tìm phòng theo mã
     */
    public PhongDTO timPhongTheoMa(String maPhong) {

        // Kiểm tra mã phòng
        if (maPhong == null || maPhong.isBlank()) {
            throw new ValidationException("Mã phòng không được để trống");
        }

        return phongService.timPhongTheoMaPhongDTO(maPhong);
    }

    /**
     * Xóa phòng (thực chất là chuyển trạng thái sang NGUNG_SU_DUNG)
     */
    public PhongDTO deletePhong(String maPhong) {

        // Kiểm tra mã phòng
        if (maPhong == null || maPhong.isBlank()) {
            throw new ValidationException("Mã phòng không được để trống");
        }

        // Gọi lại hàm cập nhật trạng thái
        return capNhatTrangThaiPhong(maPhong, TinhTrangPhong.NGUNG_SU_DUNG.name());
    }
}