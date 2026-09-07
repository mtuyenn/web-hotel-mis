package com.hospitality.mis.room.application;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.application.AuditService;
import com.hospitality.mis.room.api.RoomDtos;
import com.hospitality.mis.room.domain.Room;
import com.hospitality.mis.room.domain.RoomAvailabilityPolicy;
import com.hospitality.mis.room.domain.RoomStatus;
import com.hospitality.mis.room.domain.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomService {
    private final RoomStore rooms;
    private final ReservationOverlapPort overlaps;
    private final AuditService audit;
    private final RoomAvailabilityPolicy availabilityPolicy;

    @Autowired
    public RoomService(RoomStore rooms, ReservationOverlapPort overlaps, AuditService audit) {
        this(rooms, overlaps, audit, new RoomAvailabilityPolicy());
    }

    public RoomService(RoomStore rooms, ReservationOverlapPort overlaps, AuditService audit,
                       RoomAvailabilityPolicy availabilityPolicy) {
        this.rooms = rooms;
        this.overlaps = overlaps;
        this.audit = audit;
        this.availabilityPolicy = availabilityPolicy;
    }

    @Transactional(readOnly = true)
    public List<RoomDtos.Response> search(String type, RoomStatus status) {
        return rooms.search(type, status).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RoomDtos.Availability> availability(LocalDateTime from, LocalDateTime to, String type) {
        try {
            availabilityPolicy.validateInterval(from, to);
        } catch (IllegalArgumentException exception) {
            throw new DomainException("INVALID_INTERVAL", "Thời gian nhận phải trước thời gian trả");
        }
        return rooms.search(type, null).stream().map(room -> {
            RoomType roomType = room.getRoomType();
            boolean overlap = overlaps.hasOverlap(room.getId(), from, to);
            return new RoomDtos.Availability(room.getId(), roomType.getId(), roomType.getName(),
                    roomType.getDailyPrice(), room.getFloor(), availabilityPolicy.isAvailable(room, overlap));
        }).toList();
    }

    @Transactional
    public RoomDtos.Response updateStatus(String id, RoomStatus status, String actor) {
        if (status == null) {
            throw new DomainException("INVALID_ROOM_STATUS", "Trạng thái phòng không hợp lệ");
        }
        Room room = rooms.findForUpdate(id)
                .orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng: " + id));
        RoomStatus before = room.getStatus();
        room.setStatus(status);
        audit.record(actor, "ROOM_STATUS_CHANGED", "ROOM", id,
                before == null ? null : before.databaseCode(), status.databaseCode(), null);
        return toResponse(room);
    }

    public RoomDtos.Response toResponse(Room room) {
        RoomType roomType = room.getRoomType();
        return new RoomDtos.Response(room.getId(), room.getName(), roomType.getId(), roomType.getName(),
                roomType.getDailyPrice(), room.getFloor(), room.getStatus());
    }
}
