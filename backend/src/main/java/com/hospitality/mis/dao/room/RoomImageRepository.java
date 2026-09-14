package com.hospitality.mis.dao.room;

import com.hospitality.mis.entity.room.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Kho metadata ảnh, luôn giới hạn truy vấn theo phòng để tránh lộ ảnh ngoài object scope. */
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoomIdAndActiveTrueOrderByDisplayOrderAscIdAsc(String roomId);
    long countByRoomIdAndActiveTrue(String roomId);

    @Query("select i from RoomImage i join fetch i.room where i.id = :id and i.room.id = :roomId")
    Optional<RoomImage> findByIdAndRoomId(@Param("id") Long id, @Param("roomId") String roomId);

    @Query("select i.relativePath from RoomImage i")
    List<String> findAllRelativePaths();
}
