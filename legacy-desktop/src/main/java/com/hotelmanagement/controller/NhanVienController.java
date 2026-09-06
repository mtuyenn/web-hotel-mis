package com.hotelmanagement.controller;

import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dto.NhanVienDTO;
import com.hotelmanagement.model.enums.ChucVuNhanVien;
import com.hotelmanagement.service.NhanVienService;

import java.util.List;
import com.hotelmanagement.service.impl.NhanVienServiceImpl;

/**
 * Lớp NhanVienController dùng để xử lý các chức năng liên quan
 * đến quản lý nhân viên trong hệ thống.
 * 
 * Lớp này kiểm tra dữ liệu đầu vào và gọi Service
 * để thực hiện các nghiệp vụ tương ứng.
 */
public class NhanVienController {

    // Khởi tạo service xử lý nghiệp vụ nhân viên
    private final NhanVienService nhanVienService = new NhanVienServiceImpl();

    /**
     * Thêm mới nhân viên
     */
    public NhanVienDTO taoNhanVien(NhanVienDTO dto) {

        // Kiểm tra dữ liệu đầu vào
        if (dto == null)
            throw new ValidationException("Dữ liệu nhân viên không được để trống");

        return nhanVienService.addNhanVien(dto);
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    public NhanVienDTO capNhatNhanVien(NhanVienDTO dto) {

        // Kiểm tra dữ liệu cập nhật
        if (dto == null)
            throw new ValidationException("Dữ liệu cập nhật không được để trống");

        return nhanVienService.updateNhanVien(dto);
    }

    /**
     * Xóa nhân viên theo mã
     */
    public void xoaNhanVien(String maNV) {

        // Kiểm tra mã nhân viên
        if (maNV == null || maNV.isBlank())
            throw new ValidationException("Mã nhân viên không được để trống");

        nhanVienService.deleteNhanVien(maNV);
    }

    /**
     * Tìm nhân viên theo mã
     */
    public NhanVienDTO timNhanVien(String maNV) {

        // Kiểm tra mã nhân viên
        if (maNV == null || maNV.isBlank())
            throw new ValidationException("Mã nhân viên không được để trống");

        return nhanVienService.findByMaNV(maNV);
    }

    /**
     * Tìm danh sách nhân viên theo chức vụ
     */
    public List<NhanVienDTO> timTheoChucVu(String chucVu) {

        // Kiểm tra dữ liệu đầu vào
        if (chucVu == null || chucVu.isBlank())
            throw new ValidationException("Chức vụ không được để trống");

        try {
            // Chuyển String sang enum
            ChucVuNhanVien cv = ChucVuNhanVien.valueOf(chucVu.toUpperCase());

            // Gọi service tìm kiếm
            return nhanVienService.findByChucVu(cv);

        } catch (IllegalArgumentException e) {
            // Xử lý khi giá trị enum không hợp lệ
            throw new ValidationException("Chức vụ không hợp lệ: " + chucVu);
        }
    }

    /**
     * Lấy toàn bộ danh sách nhân viên
     */
    public List<NhanVienDTO> layTatCaNhanVien() {

        // Gọi service để lấy danh sách
        return nhanVienService.getAllNhanVien();
    }
}