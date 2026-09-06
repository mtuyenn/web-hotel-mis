package com.hotelmanagement.model.entity;

import com.hotelmanagement.model.keyclass.ChiTietDichVuId;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Lớp ChiTietDichVu đại diện cho bảng "ChiTietDichVu" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin chi tiết về việc sử dụng dịch vụ trong các đơn đặt
 * phòng,
 * bao gồm: đơn đặt phòng liên quan, dịch vụ được sử dụng, ngày sử dụng và số
 * lượng.
 * 
 * Sử dụng khóa chính kép (composite key) thông qua lớp ChiTietDichVuId.
 */
@Entity
@Table(name = "ChiTietDichVu")
@IdClass(ChiTietDichVuId.class)
public class ChiTietDichVu {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MADP", nullable = false)
    private DatPhong datPhong;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MADV", nullable = false)
    private DichVu dichVu;

    @Id
    @Column(name = "NGAYSUDUNG")
    private LocalDate ngaySuDung;

    @Column(name = "SOLUONG", nullable = false)
    private Integer soLuong = 0;

    public ChiTietDichVu() {
    }

    public ChiTietDichVu(DatPhong datPhong, DichVu dichVu, LocalDate ngaySuDung, Integer soLuong) {
        this.datPhong = datPhong;
        this.dichVu = dichVu;
        this.ngaySuDung = ngaySuDung;
        this.soLuong = soLuong;
    }

    // Getter & Setter
    public DatPhong getDatPhong() {
        return datPhong;
    }

    public void setDatPhong(DatPhong datPhong) {
        this.datPhong = datPhong;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public LocalDate getNgaySuDung() {
        return ngaySuDung;
    }

    public void setNgaySuDung(LocalDate ngaySuDung) {
        this.ngaySuDung = ngaySuDung;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}