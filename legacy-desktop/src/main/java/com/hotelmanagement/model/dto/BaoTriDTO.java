package com.hotelmanagement.model.dto;

import com.hotelmanagement.model.enums.TinhTrangBaoTri;

import java.time.LocalDate;

/**
 * Lớp BaoTriDTO (Data Transfer Object) dùng để truyền dữ liệu
 * liên quan đến bảo trì phòng giữa các tầng trong ứng dụng.
 * 
 * Mục đích
 * - Tránh lộ trực tiếp dữ liệu từ database ra ngoài
 * - Thuận tiện cho việc hiển thị và xử lý dữ liệu
 */
public class BaoTriDTO {

    // Mã bảo trì
    private String maBT;

    // Mã phòng được bảo trì
    private String maPhong;

    // Tên loại phòng (dùng để hiển thị dễ hiểu hơn)
    private String tenLoaiPhong;

    // Loại bảo trì (ví dụ: sửa chữa, kiểm tra...)
    private String loaiBaoTri;

    // Ngày thực hiện bảo trì
    private LocalDate ngayBaoTri;

    // Tình trạng bảo trì (đang xử lý, đã hoàn thành,...)
    private TinhTrangBaoTri tinhTrangBTri;

    // Mô tả chi tiết về bảo trì
    private String moTa;

    /**
     * Constructor mặc định
     */
    public BaoTriDTO() {
    }

    /**
     * Constructor đầy đủ tham số
     * Dùng để khởi tạo nhanh đối tượng khi cần
     */
    public BaoTriDTO(String maBT, String maPhong, String tenLoaiPhong, String loaiBaoTri,
            LocalDate ngayBaoTri, TinhTrangBaoTri tinhTrangBTri, String moTa) {
        this.maBT = maBT;
        this.maPhong = maPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.loaiBaoTri = loaiBaoTri;
        this.ngayBaoTri = ngayBaoTri;
        this.tinhTrangBTri = tinhTrangBTri;
        this.moTa = moTa;
    }

    // Getter và Setter cho từng thuộc tính

    public String getMaBT() {
        return maBT;
    }

    public void setMaBT(String maBT) {
        this.maBT = maBT;
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