package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.HoaDonDTO;
import com.hotelmanagement.model.enums.PhuongThucThanhToan;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface dùng để xử lý nghiệp vụ hóa đơn:
 * bao gồm tạo hóa đơn, thanh toán và truy vấn thông tin hóa đơn
 */
public interface HoaDonService {

    // Thực hiện checkout và thanh toán hóa đơn
    public HoaDonDTO checkOut(Long maDP, PhuongThucThanhToan phuongThucThanhToan, BigDecimal giamGiaInput);

    // Tạo hóa đơn khi nhận phòng
    public HoaDonDTO taoHoaDonKhiNhanPhong(Long maDP);

    // Tìm hóa đơn theo mã đặt phòng
    public HoaDonDTO timHoaDonTheoDatPhong(Long maDP);

    // Lấy toàn bộ danh sách hóa đơn
    public List<HoaDonDTO> getAllHoaDon();

    // Lấy danh sách hóa đơn mới
    public List<HoaDonDTO> getAllHoaDonFresh();
}