package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.NhanVienDTO;

import com.hotelmanagement.model.enums.ChucVuNhanVien;
import java.util.List;

/**
 * Interface for NhanVienService
 */
public interface NhanVienService {
    public List<NhanVienDTO> findByChucVu(ChucVuNhanVien chucVu);

    public NhanVienDTO findByMaNV(String maNV);

    public NhanVienDTO addNhanVien(NhanVienDTO dto);

    public NhanVienDTO updateNhanVien(NhanVienDTO dto);

    public void deleteNhanVien(String maNV);

    public List<NhanVienDTO> getAllNhanVien();
}
