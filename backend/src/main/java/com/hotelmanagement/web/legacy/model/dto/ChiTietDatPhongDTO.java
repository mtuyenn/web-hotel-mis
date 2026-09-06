package com.hotelmanagement.web.legacy.model.dto;

import com.hotelmanagement.web.legacy.model.enums.TinhTrangPhong;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp ChiTietDatPhongDTO dùng để truyền dữ liệu chi tiết đặt phòng
 * giữa các tầng (Controller, Service).
 * 
 * Mục đích:
 * - Giúp hiển thị thông tin rõ ràng, dễ xử lý
 */
public class ChiTietDatPhongDTO {

    // Mã đặt phòng
    private Long maDP;

    // Mã phòng
    private String maPhong;

    // Tên loại phòng (dùng để hiển thị)
    private String tenLoaiPhong;

    // Giá phòng
    private BigDecimal giaPhong;

    // Ngày nhận phòng dự kiến
    private LocalDateTime ngayNhan;

    // Ngày trả phòng dự kiến
    private LocalDateTime ngayTra;

    // Ngày nhận phòng thực tế
    private LocalDateTime ngayNhanThucTe;

    // Ngày trả phòng thực tế
    private LocalDateTime ngayTraThucTe;

    // Tình trạng phòng (trống, đang sử dụng,...)
    private TinhTrangPhong tinhTrangPhong;

    // Số lần chuyển phòng của khách
    private int soLanChuyenPhong;

    /**
     * Constructor mặc định
     */
    public ChiTietDatPhongDTO() {
    }

    /**
     * Constructor đầy đủ tham số
     * Dùng để khởi tạo nhanh đối tượng
     */
    public ChiTietDatPhongDTO(Long maDP, String maPhong, String tenLoaiPhong, BigDecimal giaPhong,
            LocalDateTime ngayNhan, LocalDateTime ngayTra,
            LocalDateTime ngayNhanThucTe, LocalDateTime ngayTraThucTe, TinhTrangPhong tinhTrangPhong) {
        this.maDP = maDP;
        this.maPhong = maPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.giaPhong = giaPhong;
        this.ngayNhan = ngayNhan;
        this.ngayTra = ngayTra;
        this.ngayNhanThucTe = ngayNhanThucTe;
        this.ngayTraThucTe = ngayTraThucTe;
        this.tinhTrangPhong = tinhTrangPhong;
    }

    // Getter & Setter cho từng thuộc tính

    public Long getMaDP() {
        return maDP;
    }

    public void setMaDP(Long maDP) {
        this.maDP = maDP;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public BigDecimal getGiaPhong() {
        return giaPhong;
    }

    public void setGiaPhong(BigDecimal giaPhong) {
        this.giaPhong = giaPhong;
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

    public LocalDateTime getNgayNhanThucTe() {
        return ngayNhanThucTe;
    }

    public void setNgayNhanThucTe(LocalDateTime ngayNhanThucTe) {
        this.ngayNhanThucTe = ngayNhanThucTe;
    }

    public LocalDateTime getNgayTraThucTe() {
        return ngayTraThucTe;
    }

    public void setNgayTraThucTe(LocalDateTime ngayTraThucTe) {
        this.ngayTraThucTe = ngayTraThucTe;
    }

    public TinhTrangPhong getTinhTrangPhong() {
        return tinhTrangPhong;
    }

    public void setTinhTrangPhong(TinhTrangPhong tinhTrangPhong) {
        this.tinhTrangPhong = tinhTrangPhong;
    }

    public int getSoLanChuyenPhong() {
        return soLanChuyenPhong;
    }

    public void setSoLanChuyenPhong(int soLanChuyenPhong) {
        this.soLanChuyenPhong = soLanChuyenPhong;
    }
}
