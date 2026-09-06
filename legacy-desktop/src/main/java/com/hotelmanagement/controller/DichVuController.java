package com.hotelmanagement.controller;

import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dto.DichVuDTO;
import com.hotelmanagement.service.DichVuService;

import java.util.List;
import com.hotelmanagement.service.impl.DichVuServiceImpl;

/**
 * Lớp DichVuController dùng để xử lý các chức năng liên quan
 * đến dịch vụ trong hệ thống khách sạn.
 * 
 * Lớp này tiếp nhận dữ liệu từ giao diện, kiểm tra dữ liệu đầu vào
 * và gọi Service để thực hiện các nghiệp vụ tương ứng.
 */
public class DichVuController {

    // Khởi tạo service xử lý nghiệp vụ dịch vụ
    private final DichVuService dichVuService = new DichVuServiceImpl();

    /**
     * Lấy toàn bộ danh sách dịch vụ
     */
    public List<DichVuDTO> layTatCaDichVu() {
        return dichVuService.layTatCaDichVu();
    }

    /**
     * Tìm dịch vụ theo mã dịch vụ
     */
    public DichVuDTO timDichVuTheoMa(String maDV) {

        // Kiểm tra mã dịch vụ có hợp lệ hay không
        if (maDV == null || maDV.isBlank()) {
            throw new ValidationException("Mã dịch vụ không được để trống");
        }

        return dichVuService.timDichVuTheoMa(maDV);
    }

    /**
     * Thêm mới một dịch vụ
     */
    public void taoDichVu(DichVuDTO dto) {

        // Kiểm tra dữ liệu đầu vào
        if (dto == null)
            throw new ValidationException("Dữ liệu dịch vụ không được để trống");

        if (dto.getMaDV() == null || dto.getMaDV().isBlank())
            throw new ValidationException("Mã dịch vụ không được trống");

        if (dto.getTenDichVu() == null || dto.getTenDichVu().isBlank())
            throw new ValidationException("Tên dịch vụ không được trống");

        if (dto.getGiaDV() == null || dto.getGiaDV().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ValidationException("Giá dịch vụ không hợp lệ");
        }

        // Gọi service để thêm dịch vụ
        dichVuService.themDichVu(dto);
    }

    /**
     * Cập nhật thông tin dịch vụ
     */
    public void capNhatDichVu(DichVuDTO dto) {

        // Kiểm tra dữ liệu cập nhật
        if (dto == null)
            throw new ValidationException("Dữ liệu cập nhật không được để trống");

        if (dto.getMaDV() == null || dto.getMaDV().isBlank())
            throw new ValidationException("Mã dịch vụ không được trống");

        if (dto.getTenDichVu() == null || dto.getTenDichVu().isBlank())
            throw new ValidationException("Tên dịch vụ không được trống");

        if (dto.getGiaDV() == null || dto.getGiaDV().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ValidationException("Giá dịch vụ không hợp lệ");
        }

        // Gọi service để cập nhật dịch vụ
        dichVuService.capNhatDichVu(dto);
    }

    /**
     * Xóa dịch vụ theo mã
     */
    public void xoaDichVu(String maDV) {

        // Kiểm tra mã dịch vụ
        if (maDV == null || maDV.isBlank())
            throw new ValidationException("Mã dịch vụ không được để trống");

        // Gọi service để xóa dịch vụ
        dichVuService.xoaDichVu(maDV);
    }

    /**
     * Lấy danh sách dịch vụ mà khách đã sử dụng theo mã đặt phòng
     */
    public List<com.hotelmanagement.model.dto.ChiTietDichVuDTO> layDichVuDaDung(Long maDP) {

        // Nếu mã đặt phòng null thì trả về danh sách rỗng
        if (maDP == null)
            return new java.util.ArrayList<>();

        return dichVuService.layDichVuDaDung(maDP);
    }

    /**
     * Lưu danh sách dịch vụ mà khách đã sử dụng
     */
    public void luuDichVuSuDung(Long maDP, List<com.hotelmanagement.model.dto.ChiTietDichVuDTO> selectedServices) {

        // Kiểm tra mã đặt phòng
        if (maDP == null)
            throw new ValidationException("Mã đặt phòng không hợp lệ");

        // Gọi service để lưu dữ liệu dịch vụ sử dụng
        dichVuService.luuDichVuSuDung(maDP, selectedServices);
    }
}