package com.hotelmanagement.web.legacy.model.entity;

import com.hotelmanagement.web.legacy.model.enums.PhuongThucThanhToan;
import com.hotelmanagement.web.legacy.model.enums.TinhTrangThanhToan;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp HoaDon đại diện cho bảng "HoaDon" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về hóa đơn thanh toán của các đơn đặt phòng,
 * bao gồm thông tin khách hàng, thời gian, chi phí và trạng thái thanh toán.
 * 
 * Các thuộc tính:
 * - maHoaDon: Mã hóa đơn (khóa chính, tự tăng)
 * - datPhong: Đơn đặt phòng liên quan (quan hệ one-to-one với DatPhong)
 * - ngayXuatHD: Ngày và giờ xuất hóa đơn
 * - giamGia: Số tiền được giảm giá
 * - tienCocDaDong: Số tiền đặt cọc đã thanh toán
 * - phuongThucThanhToan: Phương thức thanh toán (sử dụng enum
 * PhuongThucThanhToan)
 * - trangThai: Trạng thái thanh toán (sử dụng enum TinhTrangThanhToan)
 * - tongTienPhong: Tổng tiền phòng
 * - tongTienDichVu: Tổng tiền dịch vụ
 * - tongTienPhaiTra: Tổng tiền phải trả (đã bao gồm giảm giá)
 * 
 * Lớp này phục vụ cho việc quản lý thanh toán và xuất hóa đơn trong hệ thống.
 */
@Entity
@Table(name = "HoaDon")

public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maHoaDon;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MADP", nullable = false, unique = true)
    private DatPhong datPhong;

    @Column(name = "NGAYXUATHD", nullable = false)
    private LocalDateTime ngayXuatHD = LocalDateTime.now();

    @Column(name = "GIAMGIA", precision = 12, scale = 2)
    private BigDecimal giamGia = BigDecimal.ZERO;

    @Column(name = "TIENCOCDADONG", precision = 12, scale = 2)
    private BigDecimal tienCocDaDong = BigDecimal.ZERO;

    @Column(name = "PHUONGTHUCTHANHTOAN")
    @Enumerated(EnumType.STRING)
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "TRANGTHAI")
    @Enumerated(EnumType.STRING)
    private TinhTrangThanhToan trangThai = TinhTrangThanhToan.CHUA_THANH_TOAN;

    @Column(name = "TONGTIENPHONG", precision = 12, scale = 2)
    private BigDecimal tongTienPhong = BigDecimal.ZERO;

    @Column(name = "TONGTIENDICHVU", precision = 12, scale = 2)
    private BigDecimal tongTienDichVu = BigDecimal.ZERO;

    @Column(name = "TONGTIENPHAITRA", precision = 12, scale = 2)
    private BigDecimal tongTienPhaiTra = BigDecimal.ZERO;

    public HoaDon() {

    }

    public HoaDon(Long maHoaDon, DatPhong datPhong, LocalDateTime ngayXuatHD, BigDecimal giamGia,
            BigDecimal tienCocDaDong, PhuongThucThanhToan phuongThucThanhToan, TinhTrangThanhToan trangThai) {
        this.maHoaDon = maHoaDon;
        this.datPhong = datPhong;
        this.ngayXuatHD = ngayXuatHD;
        this.giamGia = giamGia;
        this.tienCocDaDong = tienCocDaDong;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThai = trangThai;
    }

    public Long getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(Long maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public DatPhong getDatPhong() {
        return datPhong;
    }

    public void setDatPhong(DatPhong datPhong) {
        this.datPhong = datPhong;
    }

    public LocalDateTime getNgayXuatHD() {
        return ngayXuatHD;
    }

    public void setNgayXuatHD(LocalDateTime ngayXuatHD) {
        this.ngayXuatHD = ngayXuatHD;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = giamGia;
    }

    public BigDecimal getTienCocDaDong() {
        return tienCocDaDong;
    }

    public void setTienCocDaDong(BigDecimal tienCocDaDong) {
        this.tienCocDaDong = tienCocDaDong;
    }

    public PhuongThucThanhToan getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(PhuongThucThanhToan phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public TinhTrangThanhToan getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TinhTrangThanhToan trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getTongTienPhong() {
        return tongTienPhong;
    }

    public void setTongTienPhong(BigDecimal tongTienPhong) {
        this.tongTienPhong = tongTienPhong;
    }

    public BigDecimal getTongTienDichVu() {
        return tongTienDichVu;
    }

    public void setTongTienDichVu(BigDecimal tongTienDichVu) {
        this.tongTienDichVu = tongTienDichVu;
    }

    public BigDecimal getTongTienPhaiTra() {
        return tongTienPhaiTra;
    }

    public void setTongTienPhaiTra(BigDecimal tongTienPhaiTra) {
        this.tongTienPhaiTra = tongTienPhaiTra;
    }
}

