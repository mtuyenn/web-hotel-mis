package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.KhachHang;
import java.util.List;

/**
 * Interface dùng để quản lý khách hàng:
 * bao gồm truy vấn, thêm và xóa thông tin khách hàng
 */
public interface KhachHangDAO {

    // Tìm khách hàng theo mã khách hàng
    public KhachHang findByMakh(Long maKH);

    // Tìm khách hàng theo CCCD
    public KhachHang findByCccd(String cccd);

    // Lấy toàn bộ danh sách khách hàng
    public List<KhachHang> findAll();

    // Thêm mới khách hàng
    public void save(KhachHang kh);

    // Xóa khách hàng theo CCCD
    public void delete(String cccd);
}