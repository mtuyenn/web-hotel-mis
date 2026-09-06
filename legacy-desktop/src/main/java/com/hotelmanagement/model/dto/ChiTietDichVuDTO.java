package com.hotelmanagement.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lớp ChiTietDichVuDTO dùng để truyền dữ liệu chi tiết dịch vụ
 * mà khách hàng sử dụng trong quá trình đặt phòng.
 * 
 * Mục đích:
 * - Hỗ trợ hiển thị thông tin dịch vụ một cách đầy đủ, dễ hiểu
 */
public class ChiTietDichVuDTO {

    // Mã đặt phòng (liên kết tới đơn đặt phòng)
    private Long maDP;

    // Mã dịch vụ
    private String maDV;

    // Tên dịch vụ (dùng để hiển thị)
    private String tenDichVu;

    // Giá của dịch vụ
    private BigDecimal giaDV;

    // Ngày sử dụng dịch vụ
    private LocalDate ngaySuDung;

    // Số lượng dịch vụ sử dụng
    private Integer soLuong;

    // Thành tiền = giá * số lượng
    private BigDecimal thanhTien;

    /**
     * Constructor mặc định
     */
    public ChiTietDichVuDTO() {
    }

    /**
     * Constructor có tham số
     * Tự động tính thành tiền khi khởi tạo object
     */
    public ChiTietDichVuDTO(Long maDP, String maDV, String tenDichVu, BigDecimal giaDV,
            LocalDate ngaySuDung, Integer soLuong) {
        this.maDP = maDP;
        this.maDV = maDV;
        this.tenDichVu = tenDichVu;
        this.giaDV = giaDV;
        this.ngaySuDung = ngaySuDung;
        this.soLuong = soLuong;

        // Tính thành tiền nếu dữ liệu hợp lệ, ngược lại gán = 0
        this.thanhTien = (giaDV != null && soLuong != null)
                ? giaDV.multiply(BigDecimal.valueOf(soLuong))
                : BigDecimal.ZERO;
    }

    // Getter & Setter cho từng thuộc tính

    public Long getMaDP() {
        return maDP;
    }

    public void setMaDP(Long maDP) {
        this.maDP = maDP;
    }

    public String getMaDV() {
        return maDV;
    }

    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public BigDecimal getGiaDV() {
        return giaDV;
    }

    public void setGiaDV(BigDecimal giaDV) {
        this.giaDV = giaDV;
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

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
}