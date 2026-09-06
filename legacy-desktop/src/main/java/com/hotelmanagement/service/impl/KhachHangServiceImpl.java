package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.KhachHangService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.KhachHangDAO;
import com.hotelmanagement.model.dto.KhachHangDTO;
import com.hotelmanagement.model.entity.KhachHang;
import com.hotelmanagement.mapper.Mapper;

import java.util.List;
import com.hotelmanagement.model.dao.impl.KhachHangDAOImpl;

/**
 * Service xử lý logic liên quan đến Khách hàng
 */
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangDAO khachHangDAO = new KhachHangDAOImpl();

    /**
     * Thêm khách hàng mới
     */
    public void themKhachHang(KhachHangDTO dto) {
        if (dto == null) {
            throw new ValidationException("Dữ liệu khách hàng không được để trống");
        }

        if (dto.getCccd() == null || dto.getCccd().isBlank()) {
            throw new ValidationException("Căn cước công dân không được để trống");
        }

        if (kiemTraTrungCCCD(dto.getCccd())) {
            throw new BusinessException("CCCD này đã tồn tại trong hệ thống");
        }

        KhachHang kh = Mapper.toKhachHangEntity(dto);
        khachHangDAO.save(kh);
    }

    /**
     * Kiểm tra CCCD đã tồn tại chưa
     */
    public boolean kiemTraTrungCCCD(String cccd) {
        if (cccd == null || cccd.isBlank())
            return false;
        return khachHangDAO.findByCccd(cccd) != null;
    }

    /**
     * Tìm khách hàng theo CCCD
     */
    public KhachHangDTO timKhachHangByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            throw new ValidationException("CCCD không được để trống");
        }

        KhachHang kh = khachHangDAO.findByCccd(cccd);
        if (kh == null) {
            throw new BusinessException("Không tìm thấy khách hàng với CCCD: " + cccd);
        }

        return Mapper.toKhachHangDTO(kh);
    }

    public List<KhachHangDTO> layTatCaKhachHang() {
        return khachHangDAO.findAll().stream()
                .map(Mapper::toKhachHangDTO)
                .toList();
    }

    public void capNhatKhachHang(KhachHangDTO dto) {
        if (dto == null || dto.getCccd() == null || dto.getCccd().isBlank()) {
            throw new ValidationException("Dữ liệu cập nhật không hợp lệ");
        }

        KhachHang existing = khachHangDAO.findByCccd(dto.getCccd());
        if (existing == null) {
            throw new BusinessException("Khách hàng không tồn tại");
        }

        KhachHang kh = Mapper.toKhachHangEntity(dto);
        khachHangDAO.save(kh);
    }

    public void xoaKhachHang(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            throw new ValidationException("CCCD không hợp lệ");
        }

        KhachHang kh = khachHangDAO.findByCccd(cccd);
        if (kh == null) {
            throw new BusinessException("Không tìm thấy khách hàng để xóa");
        }

        khachHangDAO.delete(cccd);
    }
}