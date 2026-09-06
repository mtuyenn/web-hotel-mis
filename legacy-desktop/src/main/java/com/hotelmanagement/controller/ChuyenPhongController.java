package com.hotelmanagement.controller;

import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dto.ChuyenPhongDTO;
import com.hotelmanagement.service.ChuyenPhongService;

import java.util.List;
import com.hotelmanagement.service.impl.ChuyenPhongServiceImpl;

/**
 * Lớp ChuyenPhongController dùng để xử lý các yêu cầu liên quan
 * đến chức năng chuyển phòng của khách hàng.
 * 
 * Lớp này thực hiện kiểm tra dữ liệu đầu vào và gọi Service
 * để xử lý nghiệp vụ chuyển phòng.
 */
public class ChuyenPhongController {

    // Khởi tạo service xử lý nghiệp vụ
    private final ChuyenPhongService chuyenPhongService = new ChuyenPhongServiceImpl();

    /**
     * Thực hiện chuyển phòng dựa trên thông tin đầu vào
     */
    public ChuyenPhongDTO chuyenPhong(Long maDP, String maPhongCu, String maPhongMoi, String lyDo) {

        // Kiểm tra dữ liệu đầu vào
        if (maDP == null)
            throw new ValidationException("Mã đặt phòng không được để trống");

        if (maPhongCu == null || maPhongMoi == null)
            throw new ValidationException("Phòng cũ và phòng mới không được để trống");

        // Gọi service để xử lý chuyển phòng
        return chuyenPhongService.chuyenPhong(maDP, maPhongCu, maPhongMoi, lyDo);
    }

    /**
     * Lấy danh sách chuyển phòng theo mã đặt phòng
     */
    public List<ChuyenPhongDTO> layTheoDatPhong(Long maDP) {

        // Kiểm tra dữ liệu đầu vào
        if (maDP == null)
            throw new ValidationException("Mã đặt phòng không được để trống");

        // Gọi service lấy dữ liệu
        return chuyenPhongService.findByDatPhong(maDP);
    }

    /**
     * Lấy toàn bộ danh sách chuyển phòng
     */
    public List<ChuyenPhongDTO> layTatCa() {
        return chuyenPhongService.findAll();
    }

    /**
     * Tìm thông tin chuyển phòng theo mã
     */
    public ChuyenPhongDTO timTheoId(Long maChuyenPhong) {

        // Kiểm tra dữ liệu đầu vào
        if (maChuyenPhong == null)
            throw new ValidationException("Mã chuyển phòng không được để trống");

        // Gọi service tìm kiếm
        return chuyenPhongService.findById(maChuyenPhong);
    }

    /**
     * Thực hiện chuyển phòng dựa trên DTO
     */
    public ChuyenPhongDTO chuyenPhong(ChuyenPhongDTO dto) {

        // Kiểm tra dữ liệu đầu vào
        if (dto == null)
            throw new ValidationException("Dữ liệu chuyển phòng không được để trống");

        // Gọi service xử lý
        return chuyenPhongService.chuyenPhong(
                dto.getMaDP(),
                dto.getMaPhongCu(),
                dto.getMaPhongMoi(),
                dto.getLyDo());
    }
}