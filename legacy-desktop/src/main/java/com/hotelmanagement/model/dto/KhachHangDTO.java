package com.hotelmanagement.model.dto;

/**
 * Lớp KhachHangDTO dùng để lưu trữ và truyền thông tin
 * của khách hàng như tên, địa chỉ, số điện thoại, email và CCCD.
 * 
 * Lớp này phục vụ cho việc hiển thị và quản lý thông tin khách hàng
 * trong hệ thống.
 */
public class KhachHangDTO {
    private Long maKH;
    private String tenKH;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String cccd;

    public KhachHangDTO() {
    }

    // Getter & Setter
    public Long getMaKH() {
        return maKH;
    }

    public void setMaKH(Long maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }
}