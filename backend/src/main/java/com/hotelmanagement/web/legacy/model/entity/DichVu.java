package com.hotelmanagement.web.legacy.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Lớp DichVu đại diện cho bảng "DichVu" trong cơ sở dữ liệu.
 * 
 * Lớp này lưu trữ thông tin về các dịch vụ có sẵn trong khách sạn,
 * bao gồm mã dịch vụ, tên dịch vụ và giá dịch vụ.
 * 
 * Các thuộc tính:
 * - maDV: Mã dịch vụ (khóa chính)
 * - tenDichVu: Tên dịch vụ
 * - giaDV: Giá của dịch vụ
 * 
 * Lớp này phục vụ cho việc quản lý danh mục dịch vụ
 * và tính toán chi phí dịch vụ trong các đơn đặt phòng.
 */
@Entity
@Table(name = "DichVu")
public class DichVu {
    @Id
    @Column(name = "MADV", length = 10)
    private String maDV;

    @Column(name = "TENDICHVU", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenDichVu;

    @Column(name = "GIADV", precision = 10, scale = 2, nullable = false)
    private BigDecimal giaDV;

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

