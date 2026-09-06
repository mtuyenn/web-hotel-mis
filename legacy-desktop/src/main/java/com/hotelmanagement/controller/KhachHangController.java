package com.hotelmanagement.controller;

import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dto.KhachHangDTO;
import com.hotelmanagement.service.KhachHangService;

import java.util.List;
import com.hotelmanagement.service.impl.KhachHangServiceImpl;

/**
 * Lớp KhachHangController dùng để xử lý các chức năng liên quan
 * đến khách hàng trong hệ thống.
 * 
 * Lớp này tiếp nhận dữ liệu từ giao diện, kiểm tra hợp lệ
 * và gọi Service để xử lý nghiệp vụ.
 */
public class KhachHangController {

    // Khởi tạo service xử lý nghiệp vụ khách hàng
    private final KhachHangService khachHangService = new KhachHangServiceImpl();

    /**
     * Thêm mới một khách hàng
     */
    public void taoKhachHang(KhachHangDTO dto) {

        // Kiểm tra dữ liệu đầu vào
        if (dto == null)
            throw new ValidationException("Dữ liệu khách hàng không được để trống");

        // Gọi service để thêm khách hàng
        khachHangService.themKhachHang(dto);
    }

    /**
     * Tìm khách hàng theo CCCD
     */
    public KhachHangDTO timKhachHangTheoCCCD(String cccd) {

        // Kiểm tra CCCD hợp lệ
        if (cccd == null || cccd.isBlank())
            throw new ValidationException("CCCD không được để trống");

        return khachHangService.timKhachHangByCccd(cccd);
    }

    /**
     * Lấy toàn bộ danh sách khách hàng
     */
    public List<KhachHangDTO> layTatCaKhachHang() {
        return khachHangService.layTatCaKhachHang();
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public void capNhatKhachHang(KhachHangDTO dto) {

        // Kiểm tra dữ liệu cập nhật
        if (dto == null)
            throw new ValidationException("Dữ liệu cập nhật không được để trống");

        // Gọi service để cập nhật
        khachHangService.capNhatKhachHang(dto);
    }

    /**
     * Xóa khách hàng theo CCCD
     */
    public void xoaKhachHang(String cccd) {

        // Kiểm tra CCCD
        if (cccd == null || cccd.isBlank())
            throw new ValidationException("CCCD không được để trống");

        // Gọi service để xóa
        khachHangService.xoaKhachHang(cccd);
    }
}