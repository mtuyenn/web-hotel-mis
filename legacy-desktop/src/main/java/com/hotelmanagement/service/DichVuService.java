package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.DichVuDTO;
import java.util.List;

/**
 * Interface dùng để xử lý nghiệp vụ dịch vụ:
 * bao gồm quản lý dịch vụ và lưu thông tin dịch vụ đã sử dụng
 */
public interface DichVuService {

    // Lấy toàn bộ danh sách dịch vụ
    public List<DichVuDTO> layTatCaDichVu();

    // Tìm dịch vụ theo mã dịch vụ
    public DichVuDTO timDichVuTheoMa(String maDV);

    // Thêm mới dịch vụ
    public void themDichVu(DichVuDTO dto);

    // Cập nhật thông tin dịch vụ
    public void capNhatDichVu(DichVuDTO dto);

    // Xóa dịch vụ theo mã
    public void xoaDichVu(String maDV);

    // Lấy danh sách dịch vụ đã sử dụng theo mã đặt phòng
    public java.util.List<com.hotelmanagement.model.dto.ChiTietDichVuDTO> layDichVuDaDung(Long maDP);

    // Lưu danh sách dịch vụ đã sử dụng cho đặt phòng
    public void luuDichVuSuDung(Long maDP,
            java.util.List<com.hotelmanagement.model.dto.ChiTietDichVuDTO> selectedServices);
}