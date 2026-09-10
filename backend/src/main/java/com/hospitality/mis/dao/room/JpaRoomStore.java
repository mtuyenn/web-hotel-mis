package com.hospitality.mis.dao.room;



import com.hospitality.mis.dao.room.RoomStore;

import com.hospitality.mis.entity.room.Room;

import com.hospitality.mis.entity.room.RoomStatus;

import org.springframework.stereotype.Repository;



import java.util.List;

import java.util.Optional;



/**

 * Room persistence adapter for the canonical room owner.
 */

@Repository

public class JpaRoomStore implements RoomStore {

    private final RoomRepository repository;



    public JpaRoomStore(RoomRepository repository) {

        this.repository = repository;

    }



    @Override

    public List<Room> search(String roomTypeId, RoomStatus status) {

        return repository.search(roomTypeId, status).stream()

                .map(room -> (Room) room)

                .toList();

    }



    @Override

    public Optional<Room> findForUpdate(String roomId) {

        return repository.findForUpdate(roomId).map(room -> room);

    }

}
