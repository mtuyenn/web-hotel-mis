package com.hospitality.mis.reservation.domain;
import com.hospitality.mis.room.domain.Room;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "room_transfers") @Access(AccessType.FIELD)
public class RoomTransfer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reservation_id", nullable = false) private Reservation reservation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "from_room_id", nullable = false) private Room fromRoom;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "to_room_id", nullable = false) private Room toRoom;
    @Column(name = "transferred_at") private LocalDateTime transferredAt;
    @Column(name = "reason", length = 255) private String reason;
    public Long getId() { return id; }
    public Reservation getReservation() { return reservation; }
    public Room getFromRoom() { return fromRoom; }
    public Room getToRoom() { return toRoom; }
    public LocalDateTime getTransferredAt() { return transferredAt; }
    public String getReason() { return reason; }
    public void setReservation(Reservation v) { reservation = v; }
    public void setFromRoom(Room v) { fromRoom = v; }
    public void setToRoom(Room v) { toRoom = v; }
    public void setTransferredAt(LocalDateTime v) { transferredAt = v; }
    public void setReason(String v) { reason = v; }
}
