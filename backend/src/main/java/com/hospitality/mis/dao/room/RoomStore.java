package com.hospitality.mis.dao.room;



import com.hospitality.mis.entity.room.Room;

import com.hospitality.mis.entity.room.RoomStatus;



import java.util.List;

import java.util.Optional;



/** Canonical room persistence port used by room application services. */

public interface RoomStore {

    List<Room> search(String roomTypeId, RoomStatus status);



    Optional<Room> findForUpdate(String roomId);

}
