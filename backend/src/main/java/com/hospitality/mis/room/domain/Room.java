package com.hospitality.mis.room.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Canonical room state and scalar persistence mapping.
 *
 * The sole concrete JPA owner of the {@code rooms} table.
 */
@Entity
@Table(name = "rooms")
@Access(AccessType.FIELD)
public class Room {
    @Id
    @Column(name = "id", length = 10, nullable = false)
    private String id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "description", length = 255)
    private String description;

    @Convert(converter = RoomStatusConverter.class)
    @Column(name = "status", length = 30, nullable = false)
    private RoomStatus status = RoomStatus.READY;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Room() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rooms_room_type"))
    private RoomType roomType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        this.status = status;
    }

    public long getVersion() {
        return version;
    }
}
