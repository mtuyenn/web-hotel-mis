package com.hospitality.mis.dao.room;



import com.hospitality.mis.entity.room.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;



/** Kho JPA cho danh mục loại phòng; hành vi truy vấn dùng các phương thức chuẩn của JpaRepository. */
public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
}
