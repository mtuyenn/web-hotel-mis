package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.DichVu;
import java.util.List;

/**
 * Interface dùng để quản lý dịch vụ:
 * bao gồm truy vấn, thêm và xóa dịch vụ
 */
public interface DichVuDAO {

    // Tìm dịch vụ theo mã dịch vụ
    public DichVu findByMaDV(String maDV);

    // Lấy toàn bộ danh sách dịch vụ
    public List<DichVu> findAll();

    // Thêm mới dịch vụ
    public void save(DichVu dichVu);

    // Xóa dịch vụ theo mã
    public void delete(String maDV);
}