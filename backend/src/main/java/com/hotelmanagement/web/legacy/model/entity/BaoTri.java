package com.hotelmanagement.web.legacy.model.entity;

import com.hotelmanagement.web.legacy.model.enums.TinhTrangBaoTri;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Lớp BaoTri đại diện cho bảng "BaoTri" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về các hoạt động bảo trì phòng,
 * bao gồm mã bảo trì, phòng được bảo trì, loại bảo trì, ngày bảo trì,
 * tình trạng và mô tả.
 * 
 * Các thuộc tính:
 * - maBT: Mã bảo trì (khóa chính)
 * - phong: Thông tin phòng được bảo trì (quan hệ many-to-one với Phong)
 * - loaiBaoTri: Loại bảo trì (ví dụ: sửa chữa, vệ sinh)
 * - ngayBaoTri: Ngày thực hiện bảo trì
 * - tinhTrangBTri: Tình trạng bảo trì (sử dụng enum TinhTrangBaoTri)
 * - moTa: Mô tả chi tiết về việc bảo trì
 */
@Entity
@Table(name = "BaoTri")
public class BaoTri {
    @Id
    @Column(name = "MABT", length = 10)
    private String maBT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPHONG", nullable = false)
    private Phong phong;

    @Column(name = "LOAIBAOTRI", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String loaiBaoTri;

    @Column(name = "NGAYBAOTRI", nullable = false)
    private LocalDate ngayBaoTri;

    @Column(name = "TINHTRANGBTRI", nullable = false)
    @Enumerated(EnumType.STRING)
    private TinhTrangBaoTri tinhTrangBTri = TinhTrangBaoTri.CHUA_XU_LY;

    @Column(name = "MOTA")
    private String moTa;

    // ===================== GETTER & SETTER =====================

    public String getMaBT() {
        return maBT;
    }

    public void setMaBT(String maBT) {
        this.maBT = maBT;
    }

    public Phong getPhong() {
        return phong;
    }

    public void setPhong(Phong phong) {
        this.phong = phong;
    }

    public String getLoaiBaoTri() {
        return loaiBaoTri;
    }

    public void setLoaiBaoTri(String loaiBaoTri) {
        this.loaiBaoTri = loaiBaoTri;
    }

    public LocalDate getNgayBaoTri() {
        return ngayBaoTri;
    }

    public void setNgayBaoTri(LocalDate ngayBaoTri) {
        this.ngayBaoTri = ngayBaoTri;
    }

    public TinhTrangBaoTri getTinhTrangBTri() {
        return tinhTrangBTri;
    }

    public void setTinhTrangBTri(TinhTrangBaoTri tinhTrangBTri) {
        this.tinhTrangBTri = tinhTrangBTri;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}

