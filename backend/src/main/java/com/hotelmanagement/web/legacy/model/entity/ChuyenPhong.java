package com.hotelmanagement.web.legacy.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Lớp ChuyenPhong đại diện cho bảng "ChuyenPhong" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về việc chuyển phòng giữa các phòng khác nhau
 * trong một đơn đặt phòng, bao gồm phòng cũ, phòng mới, thời gian chuyển
 * và lý do chuyển.
 * 
 * Các thuộc tính:
 * - maChuyenPhong: Mã chuyển phòng (khóa chính, tự tăng)
 * - datPhong: Đơn đặt phòng liên quan (quan hệ many-to-one với DatPhong)
 * - phongCu: Phòng cũ được chuyển khỏi (quan hệ many-to-one với Phong)
 * - phongMoi: Phòng mới được chuyển đến (quan hệ many-to-one với Phong)
 * - ngayChuyen: Thời gian thực hiện chuyển phòng
 * - lyDo: Lý do chuyển phòng
 */
@Entity
@Table(name = "ChuyenPhong")

public class ChuyenPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MACHUYENPHONG")
    private Long maChuyenPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MADP", nullable = false)
    private DatPhong datPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPHONGCU", nullable = false)
    private Phong phongCu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPHONGMOI", nullable = false)
    private Phong phongMoi;

    @Column(name = "NGAYCHUYENPHONG")
    private LocalDateTime ngayChuyen;

    @Column(name = "LYDO")
    private String lyDo;

    public Long getMaChuyenPhong() {
        return maChuyenPhong;
    }

    public void setMaChuyenPhong(Long maChuyenPhong) {
        this.maChuyenPhong = maChuyenPhong;
    }

    public DatPhong getDatPhong() {
        return datPhong;
    }

    public void setDatPhong(DatPhong datPhong) {
        this.datPhong = datPhong;
    }

    public Phong getPhongCu() {
        return phongCu;
    }

    public void setPhongCu(Phong phongCu) {
        this.phongCu = phongCu;
    }

    public Phong getPhongMoi() {
        return phongMoi;
    }

    public void setPhongMoi(Phong phongMoi) {
        this.phongMoi = phongMoi;
    }

    public LocalDateTime getNgayChuyen() {
        return ngayChuyen;
    }

    public void setNgayChuyen(LocalDateTime ngayChuyen) {
        this.ngayChuyen = ngayChuyen;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }
}

