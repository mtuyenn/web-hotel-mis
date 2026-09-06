package com.hotelmanagement.service.impl;

import com.hotelmanagement.service.*;

import com.hotelmanagement.service.HoaDonService;

import com.hotelmanagement.exception.BusinessException;
import com.hotelmanagement.exception.ValidationException;
import com.hotelmanagement.model.dao.*;
import com.hotelmanagement.model.dto.HoaDonDTO;
import com.hotelmanagement.model.entity.*;
import com.hotelmanagement.model.enums.PhuongThucThanhToan;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.enums.TinhTrangThanhToan;
import com.hotelmanagement.mapper.Mapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.hotelmanagement.model.dao.impl.ChiTietDatPhongDAOImpl;
import com.hotelmanagement.model.dao.impl.HoaDonDAOImpl;

/**
 * Service xử lý logic liên quan đến Hóa đơn và thanh toán
 */
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();
    private final ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAOImpl();
    private final PhongService phongService = new PhongServiceImpl();

    // Chính sách tính tiền
    private static final int GIO_TINH_NGAY_DAY_DU = 18; // sau 18 giờ tính thêm 1 ngày
    private static final BigDecimal TI_LE_GIAM_GIA_TOI_DA = BigDecimal.valueOf(0.30); // tối đa 30%

    /**
     * Tính tiền phòng cho một chi tiết đặt phòng (theo giờ)
     */
    private BigDecimal tinhTienPhongChoMotChiTiet(ChiTietDatPhong ctp) {
        if (ctp == null || ctp.getPhong() == null || ctp.getPhong().getLoaiPhong() == null) {
            return BigDecimal.ZERO;
        }

        LocalDateTime ngayNhan = ctp.getNgayNhan();
        LocalDateTime ngayTra = ctp.getNgayTra();

        Duration duration = Duration.between(ngayNhan, ngayTra);
        long tongPhut = duration.toMinutes();
        if (tongPhut <= 0)
            tongPhut = 60;

        BigDecimal giaNgay = ctp.getPhong().getLoaiPhong().getGia();
        BigDecimal giaGio = giaNgay.divide(BigDecimal.valueOf(24), 2, BigDecimal.ROUND_HALF_UP);

        long tongGio = tongPhut / 60;
        long soPhutDu = tongPhut % 60;

        // Nếu >= 18 giờ → tính theo ngày đầy đủ
        if (tongPhut >= GIO_TINH_NGAY_DAY_DU) {
            long soNgay = tongGio / 24;
            if (tongGio % 24 > 0 || soPhutDu > 0) {
                soNgay += 1;
            }
            return giaNgay.multiply(BigDecimal.valueOf(soNgay));
        }

        // Tính theo giờ
        if (soPhutDu > 10)
            tongGio += 1;
        if (tongGio == 0)
            tongGio = 1;

        return giaGio.multiply(BigDecimal.valueOf(tongGio));
    }

    /**
     * Thanh toán & tạo hóa đơn chính thức (Check-out)
     */
    public HoaDonDTO checkOut(Long maDP, PhuongThucThanhToan phuongThucThanhToan, BigDecimal giamGiaInput) {
        if (maDP == null) {
            throw new ValidationException("Mã đặt phòng không được để trống");
        }

        DatPhong dp = chiTietDatPhongDAO.findByMaDPWithFullDetails(maDP);
        if (dp == null) {
            throw new BusinessException("Không tìm thấy đặt phòng với mã: " + maDP);
        }

        HoaDon hoaDon = hoaDonDAO.findByMaDP(maDP);
        if (hoaDon != null && hoaDon.getTrangThai() == TinhTrangThanhToan.DA_THANH_TOAN) {
            throw new BusinessException("Đặt phòng này đã được thanh toán rồi!");
        }
        if (hoaDon == null) {
            hoaDon = new HoaDon();
            hoaDon.setDatPhong(dp);
            hoaDon.setTrangThai(TinhTrangThanhToan.CHUA_THANH_TOAN);
        }

        // Tính tiền phòng
        BigDecimal tongTienPhong = BigDecimal.ZERO;
        for (ChiTietDatPhong ctp : dp.getChiTietDatPhongs()) {
            ctp.setNgayTra(LocalDateTime.now()); // Thực tế trả phòng ngay bây giờ
            tongTienPhong = tongTienPhong.add(tinhTienPhongChoMotChiTiet(ctp));

            // Cập nhật trạng thái phòng thành dọn dẹp
            phongService.capNhatTrangThaiPhong(ctp.getPhong().getMaPhong(), TinhTrangPhong.DANG_DON_DEP);

            // Cập nhật lại trạng thái Chi Tiết Đặt Phòng thành ĐÃ TRẢ
            chiTietDatPhongDAO.updateTrangThaiByMaDPAndMaPhong(maDP, ctp.getPhong().getMaPhong(),
                    TinhTrangPhong.DA_TRA);
        }

        BigDecimal tongTienDichVu = tinhTongTienDichVu(dp);
        BigDecimal tongTruocGiam = tongTienPhong.add(tongTienDichVu);
        BigDecimal tienCoc = dp.getGiaCoc() != null ? dp.getGiaCoc() : BigDecimal.ZERO;

        BigDecimal soTienGiam = tinhSoTienGiam(tongTruocGiam, giamGiaInput);

        BigDecimal tongPhaiTra = tongTruocGiam.subtract(soTienGiam).subtract(tienCoc);
        if (tongPhaiTra.compareTo(BigDecimal.ZERO) < 0) {
            tongPhaiTra = BigDecimal.ZERO;
        }

        hoaDon.setNgayXuatHD(LocalDateTime.now());
        hoaDon.setGiamGia(soTienGiam);
        hoaDon.setTienCocDaDong(tienCoc);
        hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
        hoaDon.setTrangThai(TinhTrangThanhToan.DA_THANH_TOAN);
        hoaDon.setTongTienPhong(tongTienPhong);
        hoaDon.setTongTienDichVu(tongTienDichVu);
        hoaDon.setTongTienPhaiTra(tongPhaiTra);

        if (hoaDon.getMaHoaDon() == null) {
            hoaDonDAO.save(hoaDon);
        } else {
            hoaDonDAO.update(hoaDon);
        }

        return Mapper.toHoaDonDTO(hoaDon);
    }

    public HoaDonDTO taoHoaDonKhiNhanPhong(Long maDP) {
        if (maDP == null) {
            throw new ValidationException("Mã đặt phòng không được để trống");
        }

        // Lấy đầy đủ thông tin đặt phòng
        DatPhong dp = chiTietDatPhongDAO.findByMaDPWithFullDetails(maDP);
        if (dp == null) {
            throw new BusinessException("Không tìm thấy đặt phòng với mã: " + maDP);
        }

        // Kiểm tra đã có hóa đơn chưa
        HoaDon existing = hoaDonDAO.findByMaDP(maDP);
        if (existing != null) {
            return Mapper.toHoaDonDTO(existing); // đã có thì trả về luôn
        }

        // Tạo hóa đơn mới
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

        return Mapper.toHoaDonDTO(hoaDon);
    }

    /**
     * Tính tổng tiền dịch vụ
     */
    private BigDecimal tinhTongTienDichVu(DatPhong dp) {
        BigDecimal total = BigDecimal.ZERO;
        if (dp.getChiTietDichVus() != null) {
            for (ChiTietDichVu ctdv : dp.getChiTietDichVus()) {
                if (ctdv.getDichVu() != null && ctdv.getSoLuong() != null) {
                    total = total.add(
                            ctdv.getDichVu().getGiaDV().multiply(BigDecimal.valueOf(ctdv.getSoLuong())));
                }
            }
        }
        return total;
    }

    /**
     * Tính số tiền giảm giá
     */
    private BigDecimal tinhSoTienGiam(BigDecimal tongTruocGiam, BigDecimal giamGiaInput) {
        if (giamGiaInput == null || giamGiaInput.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal soTienGiam;
        if (giamGiaInput.compareTo(BigDecimal.valueOf(100)) <= 0) {
            // Giảm theo phần trăm
            soTienGiam = tongTruocGiam.multiply(
                    giamGiaInput.divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP));
        } else {
            // Giảm theo số tiền tuyệt đối
            soTienGiam = giamGiaInput;
        }

        // Không cho giảm quá 30%
        return soTienGiam.min(tongTruocGiam.multiply(TI_LE_GIAM_GIA_TOI_DA));
    }

    /**
     * Tìm hóa đơn theo mã đặt phòng và trả về DTO
     */
    public HoaDonDTO timHoaDonTheoDatPhong(Long maDP) {
        if (maDP == null) {
            throw new ValidationException("Mã đặt phòng không được để trống");
        }

        HoaDon hoaDon = hoaDonDAO.findByMaDP(maDP);
        if (hoaDon == null) {
            throw new BusinessException("Không tìm thấy hóa đơn cho đặt phòng: " + maDP);
        }

        return Mapper.toHoaDonDTO(hoaDon);
    }

    public List<HoaDonDTO> getAllHoaDon() {
        return hoaDonDAO.findAll()
                .stream()
                .map(Mapper::toHoaDonDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả hóa đơn với dữ liệu mới nhất (fresh)
     */
    public List<HoaDonDTO> getAllHoaDonFresh() {
        return hoaDonDAO.findAllFresh().stream()
                .map(Mapper::toHoaDonDTO)
                .collect(Collectors.toList());
    }
}