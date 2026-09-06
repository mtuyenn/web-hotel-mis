package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.NhanVien;
import com.hotelmanagement.model.enums.ChucVuNhanVien;
import java.util.List;

/**
 * Interface dùng để quản lý nhân viên:
 * bao gồm truy vấn, thêm, xóa và tìm kiếm theo chức vụ hoặc tên
 */
public interface NhanVienDAO {

    // Tìm nhân viên theo mã nhân viên
    public NhanVien findByMaNV(String maNV);

    // Lấy toàn bộ danh sách nhân viên
    public List<NhanVien> findAll();

    // Thêm mới nhân viên
    public void save(NhanVien nv);

    // Xóa nhân viên theo mã
    public void delete(String maNV);

    // Lấy danh sách nhân viên theo chức vụ
    public List<NhanVien> findByChucVu(ChucVuNhanVien chucVu);

    // Tìm nhân viên theo tên
    public NhanVien findByTenNV(String tenNV);
}