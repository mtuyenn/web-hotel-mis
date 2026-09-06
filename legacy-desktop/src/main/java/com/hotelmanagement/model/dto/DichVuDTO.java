package com.hotelmanagement.model.dto;

import java.math.BigDecimal;

/**
 * Lớp DichVuDTO dùng để lưu trữ và truyền thông tin cơ bản
 * của một dịch vụ, bao gồm mã dịch vụ, tên dịch vụ và giá.
 * 
 * Lớp này phục vụ cho việc hiển thị danh sách dịch vụ
 * và xử lý các chức năng liên quan đến dịch vụ.
 */
public class DichVuDTO {

    private String maDV;
    private String tenDichVu;
    private BigDecimal giaDV;

    public DichVuDTO() {
    }

    public DichVuDTO(String maDV, String tenDichVu, BigDecimal giaDV) {
        this.maDV = maDV;
        this.tenDichVu = tenDichVu;
        this.giaDV = giaDV;
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
}