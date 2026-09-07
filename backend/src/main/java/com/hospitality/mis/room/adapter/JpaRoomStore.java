package com.hospitality.mis.room.adapter;

import com.hospitality.mis.room.application.RoomStore;
import com.hospitality.mis.room.domain.Room;
import com.hospitality.mis.room.domain.RoomStatus;
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
