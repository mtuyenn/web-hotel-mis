package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.HoaDon;
import java.util.List;

/**
 * Interface dùng để quản lý hóa đơn:
 * bao gồm truy vấn, thêm, cập nhật và kiểm tra dữ liệu hóa đơn
 */
public interface HoaDonDAO {

    // Tìm hóa đơn theo mã đặt phòng
    public HoaDon findByMaDP(Long maDP);

    // Thêm mới hóa đơn
    public void save(HoaDon hd);

    // Cập nhật thông tin hóa đơn
    public void update(HoaDon hd);

    // Lấy toàn bộ danh sách hóa đơn
    public List<HoaDon> findAll();

    // Lấy danh sách hóa đơn mới (chưa xử lý hoặc mới tạo)
    public List<HoaDon> findAllFresh();

    // Kiểm tra nhân viên đã tồn tại trong hóa đơn chưa
    public boolean existsByNhanVien(String maNV);
}