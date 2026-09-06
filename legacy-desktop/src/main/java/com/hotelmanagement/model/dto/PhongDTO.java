package com.hotelmanagement.model.dto;

import com.hotelmanagement.model.enums.TinhTrangPhong;

import java.math.BigDecimal;

/**
 * Lớp PhongDTO dùng để lưu trữ và truyền thông tin
 * của một phòng bao gồm mã phòng, loại phòng, giá và trạng thái.
 * 
 * Lớp này phục vụ cho việc hiển thị danh sách phòng
 * và xử lý các chức năng liên quan đến phòng trong hệ thống.
 */
public class PhongDTO {
    private String maPhong;
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private BigDecimal gia;
    private TinhTrangPhong tinhTrang;
    private String moTa;
    private String trangThaiHienThi;

    // Constructor
    public PhongDTO() {
    }

    public PhongDTO(String maPhong, String maLoaiPhong, String tenLoaiPhong,
            BigDecimal gia, TinhTrangPhong tinhTrang, String moTa) {
        this.maPhong = maPhong;
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.gia = gia;
        this.tinhTrang = tinhTrang;
        this.moTa = moTa;
    }

    // Getter & Setter
    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public TinhTrangPhong getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(TinhTrangPhong tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTrangThaiHienThi() {
        return trangThaiHienThi;
    }

    public void setTrangThaiHienThi(String trangThaiHienThi) {
        this.trangThaiHienThi = trangThaiHienThi;
    }
}