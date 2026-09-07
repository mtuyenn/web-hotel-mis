package com.hospitality.mis.reservation.domain;

import com.hospitality.mis.room.domain.Room;
import com.hospitality.mis.room.domain.RoomStatus;
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
    @Convert(converter = com.hospitality.mis.room.domain.RoomStatusConverter.class)
    @Column(name = "status", nullable = false, length = 30) private RoomStatus status = RoomStatus.RESERVED;
    @Column(name = "transfer_count", nullable = false) private int transferCount;
    public Reservation getReservation() { return reservation; }
    public Room getRoom() { return room; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public RoomStatus getStatus() { return status; }
    public int getTransferCount() { return transferCount; }
    public void setReservation(Reservation value) { reservation = value; }
    public void setRoom(Room value) { room = value; }
    public void setCheckIn(LocalDateTime value) { checkIn = value; }
    public void setCheckOut(LocalDateTime value) { checkOut = value; }
    public void setStatus(RoomStatus value) { status = value; }
    public void incrementTransferCount() { transferCount++; }
}
