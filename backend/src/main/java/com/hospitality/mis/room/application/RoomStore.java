package com.hospitality.mis.room.application;

import com.hospitality.mis.room.domain.Room;
import com.hospitality.mis.room.domain.RoomStatus;

import java.util.List;
import java.util.Optional;

/** Canonical room persistence port used by room application services. */
public interface RoomStore {
    List<Room> search(String roomTypeId, RoomStatus status);

    Optional<Room> findForUpdate(String roomId);
}
