package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface dùng để xử lý nghiệp vụ đặt phòng:
 * bao gồm tạo, quản lý, kiểm tra và cập nhật thông tin đặt phòng
 */
public interface DatPhongService {

    // Tạo mới đặt phòng từ DTO
    public DatPhongDTO taoDatPhong(DatPhongDTO dto);

    // Tạo mới đặt phòng với thông tin cơ bản
    public DatPhongDTO taoDatPhongMoi(Long maKH, String maNV, BigDecimal tienCoc);

    // Thêm phòng vào đặt phòng
    public void themPhongVaoDatPhong(Long maDP, String maPhong, LocalDateTime ngayNhan, LocalDateTime ngayTra);

    // Lấy thông tin chi tiết đặt phòng
    public DatPhongDTO layDatPhongChiTietDTO(Long maDP);

    // Lấy toàn bộ danh sách đặt phòng
    public List<DatPhongDTO> layTatCaDatPhongDTO();

    // Lấy danh sách đặt phòng chưa thanh toán
    public List<DatPhongDTO> layTatCaDatPhongChuaThanhToanDTO();

    // Lấy lịch sử đặt phòng của khách hàng
    public List<DatPhongDTO> layLichSuDatPhongCuaKhachDTO(Long maKH);

    // Hủy đặt phòng
    public void huyDatPhong(Long maDP);

    // Thực hiện check-in cho phòng
    public void checkIn(Long maDP, String maPhong, LocalDateTime thoiGianCheckIn);

    // Kiểm tra đặt phòng có hợp lệ hay không
    public boolean isDatPhongHopLe(Long maDP);

    // Cập nhật thời gian đặt phòng
    public void capNhatThoiGianDatPhong(Long maDP, String maPhong, LocalDateTime in, LocalDateTime out);

    // Hủy riêng một phòng trong đơn đặt phòng
    public void huyPhongDat(Long maDP, String maPhong);
}