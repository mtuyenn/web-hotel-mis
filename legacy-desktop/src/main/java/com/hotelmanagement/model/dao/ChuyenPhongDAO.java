package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.ChuyenPhong;
import java.util.List;

/**
 * Interface dùng để quản lý chuyển phòng:
 * bao gồm truy vấn và lưu thông tin chuyển phòng
 */
public interface ChuyenPhongDAO {

    // Tìm thông tin chuyển phòng theo mã chuyển phòng
    public ChuyenPhong findById(Long maChuyenPhong);

    // Lấy danh sách chuyển phòng theo mã đặt phòng
    public List<ChuyenPhong> findByDatPhong(Long maDP);

    // Lấy toàn bộ danh sách chuyển phòng
    public List<ChuyenPhong> findAll();

    // Thêm mới thông tin chuyển phòng
    public void save(ChuyenPhong chuyenPhong);
}