package com.hotelmanagement.web.legacy.model.dto;

import java.time.LocalDateTime;

/**
 * Lớp ChuyenPhongDTO dùng để truyền dữ liệu liên quan đến việc chuyển phòng
 * của khách hàng trong quá trình lưu trú.
 * 
 * Mục đích:
 * - Hỗ trợ hiển thị thông tin chuyển phòng rõ ràng, dễ quản lý
 */
public class ChuyenPhongDTO {

    // Mã chuyển phòng
    private Long maChuyenPhong;

    // Mã đặt phòng (liên kết tới đơn đặt phòng)
    private Long maDP;

    // Mã phòng cũ
    private String maPhongCu;

    // Mã phòng mới
    private String maPhongMoi;

    // Tên phòng cũ (dùng để hiển thị)
    private String tenPhongCu;

    // Tên phòng mới (dùng để hiển thị)
    private String tenPhongMoi;

    // Thời gian thực hiện chuyển phòng
    private LocalDateTime ngayChuyen;

    // Lý do chuyển phòng (ví dụ: nâng cấp, sự cố,...)
    private String lyDo;

    /**
     * Constructor mặc định
     */
    public ChuyenPhongDTO() {
    }

    /**
     * Constructor đầy đủ tham số
     * Dùng để khởi tạo nhanh đối tượng khi cần
     */
    public ChuyenPhongDTO(Long maChuyenPhong, Long maDP, String maPhongCu, String maPhongMoi,
            String tenPhongCu, String tenPhongMoi, LocalDateTime ngayChuyen, String lyDo) {
        this.maChuyenPhong = maChuyenPhong;
        this.maDP = maDP;
        this.maPhongCu = maPhongCu;
        this.maPhongMoi = maPhongMoi;
        this.tenPhongCu = tenPhongCu;
        this.tenPhongMoi = tenPhongMoi;
        this.ngayChuyen = ngayChuyen;
        this.lyDo = lyDo;
    }

    // Getter & Setter cho từng thuộc tính

    public Long getMaChuyenPhong() {
        return maChuyenPhong;
    }

    public void setMaChuyenPhong(Long maChuyenPhong) {
        this.maChuyenPhong = maChuyenPhong;
    }

    public Long getMaDP() {
        return maDP;
    }

    public void setMaDP(Long maDP) {
        this.maDP = maDP;
    }

    public String getMaPhongCu() {
        return maPhongCu;
    }

    public void setMaPhongCu(String maPhongCu) {
        this.maPhongCu = maPhongCu;
    }

    public String getMaPhongMoi() {
        return maPhongMoi;
    }

    public void setMaPhongMoi(String maPhongMoi) {
        this.maPhongMoi = maPhongMoi;
    }

    public String getTenPhongCu() {
        return tenPhongCu;
    }

    public void setTenPhongCu(String tenPhongCu) {
        this.tenPhongCu = tenPhongCu;
    }

    public String getTenPhongMoi() {
        return tenPhongMoi;
    }

    public void setTenPhongMoi(String tenPhongMoi) {
        this.tenPhongMoi = tenPhongMoi;
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
