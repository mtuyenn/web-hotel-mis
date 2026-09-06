package com.hotelmanagement.model.entity;

import com.hotelmanagement.model.enums.ChucVuNhanVien;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp NhanVien đại diện cho bảng "NhanVien" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về nhân viên của khách sạn,
 * bao gồm mã nhân viên, tên, chức vụ, địa chỉ, số điện thoại và mật khẩu.
 * 
 * Các thuộc tính:
 * - maNV: Mã nhân viên (khóa chính)
 * - tenNV: Tên nhân viên
 * - password: Mật khẩu đăng nhập
 * - chucVu: Chức vụ của nhân viên (sử dụng enum ChucVuNhanVien)
 * - diaChi: Địa chỉ của nhân viên
 * - soDienThoai: Số điện thoại (duy nhất)
 * - datPhongs: Danh sách các đơn đặt phòng do nhân viên này lập (quan hệ
 * one-to-many)
 * 
 * Lớp này phục vụ cho việc quản lý thông tin nhân viên
 * và theo dõi các đơn đặt phòng do họ xử lý.
 */
@Entity
@Table(name = "NhanVien")
public class NhanVien {
    @Id
    @Column(name = "MANV", length = 10)
    private String maNV;

    @Column(name = "TENNV", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenNV;

    @Column(name = "PASSWORD", length = 255, nullable = false)
    private String password;

    @Column(name = "CHUCVU", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChucVuNhanVien chucVu;

    @Column(name = "DIACHI", columnDefinition = "NVARCHAR(255)")
    private String diaChi;

    @Column(name = "SODIENTHOAI", length = 15, unique = true, nullable = false)
    private String soDienThoai;

    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatPhong> datPhongs = new ArrayList<>();

    public NhanVien() {

    }

    public NhanVien(String maNV, String tenNV, String diaChi,
            ChucVuNhanVien chucVu, String soDienThoai, List<DatPhong> datPhongs) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.diaChi = diaChi;
        this.chucVu = chucVu;
        this.soDienThoai = soDienThoai;
        this.datPhongs = datPhongs;
    }

    public String getMaNV() {
        return maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public ChucVuNhanVien getChucVu() {
        return chucVu;
    }

    public void setChucVu(ChucVuNhanVien chucVu) {
        this.chucVu = chucVu;
    }

    public List<DatPhong> getDatPhongs() {
        return datPhongs;
    }

    public void setDatPhongs(List<DatPhong> datPhongs) {
        this.datPhongs = datPhongs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
