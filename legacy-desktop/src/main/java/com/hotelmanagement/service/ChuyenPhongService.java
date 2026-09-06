package com.hotelmanagement.service;

import com.hotelmanagement.model.dto.ChuyenPhongDTO;
import java.util.List;

/**
 * Interface dùng để xử lý nghiệp vụ chuyển phòng:
 * bao gồm thực hiện chuyển phòng và truy vấn thông tin chuyển phòng
 */
public interface ChuyenPhongService {

    // Thực hiện chuyển phòng từ phòng cũ sang phòng mới
    public ChuyenPhongDTO chuyenPhong(Long maDP, String maPhongCu, String maPhongMoi, String lyDo);

    // Lấy danh sách chuyển phòng theo mã đặt phòng
    public List<ChuyenPhongDTO> findByDatPhong(Long maDP);

    // Lấy toàn bộ danh sách chuyển phòng
    public List<ChuyenPhongDTO> findAll();

    // Tìm thông tin chuyển phòng theo mã
    public ChuyenPhongDTO findById(Long maChuyenPhong);
}