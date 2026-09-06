package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.NhanVienService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.HoaDonDAO;
import com.hotelmanagement.model.dao.NhanVienDAO;
import com.hotelmanagement.model.dto.NhanVienDTO;
import com.hotelmanagement.model.entity.NhanVien;
import com.hotelmanagement.mapper.Mapper;
import com.hotelmanagement.model.enums.ChucVuNhanVien;

import java.util.List;
import com.hotelmanagement.model.dao.impl.NhanVienDAOImpl;
import com.hotelmanagement.model.dao.impl.HoaDonDAOImpl;

/**
 * Service xử lý logic liên quan đến Nhân viên
 */
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAOImpl();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();

    public List<NhanVienDTO> findByChucVu(ChucVuNhanVien chucVu) {
        if (chucVu == null)
            return List.of();

        return nhanVienDAO.findAll().stream()
                .filter(nv -> nv.getChucVu() == chucVu)
                .map(Mapper::toNhanVienDTO)
                .toList();
    }

    public NhanVienDTO findByMaNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            throw new ValidationException("Mã nhân viên không được để trống");
        }

        NhanVien nv = nhanVienDAO.findByMaNV(maNV);
        if (nv == null) {
            throw new BusinessException("Không tìm thấy nhân viên với mã: " + maNV);
        }
        return Mapper.toNhanVienDTO(nv);
    }

    public NhanVienDTO addNhanVien(NhanVienDTO dto) {
        if (dto == null)
            throw new ValidationException("Dữ liệu nhân viên không được null");

        NhanVien nv = Mapper.toNhanVienEntity(dto);
        validateNhanVien(nv);

        if (nhanVienDAO.findByMaNV(nv.getMaNV()) != null) {
            throw new BusinessException("Mã nhân viên đã tồn tại");
        }

        nhanVienDAO.save(nv);
        return Mapper.toNhanVienDTO(nv);
    }

    public NhanVienDTO updateNhanVien(NhanVienDTO dto) {
        if (dto == null || dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new ValidationException("Mã nhân viên không hợp lệ");
        }

        NhanVien existing = nhanVienDAO.findByMaNV(dto.getMaNV());
        if (existing == null) {
            throw new BusinessException("Không tìm thấy nhân viên để cập nhật");
        }

        NhanVien nv = Mapper.toNhanVienEntity(dto);
        validateNhanVien(nv);

        nhanVienDAO.save(nv);
        return Mapper.toNhanVienDTO(nv);
    }

    public void deleteNhanVien(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            throw new ValidationException("Mã nhân viên không hợp lệ");
        }

        NhanVien nv = nhanVienDAO.findByMaNV(maNV);
        if (nv == null) {
            throw new BusinessException("Nhân viên không tồn tại");
        }

        if (hoaDonDAO.existsByNhanVien(maNV)) {
            throw new BusinessException("Không thể xóa nhân viên vì đang có hóa đơn liên quan");
        }

        nhanVienDAO.delete(maNV);
    }

    private void validateNhanVien(NhanVien nv) {
        if (nv.getMaNV() == null || nv.getMaNV().trim().isEmpty()) {
            throw new ValidationException("Mã nhân viên không được để trống");
        }
        if (nv.getTenNV() == null || nv.getTenNV().trim().isEmpty()) {
            throw new ValidationException("Tên nhân viên không được để trống");
        }
        if (nv.getChucVu() == null) {
            throw new ValidationException("Chức vụ không được để trống");
        }
        if (nv.getSoDienThoai() != null && !nv.getSoDienThoai().matches("\\d{10}")) {
            throw new ValidationException("Số điện thoại phải gồm 10 chữ số");
        }
    }

    @Override
    public List<NhanVienDTO> getAllNhanVien() {
        return nhanVienDAO.findAll().stream()
                .map(Mapper::toNhanVienDTO)
                .toList();
    }
}