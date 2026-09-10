package com.hospitality.mis.dao.room;



import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;



import java.util.List;

import java.util.Optional;



public interface RoomRepository extends JpaRepository<Room, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)

    @Query("select r from Room r join fetch r.roomType where r.id = :id")
    Optional<Room> findForUpdate(@Param("id") String id);


    @Lock(LockModeType.PESSIMISTIC_WRITE)

    @Query("select r from Room r join fetch r.roomType where r.id in :ids order by r.id")
    List<Room> findAllForUpdateOrdered(@Param("ids") List<String> ids);


    @Query("select r from Room r join fetch r.roomType where (:type is null or r.roomType.id = :type) and (:status is null or r.status = :status) order by r.id")
    List<Room> search(@Param("type") String type, @Param("status") RoomStatus status);
}
