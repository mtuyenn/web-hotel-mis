package com.hospitality.mis.dao.room;



import java.time.LocalDateTime;



/** Read-only port for reservation interval overlap used by room availability. */

@FunctionalInterface

public interface ReservationOverlapPort {

    boolean hasOverlap(String roomId, LocalDateTime from, LocalDateTime to);

}
