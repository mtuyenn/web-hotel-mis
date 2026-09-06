package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.*;

import com.hotelmanagement.service.DatPhongService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.*;
import com.hotelmanagement.model.dto.*;
import com.hotelmanagement.model.entity.*;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.mapper.Mapper;
import com.hotelmanagement.model.keyclass.ChiTietDatPhongId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.hotelmanagement.model.dao.impl.HoaDonDAOImpl;
import com.hotelmanagement.model.dao.impl.KhachHangDAOImpl;
import com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl;
import com.hotelmanagement.model.dao.impl.DatPhongDAOImpl;
import com.hotelmanagement.model.dao.impl.NhanVienDAOImpl;

/**
 * Service chính xử lý logic Đặt phòng
 */
public class DatPhongServiceImpl implements DatPhongService {

    private final DatPhongDAO datPhongDAO = new DatPhongDAOImpl();
    private final ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAOImpl();
    private final PhongService phongService = new PhongServiceImpl();
    private final KhachHangDAO khachHangDAO = new KhachHangDAOImpl();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAOImpl();

    /**
     * Tạo đặt phòng mới (bao gồm tạo đơn và thêm danh sách phòng)
     */
    public DatPhongDTO taoDatPhong(DatPhongDTO dto) {
        if (dto == null)
            throw new ValidationException("Dữ liệu đặt phòng không được để trống");
        if (dto.getMaKH() == null)
            throw new ValidationException("Mã khách hàng không được để trống");
        if (dto.getChiTietDatPhongs() == null || dto.getChiTietDatPhongs().isEmpty()) {
            throw new ValidationException("Phải chọn ít nhất một phòng");
        }

        // Tạo đơn đặt phòng trước
        DatPhongDTO savedDp = taoDatPhongMoi(dto.getMaKH(), dto.getMaNV(), dto.getGiaCoc());

        // Thêm từng phòng vào đơn
        for (ChiTietDatPhongDTO ct : dto.getChiTietDatPhongs()) {
            themPhongVaoDatPhong(savedDp.getMaDP(), ct.getMaPhong(), ct.getNgayNhan(), ct.getNgayTra());
        }

        // Trả về đầy đủ thông tin
        return layDatPhongChiTietDTO(savedDp.getMaDP());
    }

    /**
     * Tạo đơn đặt phòng cơ bản (chưa có phòng cụ thể)
     */
    public DatPhongDTO taoDatPhongMoi(Long maKH, String maNV, BigDecimal tienCoc) {
        KhachHang kh = khachHangDAO.findByMakh(maKH);
        if (kh == null)
            throw new BusinessException("Khách hàng không tồn tại");

        NhanVien nv = nhanVienDAO.findByMaNV(maNV);
        if (nv == null)
            throw new BusinessException("Nhân viên không tồn tại");

        DatPhong dp = new DatPhong();
        dp.setKhachHang(kh);
        dp.setNhanVien(nv);
        dp.setNgayDat(LocalDateTime.now());
        dp.setGiaCoc(tienCoc != null ? tienCoc : BigDecimal.ZERO);

        datPhongDAO.save(dp);

        return Mapper.toDatPhongDTO(dp);
    }

    /**
     * Thêm một phòng vào đặt phòng đã tồn tại
     */
    public void themPhongVaoDatPhong(Long maDP, String maPhong, LocalDateTime ngayNhan, LocalDateTime ngayTra) {
        // Gọi service chi tiết để thêm phòng
        // (để tránh lặp code)
        new ChiTietDatPhongServiceImpl().themPhong(maDP, maPhong, ngayNhan, ngayTra);
    }

    public DatPhongDTO layDatPhongChiTietDTO(Long maDP) {
        DatPhong dp = chiTietDatPhongDAO.findByMaDPWithFullDetails(maDP);
        if (dp == null) {
            throw new BusinessException("Không tìm thấy đặt phòng với mã: " + maDP);
        }
        return Mapper.toDatPhongDTO(dp);
    }

    public List<DatPhongDTO> layTatCaDatPhongDTO() {
        return datPhongDAO.findAllWithDetails().stream()
                .map(Mapper::toDatPhongDTO)
                .collect(Collectors.toList());
    }

    public List<DatPhongDTO> layTatCaDatPhongChuaThanhToanDTO() {
        return datPhongDAO.findAllChuaThanhToan().stream()
                .map(Mapper::toDatPhongDTO)
                .collect(Collectors.toList());
    }

    public List<DatPhongDTO> layLichSuDatPhongCuaKhachDTO(Long maKH) {
        return datPhongDAO.findByKhachHang(maKH).stream()
                .map(Mapper::toDatPhongDTO)
                .collect(Collectors.toList());
    }

    public void huyDatPhong(Long maDP) {
        DatPhong dp = chiTietDatPhongDAO.findByMaDPWithFullDetails(maDP);
        if (dp == null) {
            throw new BusinessException("Đặt phòng không tồn tại");
        }

        // Kiểm tra chưa check-in
        for (ChiTietDatPhong ctp : dp.getChiTietDatPhongs()) {
            if (ctp.getPhong().getTinhTrang() == TinhTrangPhong.DANG_O) {
                throw new BusinessException("Không thể hủy vì phòng đang có khách ở");
            }
        }

        // Lấy danh sách các mã phòng cần xóa (sao chép ra List riêng để tránh lỗi
        // ConcurrentModificationException)
        java.util.List<String> danhSachMaPhong = new java.util.ArrayList<>();
        for (ChiTietDatPhong ctp : dp.getChiTietDatPhongs()) {
            danhSachMaPhong.add(ctp.getPhong().getMaPhong());
        }

        // Xóa chi tiết và đơn đặt phòng
        for (String maPhong : danhSachMaPhong) {
            chiTietDatPhongDAO.delete(new ChiTietDatPhongId(maDP, maPhong));

            // Kiểm tra xem phòng có còn booking nào khác không
            java.util.List<ChiTietDatPhong> remaining = chiTietDatPhongDAO.findByPhong(maPhong);
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

        datPhongDAO.delete(maDP);
    }

    public void checkIn(Long maDP, String maPhong, LocalDateTime thoiGianCheckIn) {
        DatPhong dp = datPhongDAO.findByMaDP(maDP);
        if (dp == null)
            throw new BusinessException("Đặt phòng không tồn tại");

        ChiTietDatPhongId id = new ChiTietDatPhongId(maDP, maPhong);
        ChiTietDatPhong ctp = chiTietDatPhongDAO.findById(id);
        if (ctp == null) {
            throw new BusinessException("Không tìm thấy chi tiết đặt phòng cho phòng " + maPhong);
        }

        // Logic 1: Không cho phép nhận phòng trước thời gian đặt dự kiến
        if (thoiGianCheckIn.isBefore(ctp.getNgayNhan())) {
            throw new BusinessException("Không thể nhận phòng sớm trước thời gian quy định (" +
                    ctp.getNgayNhan().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ")");
        }

        // Logic 2: Nếu nhận phòng trễ hơn so với giờ đặt dự kiến,
        // cập nhật lại thời gian check-in thực tế trong chi tiết phòng
        if (thoiGianCheckIn.isAfter(ctp.getNgayNhan())) {
            chiTietDatPhongDAO.updateThoiGian(maDP, maPhong, thoiGianCheckIn, ctp.getNgayTra());
            ctp.setNgayNhan(thoiGianCheckIn); // Cập nhật object hiện tại để logic phía sau dùng
        }

        // Cập nhật trạng thái
        phongService.capNhatTrangThaiPhong(maPhong, TinhTrangPhong.DANG_O);

        // Cập nhật trạng thái trong ChiTietDatPhong
        chiTietDatPhongDAO.updateTrangThaiByMaDPAndMaPhong(maDP, maPhong, TinhTrangPhong.DANG_O);

        // Tạo hóa đơn khi nhận phòng (nếu chưa có)
        HoaDonDAO hdDAO = new HoaDonDAOImpl();
        if (hdDAO.findByMaDP(maDP) == null) {
            HoaDonService hoaDonService = new HoaDonServiceImpl();
            hoaDonService.taoHoaDonKhiNhanPhong(maDP);
        }
    }

    public boolean isDatPhongHopLe(Long maDP) {
        DatPhong dp = datPhongDAO.findByMaDP(maDP);
        return dp != null && !dp.getChiTietDatPhongs().isEmpty();
    }

    public void capNhatThoiGianDatPhong(Long maDP, String maPhong, LocalDateTime in, LocalDateTime out) {
        new ChiTietDatPhongServiceImpl().capNhatThoiGianDatPhong(maDP, maPhong, in, out);
    }

    @Override
    public void huyPhongDat(Long maDP, String maPhong) {
        DatPhong dp = chiTietDatPhongDAO.findByMaDPWithFullDetails(maDP);
        if (dp == null) {
            throw new BusinessException("Đặt phòng không tồn tại");
        }

        // Tìm chi tiết phòng cần hủy
        ChiTietDatPhong ctpToDelete = null;
        for (ChiTietDatPhong ctp : dp.getChiTietDatPhongs()) {
            if (ctp.getPhong().getMaPhong().equals(maPhong)) {
                ctpToDelete = ctp;
                break;
            }
        }

        if (ctpToDelete == null) {
            throw new BusinessException("Phòng " + maPhong + " không nằm trong đơn đặt phòng này");
        }

        if (ctpToDelete.getPhong().getTinhTrang() == TinhTrangPhong.DANG_O) {
            throw new BusinessException("Không thể hủy vì phòng đang có khách ở");
        }

        if (dp.getChiTietDatPhongs().size() == 1) {
            // Nếu đơn đặt phòng chỉ có 1 phòng, hủy luôn cả đơn đặt phòng
            huyDatPhong(maDP);
        } else {
            // Nếu đơn có nhiều phòng, chỉ hủy riêng phòng này
            new ChiTietDatPhongServiceImpl().xoaPhong(maDP, maPhong);
        }
    }
}