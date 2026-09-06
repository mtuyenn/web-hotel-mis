package com.hotelmanagement.web.legacy.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp LoaiPhong đại diện cho bảng "LoaiPhong" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về các loại phòng trong khách sạn,
 * bao gồm mã loại phòng, tên loại, giá và mô tả.
 * 
 * Các thuộc tính:
 * - maLoaiPhong: Mã loại phòng (khóa chính)
 * - tenLoai: Tên loại phòng
 * - gia: Giá của loại phòng
 * - moTa: Mô tả về loại phòng
 * - phongs: Danh sách các phòng thuộc loại này (quan hệ one-to-many)
 * 
 * Lớp này phục vụ cho việc quản lý danh mục loại phòng
 * và liên kết với các phòng cụ thể trong hệ thống.
 */
@Entity
@Table(name = "LoaiPhong")
public class LoaiPhong {
    @Id
    @Column(name = "MALOAIPHONG", length = 10)
    private String maLoaiPhong;

    @Column(name = "TENLOAI", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String tenLoai;

    @Column(name = "GIA", nullable = false, precision = 12, scale = 2)
    private BigDecimal gia;

    @Column(name = "MOTA", columnDefinition = "NVARCHAR(500)")
    private String moTa;

    @OneToMany(mappedBy = "loaiPhong", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phong> phongs = new ArrayList<>();

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public List<Phong> getPhongs() {
        return phongs;
    }

    public void setPhongs(List<Phong> phongs) {
        this.phongs = phongs;
    }
}

