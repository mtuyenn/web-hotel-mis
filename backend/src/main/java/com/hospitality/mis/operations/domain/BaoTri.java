package com.hospitality.mis.operations.domain;

import com.hospitality.mis.room.domain.Room;

import com.hospitality.mis.operations.domain.TinhTrangBaoTri;

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
 * - room: phòng được bảo trì (quan hệ many-to-one với Room)
 * - loaiBaoTri: Loại bảo trì (ví dụ: sửa chữa, vệ sinh)
 * - ngayBaoTri: Ngày thực hiện bảo trì
 * - tinhTrangBTri: Tình trạng bảo trì (sử dụng enum TinhTrangBaoTri)
 * - moTa: Mô tả chi tiết về việc bảo trì
 */
@Entity
@Table(name = "maintenance_work_orders")
public class BaoTri {
    @Id
    @Column(name = "id", length = 10)
    private String maBT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "maintenance_type", nullable = false)
    private String loaiBaoTri;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate ngayBaoTri;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TinhTrangBaoTri tinhTrangBTri = TinhTrangBaoTri.CHUA_XU_LY;

    @Column(name = "description")
    private String moTa;

    // ===================== GETTER & SETTER =====================

    public String getMaBT() {
        return maBT;
    }

    public void setMaBT(String maBT) {
        this.maBT = maBT;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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

