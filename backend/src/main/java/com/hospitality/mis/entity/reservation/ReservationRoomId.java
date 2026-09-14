package com.hospitality.mis.entity.reservation;
import java.io.Serializable;
import java.util.Objects;
/** Giá trị khóa ghép xác định một phòng trong một đặt phòng. */
public class ReservationRoomId implements Serializable {
    /** ID đặt phòng thành phần của khóa ghép. */
    private Long reservation;
    /** Mã phòng thành phần của khóa ghép. */
    private String room;
    /** Constructor rỗng bắt buộc cho IdClass/JPA. */
    public ReservationRoomId() {}
    /** Tạo định danh đầy đủ của dòng đặt phòng. */
    public ReservationRoomId(Long reservation, String room) { this.reservation = reservation; this.room = room; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof ReservationRoomId that)) return false; return Objects.equals(reservation, that.reservation) && Objects.equals(room, that.room); }
    @Override public int hashCode() { return Objects.hash(reservation, room); }
}
