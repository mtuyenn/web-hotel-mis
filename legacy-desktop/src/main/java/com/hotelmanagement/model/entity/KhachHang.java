package com.hotelmanagement.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp KhachHang đại diện cho bảng "KhachHang" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về khách hàng của khách sạn,
 * bao gồm mã khách hàng, tên, địa chỉ, số điện thoại, email và CCCD.
 * 
 * Các thuộc tính:
 * - maKH: Mã khách hàng (khóa chính, tự tăng)
 * - tenKH: Tên khách hàng
 * - diaChi: Địa chỉ của khách hàng
 * - soDienThoai: Số điện thoại (duy nhất, không được để trống)
 * - email: Địa chỉ email (duy nhất)
 * - cccd: Số CCCD/CMND (duy nhất, không được để trống)
 * - datPhongs: Danh sách các đơn đặt phòng của khách hàng (quan hệ one-to-many)
 * 
 * Lớp này phục vụ cho việc quản lý thông tin khách hàng
 * và liên kết với các đơn đặt phòng của họ.
 */
@Entity
@Table(name = "KhachHang")

public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MAKH")
    private Long maKH;

    @Column(name = "TENKH", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenKH;

    @Column(name = "DIACHI", columnDefinition = "NVARCHAR(255)")
    private String diaChi;

    @Column(name = "SODIENTHOAI", length = 15, unique = true, nullable = false)
    private String soDienThoai;

    @Column(name = "EMAIL", length = 100, unique = true)
    private String email;

    @Column(name = "CCCD", length = 12, nullable = false, unique = true)
    private String cccd;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatPhong> datPhongs = new ArrayList<>();

    public KhachHang() {

    }

    public KhachHang(Long maKH, String tenKH, String diaChi, String soDienThoai, String email, String cccd) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.cccd = cccd;
    }

    public Long getMaKH() {
        return maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public String getCccd() {
        return cccd;
    }

    public void setMaKH(Long maKH) {
        this.maKH = maKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }
}
