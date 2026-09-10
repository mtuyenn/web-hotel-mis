package com.hospitality.mis.entity.reservation;
import java.io.Serializable;
import java.util.Objects;
public class ReservationRoomId implements Serializable {
    private Long reservation;
    private String room;
    public ReservationRoomId() {}
    public ReservationRoomId(Long reservation, String room) { this.reservation = reservation; this.room = room; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof ReservationRoomId that)) return false; return Objects.equals(reservation, that.reservation) && Objects.equals(room, that.room); }
    @Override public int hashCode() { return Objects.hash(reservation, room); }
}
