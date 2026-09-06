package com.hotelmanagement.model.dao;

import com.hotelmanagement.model.entity.BaoTri;
import java.util.List;

// Interface dùng để định nghĩa các thao tác xử lý dữ liệu bảo trì (CRUD và truy vấn)
public interface BaoTriDAO {

    // Tìm thông tin bảo trì theo mã bảo trì
    public BaoTri findById(String maBT);

    // Lấy danh sách bảo trì theo mã phòng
    public List<BaoTri> findByPhong(String maPhong);

    // Lấy toàn bộ danh sách bảo trì
    public List<BaoTri> findAll();

    // Lấy danh sách các loại bảo trì
    public List<String> layDanhSachLoaiBaoTri();

    // Thêm mới một bản ghi bảo trì
    public void save(BaoTri baoTri);

    // Cập nhật thông tin bảo trì
    public void update(BaoTri baoTri);
}