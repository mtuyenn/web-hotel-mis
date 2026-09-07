package com.hospitality.mis.room.adapter;

import com.hospitality.mis.room.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
}
