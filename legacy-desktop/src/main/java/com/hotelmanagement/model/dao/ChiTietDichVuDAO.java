package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.ChiTietDichVu;
import com.hotelmanagement.model.keyclass.ChiTietDichVuId;

/**
 * Interface dùng để quản lý chi tiết dịch vụ:
 * bao gồm truy vấn, thêm và xóa dịch vụ theo đặt phòng
 */
public interface ChiTietDichVuDAO {

    // Tìm chi tiết dịch vụ theo ID
    public ChiTietDichVu findById(ChiTietDichVuId id);

    // Thêm mới chi tiết dịch vụ
    public void save(ChiTietDichVu chiTiet);

    // Xóa chi tiết dịch vụ theo ID
    public void delete(ChiTietDichVuId id);

    // Xóa tất cả dịch vụ theo mã đặt phòng
    public void deleteByDatPhong(Long maDP);

    // Lấy danh sách dịch vụ theo mã đặt phòng
    public java.util.List<ChiTietDichVu> findByMaDP(Long maDP);
}