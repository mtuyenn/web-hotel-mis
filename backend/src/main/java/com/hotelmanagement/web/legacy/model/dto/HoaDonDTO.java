package com.hotelmanagement.web.legacy.model.dto;

import com.hotelmanagement.web.legacy.model.enums.PhuongThucThanhToan;
import com.hotelmanagement.web.legacy.model.enums.TinhTrangThanhToan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp HoaDonDTO dùng để lưu trữ và truyền thông tin hóa đơn
 * thanh toán của một đơn đặt phòng, bao gồm thông tin khách hàng,
 * thời gian, chi phí và trạng thái thanh toán.
 * 
 * Lớp này phục vụ cho việc hiển thị hóa đơn và xử lý các chức năng
 * liên quan đến thanh toán trong hệ thống.
 */
public class HoaDonDTO {
    private Long maHoaDon;
    private Long maDP;

    // Thông tin hiển thị
    private String tenKhachHang;
    private String tenNV; // Nhân viên thanh toán
    private LocalDateTime ngayXuatHD;
    private LocalDateTime ngayNhan;
    private LocalDateTime ngayTra;
    private String tenLoaiPhong;

    // Tiền
    private BigDecimal tongTienPhong;
    private BigDecimal tongTienDichVu;
    private BigDecimal giamGia;
    private BigDecimal tienCocDaDong;
    private BigDecimal tongTienPhaiTra;

    // Enum
    private PhuongThucThanhToan phuongThucThanhToan;
    private TinhTrangThanhToan trangThai;

    public HoaDonDTO() {
    }

    public HoaDonDTO(Long maHoaDon, Long maDP, LocalDateTime ngayXuatHD, BigDecimal tongTienPhong,
            BigDecimal tongTienDichVu, BigDecimal giamGia, BigDecimal tienCocDaDong,
            BigDecimal tongTienPhaiTra, PhuongThucThanhToan phuongThucThanhToan,
            TinhTrangThanhToan trangThai) {
        this.maHoaDon = maHoaDon;
        this.maDP = maDP;
        this.ngayXuatHD = ngayXuatHD;
        this.tongTienPhong = tongTienPhong;
        this.tongTienDichVu = tongTienDichVu;
        this.giamGia = giamGia;
        this.tienCocDaDong = tienCocDaDong;
        this.tongTienPhaiTra = tongTienPhaiTra;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThai = trangThai;
    }

    public Long getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(Long maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public Long getMaDP() {
        return maDP;
    }

    public void setMaDP(Long maDP) {
        this.maDP = maDP;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public LocalDateTime getNgayXuatHD() {
        return ngayXuatHD;
    }

    public void setNgayXuatHD(LocalDateTime ngayXuatHD) {
        this.ngayXuatHD = ngayXuatHD;
    }

    public LocalDateTime getNgayNhan() {
        return ngayNhan;
    }

    public void setNgayNhan(LocalDateTime ngayNhan) {
        this.ngayNhan = ngayNhan;
    }

    public LocalDateTime getNgayTra() {
        return ngayTra;
    }

    public void setNgayTra(LocalDateTime ngayTra) {
        this.ngayTra = ngayTra;
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

    public BigDecimal getTongTienPhaiTra() {
        return tongTienPhaiTra;
    }

    public void setTongTienPhaiTra(BigDecimal tongTienPhaiTra) {
        this.tongTienPhaiTra = tongTienPhaiTra;
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

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }
}
