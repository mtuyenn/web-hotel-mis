package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.ChiTietDatPhong;
import com.hotelmanagement.model.entity.DatPhong;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import com.hotelmanagement.model.keyclass.ChiTietDatPhongId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface dùng để quản lý chi tiết đặt phòng:
 * bao gồm truy vấn, thêm, sửa, xóa và cập nhật trạng thái phòng
 */
public interface ChiTietDatPhongDAO {

    // Lấy thông tin đặt phòng theo mã, bao gồm đầy đủ chi tiết
    public DatPhong findByMaDPWithFullDetails(Long maDP);

    // Lấy danh sách chi tiết đặt phòng theo mã phòng
    public List<ChiTietDatPhong> findByPhong(String maPhong);

    // Tìm chi tiết đặt phòng theo ID
    public ChiTietDatPhong findById(ChiTietDatPhongId id);

    // Tìm chi tiết đặt phòng theo mã đặt phòng và mã phòng
    public ChiTietDatPhong findByMaDPAndMaPhong(Long maDP, String maPhong);

    // Thêm mới chi tiết đặt phòng
    public void save(ChiTietDatPhong ctp);

    // Cập nhật trạng thái phòng theo mã đặt phòng và mã phòng
    public void updateTrangThaiByMaDPAndMaPhong(Long maDP, String maPhong, TinhTrangPhong trangThai);

    // Cập nhật thông tin chi tiết đặt phòng
    public void update(ChiTietDatPhong ctp);

    // Xóa chi tiết đặt phòng theo ID
    public void delete(ChiTietDatPhongId id);

    // Cập nhật thời gian nhận và trả phòng
    public void updateThoiGian(Long maDP, String maPhong, LocalDateTime ngayNhan, LocalDateTime ngayTra);

    // Cập nhật trạng thái tất cả phòng theo mã đặt phòng
    public void updateTrangThaiByMaDP(Long maDP, TinhTrangPhong trangThai);

    // Cập nhật đổi phòng (từ phòng cũ sang phòng mới)
    public void updatePhongMoi(Long maDP, String maPhongCu, String maPhongMoi);
}