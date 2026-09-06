package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.*;

import com.hotelmanagement.service.BaoTriService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.BaoTriDAO;
import com.hotelmanagement.model.dao.ChiTietDatPhongDAO;
import com.hotelmanagement.model.dao.PhongDAO;
import com.hotelmanagement.model.dto.BaoTriDTO;
import com.hotelmanagement.model.entity.BaoTri;
import com.hotelmanagement.model.entity.ChiTietDatPhong;
import com.hotelmanagement.model.entity.Phong;
import com.hotelmanagement.model.enums.TinhTrangBaoTri;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.mapper.Mapper;

import java.util.List;
import java.util.stream.Collectors;
import com.hotelmanagement.model.dao.impl.PhongDAOImpl;
import com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl;
import com.hotelmanagement.model.dao.impl.BaoTriDAOImpl;

/**
 * Service xử lý logic liên quan đến Bảo trì phòng
 */
public class BaoTriServiceImpl implements BaoTriService {

    private final BaoTriDAO baoTriDAO = new BaoTriDAOImpl();
    private final PhongDAO phongDAO = new PhongDAOImpl();
    private final PhongService phongService = new PhongServiceImpl();
    private final ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAOImpl();

    /**
     * Tạo lịch bảo trì mới
     */
    public BaoTriDTO taoBaoTri(BaoTriDTO baoTriDTO) {
        if (baoTriDTO == null || baoTriDTO.getMaPhong() == null || baoTriDTO.getMaPhong().isBlank()) {
            throw new ValidationException("Mã phòng không được để trống");
        }

        Phong phong = phongDAO.findByMaPhong(baoTriDTO.getMaPhong());
        if (phong == null) {
            throw new BusinessException("Phòng không tồn tại: " + baoTriDTO.getMaPhong());
        }

        // Kiểm tra phòng có đang được sử dụng không
        List<ChiTietDatPhong> list = chiTietDatPhongDAO.findByPhong(phong.getMaPhong());
        boolean dangDuocSuDung = list.stream().anyMatch(ct -> ct.getTrangThai() == TinhTrangPhong.DA_DAT ||
                ct.getTrangThai() == TinhTrangPhong.DANG_O);

        if (dangDuocSuDung) {
            throw new BusinessException("Phòng đang được sử dụng hoặc đã đặt, không thể tạo lịch bảo trì");
        }

        BaoTri baoTri = Mapper.toBaoTri(baoTriDTO, phong);
        baoTri.setTinhTrangBTri(TinhTrangBaoTri.DANG_BAO_TRI);

        if (baoTri.getNgayBaoTri() == null) {
            baoTri.setNgayBaoTri(java.time.LocalDate.now());
        }

        // Tạo mã ID ngẫu nhiên vì MABT không có tự động GeneratedValue (Max len: 10)
        String generatedMaBT = "BT"
                + java.util.UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8).toUpperCase();
        baoTri.setMaBT(generatedMaBT);

        baoTriDAO.save(baoTri);
        phongService.capNhatTrangThaiPhong(phong.getMaPhong(), TinhTrangPhong.BAO_TRI);

        return Mapper.toBaoTriDTO(baoTri);
    }

    public BaoTriDTO findById(String maBT) {
        if (maBT == null || maBT.isBlank()) {
            throw new ValidationException("Mã bảo trì không được để trống");
        }

        BaoTri bt = baoTriDAO.findById(maBT);
        if (bt == null) {
            throw new BusinessException("Không tìm thấy lịch bảo trì với mã: " + maBT);
        }
        return Mapper.toBaoTriDTO(bt);
    }

    public List<BaoTriDTO> findByPhong(String maPhong) {
        return baoTriDAO.findByPhong(maPhong).stream()
                .map(Mapper::toBaoTriDTO)
                .collect(Collectors.toList());
    }

    public List<BaoTriDTO> findAll() {
        return baoTriDAO.findAll().stream()
                .map(Mapper::toBaoTriDTO)
                .collect(Collectors.toList());
    }

    public List<String> layDanhSachLoaiBaoTri() {
        return baoTriDAO.layDanhSachLoaiBaoTri();
    }

    public BaoTriDTO capNhatTrangThaiBaoTri(String maBT, TinhTrangBaoTri trangThaiMoi, String moTaThem) {
        if (maBT == null || maBT.isBlank()) {
            throw new ValidationException("Mã bảo trì không được để trống");
        }

        BaoTri bt = baoTriDAO.findById(maBT);
        if (bt == null) {
            throw new BusinessException("Không tìm thấy lịch bảo trì với mã: " + maBT);
        }

        bt.setTinhTrangBTri(trangThaiMoi);
        if (moTaThem != null && !moTaThem.isBlank()) {
            bt.setMoTa(bt.getMoTa() != null ? bt.getMoTa() + " | " + moTaThem : moTaThem);
        }

        baoTriDAO.update(bt);

        if (trangThaiMoi == TinhTrangBaoTri.DA_HOAN_THANH) {
            phongService.capNhatTrangThaiPhong(bt.getPhong().getMaPhong(), TinhTrangPhong.SAN_SANG);
        }

        return Mapper.toBaoTriDTO(bt);
    }

    public void huyBaoTri(String maBT) {
        if (maBT == null || maBT.isBlank()) {
            throw new ValidationException("Mã bảo trì không hợp lệ");
        }

        BaoTri bt = baoTriDAO.findById(maBT);
        if (bt == null) {
            throw new BusinessException("Không tìm thấy lịch bảo trì");
        }

        phongService.capNhatTrangThaiPhong(bt.getPhong().getMaPhong(), TinhTrangPhong.SAN_SANG);
    }
}
