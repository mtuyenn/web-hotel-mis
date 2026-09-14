package com.hospitality.mis.dao.room;



import com.hospitality.mis.entity.room.Room;

import com.hospitality.mis.entity.room.RoomStatus;



import java.util.List;

import java.util.Optional;



/** Cổng lưu trữ phòng chuẩn hóa được các dịch vụ ứng dụng phòng sử dụng. */

public interface RoomStore {

    /** Tìm phòng theo loại và trạng thái; giá trị null của bộ lọc nghĩa là không giới hạn trường đó. */
    List<Room> search(String roomTypeId, RoomStatus status);

    /** Lấy phòng theo mã cho truy vấn chi tiết read-only. */
    Optional<Room> findById(String roomId);



    /** Lấy phòng đã được khóa ghi để caller thực hiện cập nhật trong transaction. */
    Optional<Room> findForUpdate(String roomId);

}
