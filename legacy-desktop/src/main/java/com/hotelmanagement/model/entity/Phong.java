package com.hotelmanagement.model.entity;

import com.hotelmanagement.model.enums.TinhTrangPhong;
import jakarta.persistence.*;

/**
 * Lớp Phong đại diện cho bảng "Phong" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về các phòng trong khách sạn,
 * bao gồm mã phòng, loại phòng, tình trạng và mô tả.
 * 
 * Các thuộc tính:
 * - maPhong: Mã phòng (khóa chính)
 * - loaiPhong: Loại phòng (quan hệ many-to-one với LoaiPhong)
 * - tinhTrang: Tình trạng phòng (sử dụng enum TinhTrangPhong)
 * - moTa: Mô tả về phòng
 * 
 * Lớp này phục vụ cho việc quản lý thông tin phòng
 * và liên kết với các đơn đặt phòng trong hệ thống.
 */
@Entity
@Table(name = "Phong")
public class Phong {
    @Id
    @Column(name = "MAPHONG", length = 10)
    private String maPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MALOAIPHONG", nullable = false)
    private LoaiPhong loaiPhong;

    @Column(name = "TINHTRANG", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TinhTrangPhong tinhTrang = TinhTrangPhong.SAN_SANG;

    @Column(name = "MOTA", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    public Phong() {

    }

    public Phong(String maPhong, LoaiPhong loaiPhong, TinhTrangPhong tinhTrang, String moTa) {
        this.maPhong = maPhong;
        this.loaiPhong = loaiPhong;
        this.tinhTrang = tinhTrang;
        this.moTa = moTa;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public LoaiPhong getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(LoaiPhong loaiPhong) {
        this.loaiPhong = loaiPhong;
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
}
