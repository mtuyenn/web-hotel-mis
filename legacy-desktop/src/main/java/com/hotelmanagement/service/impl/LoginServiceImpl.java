package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.LoginService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.NhanVienDAO;
import com.hotelmanagement.model.dto.NhanVienDTO;
import com.hotelmanagement.model.entity.NhanVien;
import com.hotelmanagement.model.dao.impl.NhanVienDAOImpl;

/**
 * Service xử lý logic đăng nhập
 * Sử dụng tenNV làm username (theo yêu cầu của em)
 */
public class LoginServiceImpl implements LoginService {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAOImpl();

    /**
     * Đăng nhập bằng Tên nhân viên (tenNV) và mật khẩu
     */
    public NhanVienDTO login(String tenNV, String password) {
        if (tenNV == null || tenNV.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập (tên nhân viên) không được để trống");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống");
        }

        NhanVien nv = nhanVienDAO.findByTenNV(tenNV.trim());

        if (nv == null) {
            throw new BusinessException("Không tìm thấy nhân viên với tên: " + tenNV);
        }

        if (!nv.getPassword().equals(password)) {
            throw new BusinessException("Mật khẩu không đúng");
        }

        // Tạo DTO
        NhanVienDTO dto = new NhanVienDTO();
        dto.setMaNV(nv.getMaNV());
        dto.setTenNV(nv.getTenNV());

        // Xử lý Enum ChucVuNhanVien → chuyển thành String
        if (nv.getChucVu() != null) {
            dto.setChucVu(nv.getChucVu().name()); // Ví dụ: "LE_TAN", "QUAN_LY", "ADMIN"
        } else {
            dto.setChucVu("NHAN_VIEN");
        }

        if (nv.getSoDienThoai() != null) {
            dto.setSoDienThoai(nv.getSoDienThoai());
        }

        return dto;
    }
}