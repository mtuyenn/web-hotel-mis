package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.LoaiPhong;
import com.hotelmanagement.model.entity.Phong;
import com.hotelmanagement.model.enums.TinhTrangPhong;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface dùng để quản lý phòng:
 * bao gồm truy vấn, tìm phòng trống và cập nhật trạng thái phòng
 */
public interface PhongDAO {

    // Tìm phòng theo mã phòng
    public Phong findByMaPhong(String maPhong);

    // Lấy toàn bộ danh sách phòng
    public List<Phong> findAll();

    // Tìm phòng trống theo thời gian và loại phòng
    public List<Phong> timPhongTrong(LocalDateTime ngayNhan, LocalDateTime ngayTra, LoaiPhong loaiPhong);

    // Cập nhật trạng thái phòng
    public void updateTinhTrang(String maPhong, TinhTrangPhong tinhTrang);

    // Thêm mới phòng
    public void save(Phong phong);

    // Tìm phòng theo trạng thái
    public List<Phong> timBoiTinhTrang(TinhTrangPhong tinhTrangPhong);

    // Lấy danh sách phòng đang có khách ở
    public List<Phong> timPhongDangO();

    // Lấy danh sách phòng đang dọn dẹp
    public List<Phong> timPhongDangDonDep();

    // Lấy danh sách phòng đang bảo trì
    public List<Phong> timPhongBaoTri();
}