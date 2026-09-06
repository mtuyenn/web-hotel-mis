package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.PhongDTO;

import com.hotelmanagement.model.entity.LoaiPhong;
import com.hotelmanagement.model.entity.Phong;
import com.hotelmanagement.model.enums.TinhTrangPhong;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for PhongService
 */
public interface PhongService {
    public List<PhongDTO> timTatCaPhongDTO();

    public List<PhongDTO> timPhongTrong(LocalDateTime ngayNhan, LocalDateTime ngayTra, LoaiPhong loaiPhong);

    public PhongDTO timPhongTheoMaPhongDTO(String maPhong);

    public Phong timPhongTheoMaPhong(String maPhong);

    public PhongDTO taoPhong(PhongDTO dto);

    public void capNhatTrangThaiPhong(String maPhong, TinhTrangPhong tinhTrangMoi);
}
