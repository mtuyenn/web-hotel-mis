package com.hotelmanagement.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lớp DatPhong đại diện cho bảng "DatPhong" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về các đơn đặt phòng, bao gồm:
 * - Thông tin khách hàng (KhachHang)
 * - Nhân viên lập đơn (NhanVien)
 * - Thời gian đặt phòng
 * - Tiền đặt cọc
 * - Chi tiết các phòng được đặt (ChiTietDatPhong)
 * - Các dịch vụ đi kèm (ChiTietDichVu)
 * - Hóa đơn thanh toán (HoaDon)
 * 
 * Các mối quan hệ:
 * - Many-to-one với KhachHang (nhiều đơn đặt phòng có thể thuộc về một khách
 * hàng)
 * - Many-to-one với NhanVien (nhiều đơn đặt phòng có thể được lập bởi một nhân
 * viên)
 * - One-to-many với ChiTietDatPhong (một đơn đặt phòng có thể có nhiều chi tiết
 * phòng)
 * - One-to-many với ChiTietDichVu (một đơn đặt phòng có thể có nhiều dịch vụ)
 * - One-to-one với HoaDon (một đơn đặt phòng có thể có một hóa đơn)
 */
@Entity
@Table(name = "DatPhong")
public class DatPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MADP")
    private Long maDP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAKH", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANV", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "NGAYDAT", nullable = false)
    private LocalDateTime ngayDat = LocalDateTime.now();

    @Column(name = "GIACOC", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaCoc = BigDecimal.ZERO;

    @OneToMany(mappedBy = "datPhong", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();

    @OneToMany(mappedBy = "datPhong", fetch = FetchType.LAZY)
    private Set<ChiTietDichVu> chiTietDichVus;

    @OneToOne(mappedBy = "datPhong", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private HoaDon hoaDon;

    public DatPhong() {

    }

    public DatPhong(Long maDP, KhachHang khachHang, LocalDateTime ngayDat,
            NhanVien nhanVien, BigDecimal giaCoc, List<ChiTietDatPhong> chiTietDatPhongs, HoaDon hoaDon) {
        this.maDP = maDP;
        this.khachHang = khachHang;
        this.ngayDat = ngayDat;
        this.nhanVien = nhanVien;
        this.giaCoc = giaCoc;
        this.chiTietDatPhongs = chiTietDatPhongs;
        this.hoaDon = hoaDon;
    }

    public Long getMaDP() {
        return maDP;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public BigDecimal getGiaCoc() {
        return giaCoc;
    }

    public List<ChiTietDatPhong> getChiTietDatPhongs() {
        return chiTietDatPhongs;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setMaDP(Long maDP) {
        this.maDP = maDP;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public void setGiaCoc(BigDecimal giaCoc) {
        this.giaCoc = giaCoc;
    }

    public void setChiTietDatPhongs(List<ChiTietDatPhong> chiTietDatPhongs) {
        this.chiTietDatPhongs = chiTietDatPhongs;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public Set<ChiTietDichVu> getChiTietDichVus() {
        return chiTietDichVus;
    }

    public void setChiTietDichVus(Set<ChiTietDichVu> chiTietDichVus) {
        this.chiTietDichVus = chiTietDichVus;
    }
}
