package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.PhongService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.ChiTietDatPhongDAO;
import com.hotelmanagement.model.dao.HoaDonDAO;
import com.hotelmanagement.model.dao.PhongDAO;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.entity.ChiTietDatPhong;
import com.hotelmanagement.model.entity.DatPhong;
import com.hotelmanagement.model.entity.HoaDon;
import com.hotelmanagement.model.entity.LoaiPhong;
import com.hotelmanagement.model.entity.Phong;
import com.hotelmanagement.model.enums.PhuongThucThanhToan;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.enums.TinhTrangThanhToan;
import com.hotelmanagement.model.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import com.hotelmanagement.mapper.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.hotelmanagement.model.dao.impl.PhongDAOImpl;
import com.hotelmanagement.model.dao.impl.HoaDonDAOImpl;
import com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl;

/**
 * Service xử lý logic liên quan đến Phòng
 */
public class PhongServiceImpl implements PhongService {

    private final PhongDAO phongDAO = new PhongDAOImpl();
    private final ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAOImpl();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAOImpl(); // ← Thêm dòng này

    public List<PhongDTO> timTatCaPhongDTO() {
        return phongDAO.findAll().stream()
                .map(p -> {
                    PhongDTO dto = Mapper.toPhongDTO(p);
                    dto.setTrangThaiHienThi(tinhTrangThaiHienThi(p));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<PhongDTO> timPhongTrong(LocalDateTime ngayNhan, LocalDateTime ngayTra, LoaiPhong loaiPhong) {
        if (ngayNhan == null || ngayTra == null) {
            throw new ValidationException("Ngày nhận và ngày trả không được để trống");
        }
        if (ngayNhan.isAfter(ngayTra) || ngayNhan.isEqual(ngayTra)) {
            throw new ValidationException("Ngày nhận phải trước ngày trả");
        }

        return phongDAO.timPhongTrong(ngayNhan, ngayTra, loaiPhong).stream()
                .map(p -> {
                    PhongDTO dto = Mapper.toPhongDTO(p);
                    dto.setTrangThaiHienThi(tinhTrangThaiHienThi(p));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public PhongDTO timPhongTheoMaPhongDTO(String maPhong) {
        Phong p = phongDAO.findByMaPhong(maPhong);
        if (p == null) {
            throw new BusinessException("Không tìm thấy phòng với mã: " + maPhong);
        }
        return Mapper.toPhongDTO(p);
    }

    public Phong timPhongTheoMaPhong(String maPhong) {
        if (maPhong == null || maPhong.isBlank()) {
            throw new ValidationException("Mã phòng không được để trống");
        }

        Phong p = phongDAO.findByMaPhong(maPhong);
        if (p == null) {
            throw new BusinessException("Không tìm thấy phòng với mã: " + maPhong);
        }

        // Force initialize LoaiPhong để tránh lazy proxy error
        if (p.getLoaiPhong() != null) {
            p.getLoaiPhong().getTenLoai();
            p.getLoaiPhong().getGia();
        }

        return p;
    }

    public PhongDTO taoPhong(PhongDTO dto) {
        if (dto == null || dto.getMaPhong() == null || dto.getMaPhong().isBlank()) {
            throw new ValidationException("Mã phòng không được để trống");
        }

        Phong p = new Phong();
        p.setMaPhong(dto.getMaPhong());
        p.setMoTa(dto.getMoTa());
        p.setTinhTrang(dto.getTinhTrang() != null ? dto.getTinhTrang() : TinhTrangPhong.SAN_SANG);

        if (dto.getMaLoaiPhong() != null && !dto.getMaLoaiPhong().isBlank()) {
            LoaiPhong loai = new LoaiPhong();
            loai.setMaLoaiPhong(dto.getMaLoaiPhong());
            p.setLoaiPhong(loai);
        }

        phongDAO.save(p);

        PhongDTO result = Mapper.toPhongDTO(p);
        result.setTrangThaiHienThi(tinhTrangThaiHienThi(p));

        return result;
    }

    /**
     * Cập nhật trạng thái phòng + Đồng bộ ChiTietDatPhong + Tạo hóa đơn nếu cần
     */
    public void capNhatTrangThaiPhong(String maPhong, TinhTrangPhong tinhTrangMoi) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. Update bảng Phong
            Phong phong = em.find(Phong.class, maPhong);
            if (phong != null) {
                phong.setTinhTrang(tinhTrangMoi);
                em.merge(phong);
            }

            // 2. Đồng bộ tất cả ChiTietDatPhong của phòng này
            em.createQuery("""
                    UPDATE ChiTietDatPhong c
                    SET c.trangThai = :newStatus
                    WHERE c.phong.maPhong = :maPhong
                    """)
                    .setParameter("newStatus", tinhTrangMoi)
                    .setParameter("maPhong", maPhong)
                    .executeUpdate();

            tx.commit();

            // 3. Nếu chuyển sang DANG_O → tạo hóa đơn tự động (nếu chưa có)
            if (tinhTrangMoi == TinhTrangPhong.DANG_O) {
                taoHoaDonTuDongChoPhong(maPhong);
            }

        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw new RuntimeException("Lỗi cập nhật trạng thái phòng: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Tạo hóa đơn tự động cho phòng khi nhận khách (DANG_O)
     */
    private void taoHoaDonTuDongChoPhong(String maPhong) {
        // Tìm tất cả đơn đặt phòng chưa thanh toán có chứa phòng này
        List<DatPhong> datPhongs = chiTietDatPhongDAO.findByPhong(maPhong).stream()
                .map(ChiTietDatPhong::getDatPhong)
                .filter(dp -> dp != null)
                .distinct()
                .toList();

        for (DatPhong dp : datPhongs) {
            Long maDP = dp.getMaDP();
            if (maDP == null)
                continue;

            // Nếu chưa có hóa đơn thì tạo mới
            if (hoaDonDAO.findByMaDP(maDP) == null) {
                taoHoaDonChoDatPhong(maDP);
            }
        }
    }

    private void taoHoaDonChoDatPhong(Long maDP) {
        DatPhong dp = chiTietDatPhongDAO.findByMaDPWithFullDetails(maDP);
        if (dp == null)
            return;

        HoaDon hoaDon = new HoaDon();
        hoaDon.setDatPhong(dp);
        hoaDon.setNgayXuatHD(LocalDateTime.now());
        hoaDon.setGiamGia(BigDecimal.ZERO);
        hoaDon.setTienCocDaDong(dp.getGiaCoc() != null ? dp.getGiaCoc() : BigDecimal.ZERO);
        hoaDon.setPhuongThucThanhToan(PhuongThucThanhToan.TIEN_MAT);
        hoaDon.setTrangThai(TinhTrangThanhToan.CHUA_THANH_TOAN);

        // Tính tiền phòng tạm thời
        BigDecimal tongTienPhong = BigDecimal.ZERO;
        for (ChiTietDatPhong ctp : dp.getChiTietDatPhongs()) {
            tongTienPhong = tongTienPhong.add(tinhTienPhongChoMotChiTiet(ctp));
        }

        hoaDon.setTongTienPhong(tongTienPhong);
        hoaDon.setTongTienDichVu(BigDecimal.ZERO);
        hoaDon.setTongTienPhaiTra(tongTienPhong.subtract(hoaDon.getTienCocDaDong()));

        hoaDonDAO.save(hoaDon);
    }

    // Hàm tính tiền phòng cho một chi tiết (copy từ HoaDonService)
    private BigDecimal tinhTienPhongChoMotChiTiet(ChiTietDatPhong ctp) {
        if (ctp == null || ctp.getPhong() == null || ctp.getPhong().getLoaiPhong() == null) {
            return BigDecimal.ZERO;
        }

        LocalDateTime ngayNhan = ctp.getNgayNhan();
        LocalDateTime ngayTra = ctp.getNgayTra();

        java.time.Duration duration = java.time.Duration.between(ngayNhan, ngayTra);
        long tongPhut = duration.toMinutes();
        if (tongPhut <= 0)
            tongPhut = 60;

        BigDecimal giaNgay = ctp.getPhong().getLoaiPhong().getGia();
        BigDecimal giaGio = giaNgay.divide(BigDecimal.valueOf(24), 2, BigDecimal.ROUND_HALF_UP);

        long tongGio = tongPhut / 60;
        long soPhutDu = tongPhut % 60;

        if (tongPhut >= 18 * 60) { // >= 18 giờ tính theo ngày
            long soNgay = tongGio / 24 + (tongGio % 24 > 0 || soPhutDu > 0 ? 1 : 0);
            return giaNgay.multiply(BigDecimal.valueOf(soNgay));
        }

        if (soPhutDu > 10)
            tongGio += 1;
        if (tongGio == 0)
            tongGio = 1;

        return giaGio.multiply(BigDecimal.valueOf(tongGio));
    }

    private String tinhTrangThaiHienThi(Phong phong) {
        if (phong.getTinhTrang() == TinhTrangPhong.BAO_TRI)
            return "BAO_TRI";
        if (phong.getTinhTrang() == TinhTrangPhong.DANG_DON_DEP)
            return "DANG_DON_DEP";
        if (phong.getTinhTrang() == TinhTrangPhong.NGUNG_SU_DUNG)
            return "NGUNG_SU_DUNG";

        List<ChiTietDatPhong> list = chiTietDatPhongDAO.findByPhong(phong.getMaPhong());

        boolean dangO = list.stream().anyMatch(ct -> ct.getTrangThai() == TinhTrangPhong.DANG_O);
        if (dangO)
            return "DANG_O";

        boolean daDat = list.stream().anyMatch(ct -> ct.getTrangThai() == TinhTrangPhong.DA_DAT);
        if (daDat)
            return "DA_DAT";

        return "SAN_SANG";
    }
}