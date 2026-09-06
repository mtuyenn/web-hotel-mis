package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.*;

import com.hotelmanagement.service.ChuyenPhongService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.*;
import com.hotelmanagement.model.dto.ChuyenPhongDTO;
import com.hotelmanagement.model.entity.*;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.keyclass.ChiTietDatPhongId;
import com.hotelmanagement.mapper.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import com.hotelmanagement.model.dao.impl.PhongDAOImpl;
import com.hotelmanagement.model.dao.impl.ChuyenPhongDAOImpl;
import com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl;
import com.hotelmanagement.model.dao.impl.DatPhongDAOImpl;

/**
 * Service xử lý logic chuyển phòng
 */
public class ChuyenPhongServiceImpl implements ChuyenPhongService {

    private final ChuyenPhongDAO chuyenPhongDAO = new ChuyenPhongDAOImpl();
    private final PhongDAO phongDAO = new PhongDAOImpl();
    private final DatPhongDAO datPhongDAO = new DatPhongDAOImpl();
    private final PhongService phongService = new PhongServiceImpl();
    private final ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAOImpl();

    /**
     * Thực hiện chuyển phòng cho một đặt phòng
     */
    public ChuyenPhongDTO chuyenPhong(Long maDP, String maPhongCu, String maPhongMoi, String lyDo) {
        if (maDP == null)
            throw new ValidationException("Mã đặt phòng không được để trống");
        if (maPhongCu == null || maPhongMoi == null) {
            throw new ValidationException("Phòng cũ và phòng mới không được để trống");
        }
        if (maPhongCu.equals(maPhongMoi)) {
            throw new ValidationException("Phòng mới phải khác phòng cũ");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new ValidationException("Lý do chuyển phòng không được để trống");
        }

        DatPhong dp = datPhongDAO.findByMaDP(maDP);
        if (dp == null) {
            throw new BusinessException("Không tìm thấy đặt phòng với mã: " + maDP);
        }

        Phong phongCu = phongDAO.findByMaPhong(maPhongCu);
        Phong phongMoi = phongDAO.findByMaPhong(maPhongMoi);

        if (phongCu == null || phongMoi == null) {
            throw new BusinessException("Phòng cũ hoặc phòng mới không tồn tại");
        }

        // Kiểm tra phòng mới có sẵn sàng không
        if (phongMoi.getTinhTrang() != TinhTrangPhong.SAN_SANG &&
                phongMoi.getTinhTrang() != TinhTrangPhong.DANG_DON_DEP) {
            throw new BusinessException("Phòng mới không sẵn sàng để chuyển vào");
        }

        ChiTietDatPhong ctpCu = chiTietDatPhongDAO.findByMaDPAndMaPhong(maDP, maPhongCu);
        if (ctpCu == null) {
            throw new BusinessException("Không tìm thấy chi tiết đặt phòng cho phòng cũ");
        }

        // Tạo record chuyển phòng
        ChuyenPhong cp = new ChuyenPhong();
        cp.setDatPhong(dp);
        cp.setPhongCu(phongCu);
        cp.setPhongMoi(phongMoi);
        cp.setNgayChuyen(LocalDateTime.now());
        cp.setLyDo(lyDo);

        chuyenPhongDAO.save(cp);

        // Cập nhật trạng thái phòng
        phongService.capNhatTrangThaiPhong(maPhongCu, TinhTrangPhong.SAN_SANG);
        phongService.capNhatTrangThaiPhong(maPhongMoi, TinhTrangPhong.DANG_O);

        // Cập nhật ChiTietDatPhong: xóa cũ → tạo mới
        ChiTietDatPhongId idCu = new ChiTietDatPhongId(maDP, maPhongCu);
        chiTietDatPhongDAO.delete(idCu);

        ChiTietDatPhong ctpMoi = new ChiTietDatPhong();
        ctpMoi.setDatPhong(dp);
        ctpMoi.setPhong(phongMoi);
        ctpMoi.setNgayNhan(ctpCu.getNgayNhan());
        ctpMoi.setNgayTra(ctpCu.getNgayTra());
        ctpMoi.setTrangThai(ctpCu.getTrangThai());
        ctpMoi.setSoLanChuyenPhong(ctpCu.getSoLanChuyenPhong() + 1);

        chiTietDatPhongDAO.save(ctpMoi);

        return Mapper.toChuyenPhongDTO(cp);
    }

    public List<ChuyenPhongDTO> findByDatPhong(Long maDP) {
        return chuyenPhongDAO.findByDatPhong(maDP).stream()
                .map(Mapper::toChuyenPhongDTO)
                .toList();
    }

    public List<ChuyenPhongDTO> findAll() {
        return chuyenPhongDAO.findAll().stream()
                .map(Mapper::toChuyenPhongDTO)
                .toList();
    }

    public ChuyenPhongDTO findById(Long maChuyenPhong) {
        ChuyenPhong cp = chuyenPhongDAO.findById(maChuyenPhong);
        if (cp == null) {
            throw new BusinessException("Không tìm thấy lịch chuyển phòng với mã: " + maChuyenPhong);
        }
        return Mapper.toChuyenPhongDTO(cp);
    }
}
