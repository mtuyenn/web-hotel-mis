package com.hotelmanagement.controller;

import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dto.HoaDonDTO;
import com.hotelmanagement.model.enums.PhuongThucThanhToan;
import com.hotelmanagement.service.HoaDonService;

import java.math.BigDecimal;
import java.util.List;
import com.hotelmanagement.service.impl.HoaDonServiceImpl;

/**
 * Controller xử lý các chức năng liên quan đến Hóa đơn và thanh toán
 */
public class HoaDonController {

    private final HoaDonService hoaDonService = new HoaDonServiceImpl();

    /**
     * Lấy tất cả hóa đơn
     */
    public List<HoaDonDTO> getAllHoaDon() {
        return hoaDonService.getAllHoaDon();
    }

    /**
     * Lấy tất cả hóa đơn với dữ liệu mới nhất
     */
    public List<HoaDonDTO> getAllHoaDonFresh() {
        return hoaDonService.getAllHoaDonFresh();
    }

    /**
     * Xem trước hóa đơn (không lưu vào DB)
     */
    public HoaDonDTO taoHoaDonKhiNhanPhong(Long maDP) {
        if (maDP == null) {
            throw new ValidationException("Mã đặt phòng không được để trống");
        }
        return hoaDonService.taoHoaDonKhiNhanPhong(maDP);
    }

    /**
     * Thanh toán và tạo hóa đơn chính thức
     */
    public HoaDonDTO thanhToan(Long maDP, PhuongThucThanhToan phuongThuc, BigDecimal giamGia) {
        if (maDP == null) {
            throw new ValidationException("Mã đặt phòng không được để trống");
        }
        if (phuongThuc == null) {
            throw new ValidationException("Phương thức thanh toán không được để trống");
        }
        return hoaDonService.checkOut(maDP, phuongThuc, giamGia);
    }

    /**
     * Tìm hóa đơn theo mã đặt phòng
     */
    public HoaDonDTO timHoaDonTheoDatPhong(Long maDP) {
        if (maDP == null) {
            throw new ValidationException("Mã đặt phòng không được để trống");
        }
        return hoaDonService.timHoaDonTheoDatPhong(maDP); // Service đã trả về DTO
    }
}