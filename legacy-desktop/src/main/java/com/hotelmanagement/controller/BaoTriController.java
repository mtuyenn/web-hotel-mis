package com.hotelmanagement.controller;

import com.hotelmanagement.model.dto.BaoTriDTO;
import com.hotelmanagement.model.enums.TinhTrangBaoTri;
import com.hotelmanagement.service.BaoTriService;

import java.util.List;
import com.hotelmanagement.service.impl.BaoTriServiceImpl;


public class BaoTriController {

    private final BaoTriService baoTriService = new BaoTriServiceImpl();

    public BaoTriDTO taoBaoTri(BaoTriDTO dto) {
        return baoTriService.taoBaoTri(dto);
    }

    public List<BaoTriDTO> layDanhSachBaoTri(String maPhong) {
        return baoTriService.findByPhong(maPhong);
    }

    // Tìm lịch bảo trì theo mã
    public BaoTriDTO timBaoTriTheoMa(String maBT) {
        return baoTriService.findById(maBT);
    }

    // Cập nhật trạng thái bảo trì
    public BaoTriDTO capNhatTrangThaiBaoTri(String maBT, String trangThaiMoi, String moTaThem) {
        TinhTrangBaoTri ttEnum = TinhTrangBaoTri.valueOf(trangThaiMoi);
        return baoTriService.capNhatTrangThaiBaoTri(maBT, ttEnum, moTaThem);
    }

    // Hủy lịch bảo trì
    public void huyBaoTri(String maBT) {
        baoTriService.huyBaoTri(maBT);
    }

    // Lấy tất cả lịch bảo trì
    public List<BaoTriDTO> layTatCaBaoTri() {
        return baoTriService.findAll();
    }

    // Lấy danh sách các loại bảo trì độc nhất từ DB
    public List<String> layDanhSachLoaiBaoTri() {
        return baoTriService.layDanhSachLoaiBaoTri();
    }
}
