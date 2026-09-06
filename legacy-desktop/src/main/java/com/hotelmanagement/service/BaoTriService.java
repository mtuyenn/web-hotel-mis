package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.BaoTriDTO;
import com.hotelmanagement.model.enums.TinhTrangBaoTri;

import java.util.List;

/**
 * Interface dùng để xử lý nghiệp vụ bảo trì:
 * bao gồm tạo mới, truy vấn và cập nhật trạng thái bảo trì
 */
public interface BaoTriService {

    // Tạo mới một yêu cầu bảo trì
    public BaoTriDTO taoBaoTri(BaoTriDTO baoTriDTO);

    // Tìm bảo trì theo mã bảo trì
    public BaoTriDTO findById(String maBT);

    // Lấy danh sách bảo trì theo mã phòng
    public List<BaoTriDTO> findByPhong(String maPhong);

    // Lấy toàn bộ danh sách bảo trì
    public List<BaoTriDTO> findAll();

    // Lấy danh sách các loại bảo trì
    public List<String> layDanhSachLoaiBaoTri();

    // Cập nhật trạng thái bảo trì
    public BaoTriDTO capNhatTrangThaiBaoTri(String maBT, TinhTrangBaoTri trangThaiMoi, String moTaThem);

    // Hủy yêu cầu bảo trì
    public void huyBaoTri(String maBT);
}