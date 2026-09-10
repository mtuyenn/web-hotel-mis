package com.hospitality.mis.service.room;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.room.ReservationOverlapPort;
import com.hospitality.mis.dao.room.RoomStore;
import com.hospitality.mis.dto.room.RoomDtos;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomAvailabilityPolicy;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.entity.room.RoomType;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public RoomDtos.Response updateStatus(String id, RoomStatus status, String suppliedActor) {
        String actor = authenticatedActor(suppliedActor);
        if (status == null) {
            throw new DomainException("INVALID_ROOM_STATUS", "Trạng thái phòng không hợp lệ");
        }
        Room room = rooms.findForUpdate(id)
                .orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng: " + id));
        RoomStatus before = room.getStatus();
        if (before == status) return toResponse(room);

        requireSupportedTransition(id, before, status);
        if (status == RoomStatus.READY
                && overlaps.hasOverlap(id, LocalDateTime.now(), LocalDateTime.now().plusNanos(1))) {
            throw new DomainException("ROOM_NOT_AVAILABLE", "Phòng đang có lượt đặt hoặc lưu trú hoạt động");
        }

        room.setStatus(status);
        audit.record(actor, "ROOM_STATUS_CHANGED", "ROOM", id,
                before == null ? null : before.databaseCode(), status.databaseCode(), null);
        return toResponse(room);
    }

    private void requireSupportedTransition(String id, RoomStatus before, RoomStatus next) {
        if (before == RoomStatus.OCCUPIED && next == RoomStatus.READY) {
            throw new DomainException("INVALID_ROOM_TRANSITION",
                    "Cannot move an occupied room to READY: " + id);
        }
        if ((before == RoomStatus.READY && next == RoomStatus.MAINTENANCE)
                || (before == RoomStatus.MAINTENANCE && next == RoomStatus.READY)) {
            if (!hasMaintenanceRole()) {
                throw new DomainException("ROOM_STATUS_FORBIDDEN",
                    "Only TECHNICAL staff or managers may start or finish maintenance");
            }
            return;
        }
        throw new DomainException("INVALID_ROOM_TRANSITION",
                    "Room state transition is not supported by the current contract: " + before + " -> " + next);
    }

    private boolean hasMaintenanceRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> authority.equals("ROLE_ADMIN")
                        || authority.equals("ROLE_DIRECTOR")
                        || authority.equals("ROLE_MANAGER")
                        || authority.equals("ROLE_TECHNICAL"));
    }

    private String authenticatedActor(String supplied) {
        try {
            return SecurityActor.requireBoundActor(supplied);
        } catch (AuthenticationCredentialsNotFoundException exception) {
            throw new DomainException("ACTOR_REQUIRED", "Authenticated actor is required");
        } catch (AccessDeniedException exception) {
            throw new DomainException("ACTOR_MISMATCH", "Actor does not match the current principal");
        }
    }

    public RoomDtos.Response toResponse(Room room) {
        RoomType roomType = room.getRoomType();
        return new RoomDtos.Response(room.getId(), room.getName(), roomType.getId(), roomType.getName(),
                roomType.getDailyPrice(), room.getFloor(), room.getStatus());
    }
}
