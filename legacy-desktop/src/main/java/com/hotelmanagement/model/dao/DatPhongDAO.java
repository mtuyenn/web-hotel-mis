package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.DatPhong;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface dùng để quản lý đặt phòng:
 * bao gồm truy vấn, thêm, sửa, xóa và tìm kiếm theo điều kiện
 */
public interface DatPhongDAO {

    // Lấy danh sách tất cả đặt phòng kèm chi tiết
    public List<DatPhong> findAllWithDetails();

    // Tìm đặt phòng theo mã đặt phòng
    public DatPhong findByMaDP(Long maDP);

    // Lấy danh sách đặt phòng chưa thanh toán
    public List<DatPhong> findAllChuaThanhToan();

    // Lấy danh sách đặt phòng theo khách hàng
    public List<DatPhong> findByKhachHang(Long maKH);

    // Thêm mới đặt phòng
    public void save(DatPhong dp);

    // Cập nhật thông tin đặt phòng
    public void update(DatPhong dp);

    // Xóa đặt phòng theo mã
    public void delete(Long maDP);

    // Tìm đặt phòng theo khoảng thời gian đặt
    public List<DatPhong> findByNgayDatBetween(LocalDateTime from, LocalDateTime to);
}