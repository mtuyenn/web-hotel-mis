package com.hotelmanagement.web.legacy.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lớp DatPhongDTO dùng để truyền dữ liệu liên quan đến việc đặt phòng
 * trong hệ thống quản lý khách sạn.
 * 
 * Mục đích:
 * - Tổng hợp thông tin đặt phòng, khách hàng, nhân viên và các chi tiết liên
 * quan
 */
public class DatPhongDTO {

    // Mã đặt phòng
    private Long maDP;

    // Mã khách hàng
    private Long maKH;

    // Tên khách hàng (dùng để hiển thị)
    private String tenKH;

    // Số điện thoại khách hàng
    private String soDienThoaiKH;

    // Mã nhân viên xử lý
    private String maNV;

    // Tên nhân viên (dùng để hiển thị)
    private String tenNV;

    // Ngày tạo đơn đặt phòng
    private LocalDateTime ngayDat;

    // Số tiền đặt cọc
    private BigDecimal giaCoc;

    // Danh sách chi tiết các phòng đã đặt
    private List<ChiTietDatPhongDTO> chiTietDatPhongs;

    // Danh sách các dịch vụ đã sử dụng
    private List<ChiTietDichVuDTO> chiTietDichVus;

    // Thông tin hóa đơn liên quan
    private HoaDonDTO hoaDon;

    // Trạng thái đơn (VD: DA_DAT, DANG_O, DA_TRA_PHONG,...)
    private String trangThai;

    // Tổng tiền phòng (tính sẵn để hiển thị nhanh)
    private BigDecimal tongTienPhong;

    // Tổng tiền phải trả (bao gồm phòng + dịch vụ)
    private BigDecimal tongTienPhaiTra;

    /**
     * Constructor mặc định
     */
    public DatPhongDTO() {
    }

    // Getter & Setter cho từng thuộc tính

    public Long getMaDP() {
        return maDP;
    }

    public void setMaDP(Long maDP) {
        this.maDP = maDP;
    }

    public Long getMaKH() {
        return maKH;
    }

    public void setMaKH(Long maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getSoDienThoaiKH() {
        return soDienThoaiKH;
    }

    public void setSoDienThoaiKH(String soDienThoaiKH) {
        this.soDienThoaiKH = soDienThoaiKH;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public BigDecimal getGiaCoc() {
        return giaCoc;
    }

    public void setGiaCoc(BigDecimal giaCoc) {
        this.giaCoc = giaCoc;
    }

    public List<ChiTietDatPhongDTO> getChiTietDatPhongs() {
        return chiTietDatPhongs;
    }

    public void setChiTietDatPhongs(List<ChiTietDatPhongDTO> chiTietDatPhongs) {
        this.chiTietDatPhongs = chiTietDatPhongs;
    }

    public List<ChiTietDichVuDTO> getChiTietDichVus() {
        return chiTietDichVus;
    }

    public void setChiTietDichVus(List<ChiTietDichVuDTO> chiTietDichVus) {
        this.chiTietDichVus = chiTietDichVus;
    }

    public HoaDonDTO getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDonDTO hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getTongTienPhong() {
        return tongTienPhong;
    }

    public void setTongTienPhong(BigDecimal tongTienPhong) {
        this.tongTienPhong = tongTienPhong;
    }

    public BigDecimal getTongTienPhaiTra() {
        return tongTienPhaiTra;
    }

    public void setTongTienPhaiTra(BigDecimal tongTienPhaiTra) {
        this.tongTienPhaiTra = tongTienPhaiTra;
    }
}
