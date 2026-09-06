package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.LoaiPhong;
import java.util.List;

/**
 * Interface dùng để quản lý loại phòng:
 * bao gồm truy vấn, thêm và xóa loại phòng
 */
public interface LoaiPhongDAO {

    // Tìm loại phòng theo mã loại phòng
    public LoaiPhong findByMaLoaiPhong(String maLoaiPhong);

    // Lấy toàn bộ danh sách loại phòng
    public List<LoaiPhong> findAll();

    // Thêm mới loại phòng
    public void save(LoaiPhong loaiPhong);

    // Xóa loại phòng theo mã
    public void delete(String maLoaiPhong);
}