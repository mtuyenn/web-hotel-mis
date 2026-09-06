package com.hotelmanagement.model.keyclass;

import java.io.Serializable;
import java.util.Objects;

/**
 * Lớp này dùng làm khóa chính tổng hợp (Composite Primary Key)
 * cho entity ChiTietDatPhong.
 **/
public class ChiTietDatPhongId implements Serializable {

    private Long datPhong;
    private String phong;

    public ChiTietDatPhongId() {

    }

    public ChiTietDatPhongId(Long datPhong, String phong) {
        this.datPhong = datPhong;
        this.phong = phong;
    }

    /**
     * So sánh 2 object có bằng nhau không
     * 
     * Rất quan trọng trong JPA vì:
     * - Dùng để xác định entity trong Persistence Context
     * - Nếu sai có thể gây lỗi duplicate hoặc không tìm thấy dữ liệu
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChiTietDatPhongId that = (ChiTietDatPhongId) o;
        return Objects.equals(datPhong, that.datPhong) &&
                Objects.equals(phong, that.phong);
    }

    /**
     * Tạo mã hash cho object
     * 
     * Bắt buộc override khi đã override equals()
     * Dùng trong HashMap, HashSet và cơ chế cache của Hibernate
     */
    @Override
    public int hashCode() {
        return Objects.hash(datPhong, phong);
    }

    public Long getDatPhong() {
        return datPhong;
    }

    public void setDatPhong(Long datPhong) {
        this.datPhong = datPhong;
    }

    public String getPhong() {
        return phong;
    }

    public void setPhong(String phong) {
        this.phong = phong;
    }
}
