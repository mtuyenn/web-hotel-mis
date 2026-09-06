package com.hotelmanagement.web.legacy.model.keyclass;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/**
 * Lớp ChiTietDichVuId dùng để biểu diễn khóa chính kép (composite key)
 * cho bảng ChiTietDichVu.
 * 
 * Khóa chính bao gồm:
 * - datPhong: mã đặt phòng
 * - dichVu: mã dịch vụ
 * - ngaySuDung: ngày sử dụng dịch vụ
 * 
 * Lớp này được sử dụng trong JPA để định danh duy nhất một bản ghi.
 */
public class ChiTietDichVuId {

    // Mã đặt phòng (liên kết tới bảng DatPhong)
    private Long datPhong;

    // Mã dịch vụ (liên kết tới bảng DichVu)
    private String dichVu;

    // Ngày sử dụng dịch vụ
    private LocalDate ngaySuDung;

    /**
     * Constructor mặc định (bắt buộc đối với JPA)
     */
    public ChiTietDichVuId() {
    }

    /**
     * Constructor có tham số để khởi tạo nhanh đối tượng
     */
    public ChiTietDichVuId(Long datPhong, String dichVu, LocalDate ngaySuDung) {
        this.datPhong = datPhong;
        this.dichVu = dichVu;
        this.ngaySuDung = ngaySuDung;
    }

    /**
     * Ghi đè phương thức equals để so sánh 2 đối tượng
     * Dùng để kiểm tra xem 2 khóa chính có giống nhau hay không
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true; // cùng tham chiếu
        if (o == null || getClass() != o.getClass())
            return false; // khác kiểu

        ChiTietDichVuId that = (ChiTietDichVuId) o;

        return Objects.equals(datPhong, that.datPhong)
                && Objects.equals(dichVu, that.dichVu)
                && Objects.equals(ngaySuDung, that.ngaySuDung);
    }

    /**
     * Ghi đè hashCode để đảm bảo đồng nhất với equals
     * Dùng trong các cấu trúc dữ liệu như HashMap, HashSet và Hibernate
     */
    @Override
    public int hashCode() {
        return Objects.hash(datPhong, dichVu, ngaySuDung);
    }

    // Getter và Setter cho từng thuộc tính

    public Long getDatPhong() {
        return datPhong;
    }

    public void setDatPhong(Long datPhong) {
        this.datPhong = datPhong;
    }

    public String getDichVu() {
        return dichVu;
    }

    public void setDichVu(String dichVu) {
        this.dichVu = dichVu;
    }

    public LocalDate getNgaySuDung() {
        return ngaySuDung;
    }

    public void setNgaySuDung(LocalDate ngaySuDung) {
        this.ngaySuDung = ngaySuDung;
    }
}
