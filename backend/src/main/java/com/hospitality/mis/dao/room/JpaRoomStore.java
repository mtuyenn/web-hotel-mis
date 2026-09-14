package com.hospitality.mis.dao.room;



import com.hospitality.mis.dao.room.RoomStore;

import com.hospitality.mis.entity.room.Room;

import com.hospitality.mis.entity.room.RoomStatus;

import org.springframework.stereotype.Repository;



import java.util.List;

import java.util.Optional;



/**

 * Bộ chuyển tiếp lưu trữ phòng cho thành phần sở hữu phòng chuẩn hóa.
 */

@Repository

public class JpaRoomStore implements RoomStore {

    /** Repository JPA của aggregate phòng; adapter không tự thêm quy tắc nghiệp vụ. */
    private final RoomRepository repository;



    public JpaRoomStore(RoomRepository repository) {

        this.repository = repository;

    }



    @Override

    /** Tìm phòng theo bộ lọc và chuyển kết quả persistence sang danh sách domain. */
    public List<Room> search(String roomTypeId, RoomStatus status) {

        return repository.search(roomTypeId, status).stream()

                .map(room -> (Room) room)

                .toList();

    }

    @Override
    /** Tìm phòng và nạp loại phòng cho mapper trong transaction đọc. */
    public Optional<Room> findById(String roomId) {
        return repository.findByIdWithRoomType(roomId).map(room -> room);
    }



    @Override

    /** Lấy phòng dưới khóa ghi để dịch vụ cập nhật trạng thái an toàn trong transaction. */
    public Optional<Room> findForUpdate(String roomId) {

        return repository.findForUpdate(roomId).map(room -> room);

    }

}
