package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.*;

import com.hotelmanagement.service.ChiTietDatPhongService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.ChiTietDatPhongDAO;
import com.hotelmanagement.model.dao.DatPhongDAO;
import com.hotelmanagement.model.dto.ChiTietDatPhongDTO;
import com.hotelmanagement.model.entity.ChiTietDatPhong;
import com.hotelmanagement.model.entity.DatPhong;
import com.hotelmanagement.model.entity.Phong;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.keyclass.ChiTietDatPhongId;
import com.hotelmanagement.mapper.Mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl;
import com.hotelmanagement.model.dao.impl.DatPhongDAOImpl;

/**
 * Service xử lý logic chi tiết đặt phòng (thêm/xóa/sửa phòng trong một đặt
 * phòng)
 */
public class ChiTietDatPhongServiceImpl implements ChiTietDatPhongService {

    private final ChiTietDatPhongDAO chiTietDAO = new ChiTietDatPhongDAOImpl();
    private final DatPhongDAO datPhongDAO = new DatPhongDAOImpl();
    private final PhongService phongService = new PhongServiceImpl();

    private static final int GIO_TOI_THIEU = 2;

    /**
     * Kiểm tra thời gian thuê phòng có hợp lệ không
     */
    private void validateThoiGian(LocalDateTime ngayNhan, LocalDateTime ngayTra) {
        if (ngayNhan == null || ngayTra == null) {
            throw new ValidationException("Ngày nhận và ngày trả không được để trống");
        }

        if (!ngayNhan.isBefore(ngayTra) && !ngayNhan.isEqual(ngayTra)) {
            throw new ValidationException("Ngày nhận phải trước hoặc bằng ngày trả");
        }

        // Cho phép nhận phòng NGAY LẬP TỨC (không bắt buộc phải sau now)
        // Chỉ kiểm tra ngày trả phải sau ngày nhận
        if (ngayTra.isBefore(ngayNhan)) {
            throw new ValidationException("Ngày trả phải sau ngày nhận");
        }

        Duration duration = Duration.between(ngayNhan, ngayTra);
        if (duration.toHours() < GIO_TOI_THIEU) {
            throw new ValidationException("Thời gian thuê tối thiểu là " + GIO_TOI_THIEU + " giờ");
        }
    }

    /**
     * Thêm một phòng vào đặt phòng
     */
    public ChiTietDatPhongDTO themPhong(Long maDP, String maPhong,
            LocalDateTime ngayNhan, LocalDateTime ngayTra) {

        validateThoiGian(ngayNhan, ngayTra);

        DatPhong dp = datPhongDAO.findByMaDP(maDP);
        if (dp == null) {
            throw new BusinessException("Không tìm thấy đặt phòng với mã: " + maDP);
        }

        Phong phong = phongService.timPhongTheoMaPhong(maPhong);

        // Kiểm tra trùng lịch
        boolean isConflict = chiTietDAO.findByPhong(maPhong).stream()
                .anyMatch(ctp -> !ctp.getDatPhong().getMaDP().equals(maDP) &&
                        !(ngayTra.isBefore(ctp.getNgayNhan()) || ngayNhan.isAfter(ctp.getNgayTra())));

        if (isConflict) {
            throw new BusinessException("Phòng " + maPhong + " đã được đặt trong khoảng thời gian này");
        }

        ChiTietDatPhong ctp = new ChiTietDatPhong();
        ctp.setDatPhong(dp);
        ctp.setPhong(phong);
        ctp.setNgayNhan(ngayNhan);
        ctp.setNgayTra(ngayTra);
        ctp.setTrangThai(TinhTrangPhong.DA_DAT); // Ban đầu là ĐÃ ĐẶT

        chiTietDAO.save(ctp);

        // Cập nhật trạng thái phòng mặc định là DA_DAT
        phongService.capNhatTrangThaiPhong(maPhong, TinhTrangPhong.DA_DAT);

        return Mapper.toChiTietDatPhongDTO(ctp);
    }

    /**
     * Xóa một phòng khỏi đặt phòng
     */
    public void xoaPhong(Long maDP, String maPhong) {
        ChiTietDatPhongId id = new ChiTietDatPhongId(maDP, maPhong);

        ChiTietDatPhong ctp = chiTietDAO.findById(id);
        if (ctp == null) {
            throw new BusinessException("Không tìm thấy chi tiết đặt phòng cho phòng " + maPhong);
        }
        
        chiTietDAO.delete(id);

        // Kiểm tra xem phòng có còn booking nào khác không
        java.util.List<ChiTietDatPhong> remaining = chiTietDAO.findByPhong(maPhong);
        boolean hasDangO = remaining.stream().anyMatch(c -> c.getTrangThai() == TinhTrangPhong.DANG_O);
        boolean hasDaDat = remaining.stream().anyMatch(c -> c.getTrangThai() == TinhTrangPhong.DA_DAT);

        if (hasDangO) {
            phongService.capNhatTrangThaiPhong(maPhong, TinhTrangPhong.DANG_O);
        } else if (hasDaDat) {
            phongService.capNhatTrangThaiPhong(maPhong, TinhTrangPhong.DA_DAT);
        } else {
            phongService.capNhatTrangThaiPhong(maPhong, TinhTrangPhong.SAN_SANG);
        }
    }

    /**
     * Lấy danh sách phòng của một đặt phòng
     */
    public List<ChiTietDatPhongDTO> findByDatPhong(Long maDP) {
        DatPhong dp = datPhongDAO.findByMaDP(maDP);
        if (dp == null) {
            throw new BusinessException("Không tìm thấy đặt phòng với mã: " + maDP);
        }

        return dp.getChiTietDatPhongs().stream()
                .map(Mapper::toChiTietDatPhongDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thời gian thuê của một phòng
     */
    public ChiTietDatPhongDTO updateThoiGian(Long maDP, String maPhong,
            LocalDateTime ngayNhan, LocalDateTime ngayTra) {

        validateThoiGian(ngayNhan, ngayTra);

        ChiTietDatPhongId id = new ChiTietDatPhongId(maDP, maPhong);
        ChiTietDatPhong ctp = chiTietDAO.findById(id);

        if (ctp == null) {
            throw new BusinessException("Không tìm thấy chi tiết đặt phòng cho phòng " + maPhong);
        }

        ctp.setNgayNhan(ngayNhan);
        ctp.setNgayTra(ngayTra);

        chiTietDAO.updateThoiGian(maDP, maPhong, ngayNhan, ngayTra);

        return Mapper.toChiTietDatPhongDTO(ctp);
    }

    /**
     * Cập nhật trạng thái đặt phòng (dùng nội bộ)
     */
    public void capNhatTrangThaiDatPhong(Long maDP, TinhTrangPhong trangThaiMoi) {
        chiTietDAO.updateTrangThaiByMaDP(maDP, trangThaiMoi);
    }

    // Cập nhật thời gian đặt phòng
    public void capNhatThoiGianDatPhong(Long maDP, String maPhong, LocalDateTime in, LocalDateTime out) {
        ChiTietDatPhongId id = new ChiTietDatPhongId(maDP, maPhong);
        ChiTietDatPhong ctp = chiTietDAO.findById(id);
        if (ctp == null) {
            throw new BusinessException("Không tìm thấy chi tiết đặt phòng cho phòng " + maPhong);
        }
        if (ctp.getSoLanChuyenPhong() < 1) {
            ctp.setNgayNhan(in);
            ctp.setNgayTra(out);
            ctp.setSoLanChuyenPhong(ctp.getSoLanChuyenPhong() + 1);
            
            // Dùng hàm cập nhật nguyên object
            new com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl().update(ctp);
        } else {
            throw new BusinessException("Không thể cập nhật lịch vì bạn đã đổi lịch vượt quá số lần quy định (Tối đa 1 lần)");
        }
    }
}
