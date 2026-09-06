package com.hotelmanagement.web.legacy.model.dto;

/**
 * Lớp NhanVienDTO dùng để lưu trữ và truyền thông tin
 * của nhân viên như mã, tên, chức vụ, địa chỉ và số điện thoại.
 * 
 * Lớp này phục vụ cho việc hiển thị và quản lý thông tin nhân viên
 * trong hệ thống.
 */
public class NhanVienDTO {
    private String maNV;
    private String tenNV;
    private String password;
    private String chucVu;
    private String diaChi;
    private String soDienThoai;

    public NhanVienDTO() {
    }

    // Getter & Setter
    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

