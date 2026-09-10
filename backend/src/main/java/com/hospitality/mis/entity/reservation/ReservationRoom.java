package com.hospitality.mis.entity.reservation;
import com.hospitality.mis.dao.room.RoomStatusConverter;


import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Canonical owner of one reserved room in a reservation. */
@Entity
@Table(name = "reservation_rooms")
@IdClass(ReservationRoomId.class)
@Access(AccessType.FIELD)
public class ReservationRoom {
    @Id @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    @Id @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    @Column(name = "check_in", nullable = false) private LocalDateTime checkIn;
    @Column(name = "check_out", nullable = false) private LocalDateTime checkOut;
    /** Checkout before any extension. Kept immutable so each room is billed independently. */
    @Column(name = "original_check_out", nullable = false) private LocalDateTime originalCheckOut;
    @Convert(converter = com.hospitality.mis.dao.room.RoomStatusConverter.class)
    @Column(name = "status", nullable = false, length = 30) private RoomStatus status = RoomStatus.RESERVED;
    @Column(name = "transfer_count", nullable = false) private int transferCount;
    public Reservation getReservation() { return reservation; }
    public Room getRoom() { return room; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public LocalDateTime getOriginalCheckOut() { return originalCheckOut == null ? checkOut : originalCheckOut; }
    public RoomStatus getStatus() { return status; }
    public int getTransferCount() { return transferCount; }
    public void setReservation(Reservation value) { reservation = value; }
    public void setRoom(Room value) { room = value; }
    public void setCheckIn(LocalDateTime value) { checkIn = value; }
    public void setCheckOut(LocalDateTime value) {
        checkOut = value;
        if (originalCheckOut == null) originalCheckOut = value;
    }
    public void setOriginalCheckOut(LocalDateTime value) { originalCheckOut = value; }
    public void setStatus(RoomStatus value) { status = value; }
    public void incrementTransferCount() { transferCount++; }

    @PrePersist
    void ensureOriginalCheckout() {
        if (originalCheckOut == null) originalCheckOut = checkOut;
    }
}
