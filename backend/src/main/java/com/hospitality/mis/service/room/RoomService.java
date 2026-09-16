/* Service này kiểm tra các chuyển trạng thái phòng trước khi ghi xuống database. */
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
import java.time.Clock;
import java.util.List;

@Service
public class RoomService {
    /** Kho phòng, hỗ trợ tìm kiếm và khóa dòng khi đổi trạng thái. */
    private final RoomStore rooms;
    /** Kiểm tra đặt phòng/lưu trú chồng lấn khi báo availability hoặc READY. */
    private final ReservationOverlapPort overlaps;
    /** Ghi actor và trạng thái trước/sau khi đổi phòng. */
    private final AuditService audit;
    /** Policy xác thực khoảng thời gian và tính phòng có sẵn. */
    private final RoomAvailabilityPolicy availabilityPolicy;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

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

    @Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }

    /** Tìm phòng theo loại và trạng thái, trả DTO không làm thay đổi dữ liệu. */
    @Transactional(readOnly = true)
    public List<RoomDtos.Response> search(String type, RoomStatus status) {
        return rooms.search(type, status).stream().map(this::toResponse).toList();
    }

    /** Kiểm tra khoảng thời gian hợp lệ rồi tính availability theo overlap từng phòng. */
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

    /** Khóa phòng, áp policy transition/quyền bảo trì và audit trạng thái mới. */
    @Transactional
    public RoomDtos.Response updateStatus(String id, RoomStatus status, String suppliedActor) {
        String actor = authenticatedActor(suppliedActor);
        if (status == null || !status.isOperationalStatus()) {
            throw new DomainException("INVALID_ROOM_STATUS", "Trạng thái phòng không hợp lệ");
        }
        Room room = rooms.findForUpdate(id)
                .orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng: " + id));
        RoomStatus before = room.getStatus();
        if (before == status) return toResponse(room);

        requireSupportedTransition(id, before, status);
        LocalDateTime now = LocalDateTime.now(clock);
        if (status == RoomStatus.READY && overlaps.hasOverlap(id, now, now.plusNanos(1))) {
            throw new DomainException("ROOM_NOT_AVAILABLE", "Phòng đang có lượt đặt hoặc lưu trú hoạt động");
        }

        room.setStatus(status);
        audit.record(actor, "ROOM_STATUS_CHANGED", "ROOM", id,
                before == null ? null : before.databaseCode(), status.databaseCode(), null);
        return toResponse(room);
    }

    /** Chặn transition ngoài hợp đồng và yêu cầu role kỹ thuật/quản lý cho bảo trì. */
    private void requireSupportedTransition(String id, RoomStatus before, RoomStatus next) {
        if (before == RoomStatus.OCCUPIED && next == RoomStatus.READY) {
            throw new DomainException("INVALID_ROOM_TRANSITION",
                    "Cannot move an occupied room to READY: " + id);
        }
        if (before == RoomStatus.READY && next == RoomStatus.MAINTENANCE) {
            if (!hasRole("ROLE_TECHNICAL") && !hasManagementRole())
                throw new DomainException("ROOM_STATUS_FORBIDDEN", "Only TECHNICAL staff or managers may start maintenance");
            return;
        }
        if (before == RoomStatus.MAINTENANCE && next == RoomStatus.READY) {
            if (hasRole("ROLE_TECHNICAL"))
                throw new DomainException("ROOM_RELEASE_COMMAND_REQUIRED", "Technical phải dùng command release sau nghiệm thu");
            if (!hasManagementRole())
                throw new DomainException("ROOM_STATUS_FORBIDDEN", "Housekeeping không được tự mở khóa phòng");
            return;
        }
        throw new DomainException("INVALID_ROOM_TRANSITION",
                    "Room state transition is not supported by the current contract: " + before + " -> " + next);
    }

    /** Kiểm tra principal có một trong các role được phép quản lý bảo trì. */
    private boolean hasManagementRole() { return hasRole("ROLE_ADMIN") || hasRole("ROLE_DIRECTOR") || hasRole("ROLE_MANAGER"); }
    private boolean hasRole(String role) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    /** Ràng buộc actor với principal và chuyển lỗi Spring Security thành lỗi miền. */
    private String authenticatedActor(String supplied) {
        try {
            return SecurityActor.requireBoundActor(supplied);
        } catch (AuthenticationCredentialsNotFoundException exception) {
            throw new DomainException("ACTOR_REQUIRED", "Authenticated actor is required");
        } catch (AccessDeniedException exception) {
            throw new DomainException("ACTOR_MISMATCH", "Actor does not match the current principal");
        }
    }

    /** Chuyển phòng và loại phòng thành DTO trả cho API. */
    public RoomDtos.Response toResponse(Room room) {
        RoomType roomType = room.getRoomType();
        return new RoomDtos.Response(room.getId(), room.getName(), roomType.getId(), roomType.getName(),
                roomType.getDailyPrice(), room.getFloor(), room.getStatus());
    }
}
