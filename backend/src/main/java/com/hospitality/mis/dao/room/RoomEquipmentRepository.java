package com.hospitality.mis.dao.room;

import com.hospitality.mis.entity.room.RoomEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomEquipmentRepository extends JpaRepository<RoomEquipment, Long> {
    List<RoomEquipment> findByRoomIdAndActiveTrueOrderByNameAsc(String roomId);
}
