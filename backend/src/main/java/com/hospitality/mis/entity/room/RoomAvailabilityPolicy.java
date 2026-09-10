package com.hospitality.mis.entity.room;



import java.time.LocalDateTime;

import java.util.Objects;



/** Availability rule owned by the room bounded context. */

public final class RoomAvailabilityPolicy {

    public void validateInterval(LocalDateTime from, LocalDateTime to) {

        if (from == null || to == null || !from.isBefore(to)) {

            throw new IllegalArgumentException("from must be before to");

        }

    }



    /**

     * A room is available only when its operational state allows allocation

     * and the reservation adapter found no half-open interval overlap.

     */

    public boolean isAvailable(Room room, boolean hasOverlappingReservation) {

        Objects.requireNonNull(room, "room");

        return !room.getStatus().blocksAvailability() && !hasOverlappingReservation;

    }



    public boolean isAvailable(RoomStatus status, boolean hasOverlappingReservation) {

        Objects.requireNonNull(status, "status");

        return !status.blocksAvailability() && !hasOverlappingReservation;

    }

}
