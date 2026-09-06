package com.hotelmanagement.model.entity;

import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.keyclass.ChiTietDatPhongId;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lớp ChiTietDatPhong đại diện cho bảng "ChiTietDatPhong" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin chi tiết về việc đặt phòng, bao gồm:
 * - Đơn đặt phòng liên quan (DatPhong)
 * - Phòng được đặt (Phong)
 * - Thời gian nhận và trả phòng
 * - Trạng thái của chi tiết đặt phòng
 * - Số lần chuyển phòng
 * 
 * Sử dụng khóa chính kép (composite key) thông qua lớp ChiTietDatPhongId.
 */
@Entity
@Table(name = "ChiTietDatPhong")
@IdClass(ChiTietDatPhongId.class)
public class ChiTietDatPhong {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MADP", nullable = false)
    private DatPhong datPhong;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPHONG", nullable = false, referencedColumnName = "MAPHONG")
    private Phong phong;

    @Column(name = "NGAYNHAN", nullable = false)
    private LocalDateTime ngayNhan;

    @Column(name = "NGAYTRA", nullable = false)
    private LocalDateTime ngayTra;

    @Enumerated(EnumType.STRING)
    @Column(name = "TINHTRANG")
    private TinhTrangPhong trangThai;

    @Column(name = "SOLANCHUYENPHONG", columnDefinition = "INT DEFAULT 0")
    private int soLanChuyenPhong = 0;

    // Getter & Setter
    public DatPhong getDatPhong() {
        return datPhong;
    }

    public void setDatPhong(DatPhong datPhong) {
        this.datPhong = datPhong;
    }

    public Phong getPhong() {
        return phong;
    }

    public void setPhong(Phong phong) {
        this.phong = phong;
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

    public TinhTrangPhong getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TinhTrangPhong trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoLanChuyenPhong() {
        return soLanChuyenPhong;
    }

    public void setSoLanChuyenPhong(int soLanChuyenPhong) {
        this.soLanChuyenPhong = soLanChuyenPhong;
    }
}
