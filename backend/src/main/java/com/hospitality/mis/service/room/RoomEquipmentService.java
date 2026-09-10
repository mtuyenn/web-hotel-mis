package com.hospitality.mis.service.room;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.room.RoomEquipmentRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.room.RoomEquipmentDtos;
import com.hospitality.mis.entity.room.RoomEquipment;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.reservation.IdempotencySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class RoomEquipmentService {
    private final RoomEquipmentRepository equipment;
    private final RoomRepository rooms;
    private final AuditService audit;
    private final IdempotencySupport idempotency = new IdempotencySupport();

    public RoomEquipmentService(RoomEquipmentRepository equipment, RoomRepository rooms) {
        this(equipment, rooms, null);
    }

    @Autowired
    public RoomEquipmentService(RoomEquipmentRepository equipment, RoomRepository rooms, AuditService audit) {
        this.equipment = equipment;
        this.rooms = rooms;
        this.audit = audit;
    }

    /** Controller-compatible entry point; actor and key come from request context. */
    @Transactional
    public RoomEquipmentDtos.Response add(RoomEquipmentDtos.CreateRequest request) {
        return add(request, currentActor(), currentIdempotencyKey());
    }

    @Transactional
    public RoomEquipmentDtos.Response add(RoomEquipmentDtos.CreateRequest request, String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        String fingerprint = IdempotencySupport.fingerprint("ROOM_EQUIPMENT|" + request.roomId() + "|" + request.name()
                + "|" + request.originalValue() + "|" + request.purchasedOn() + "|" + request.quantity());
        return idempotency.execute("room-equipment", key, actor, fingerprint, () -> {
            var room = rooms.findForUpdate(request.roomId())
                    .orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
            RoomEquipment item = new RoomEquipment();
            item.setRoom(room); item.setName(request.name()); item.setOriginalValue(request.originalValue());
            item.setPurchasedOn(request.purchasedOn()); item.setQuantity(request.quantity());
            var saved = equipment.save(item);
            if (audit != null) audit.record(actor, "ROOM_EQUIPMENT_ADDED", "ROOM", room.getId(), null, request.name(), null);
            return toResponse(saved);
        });
    }

    @Transactional(readOnly = true)
    public List<RoomEquipmentDtos.Response> list(String roomId) {
        return equipment.findByRoomIdAndActiveTrueOrderByNameAsc(roomId).stream().map(this::toResponse).toList();
    }

    private String currentActor() {
        return SecurityActor.currentActor();
    }

    private String currentIdempotencyKey() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getHeader("Idempotency-Key");
        }
        throw new DomainException("IDEMPOTENCY_KEY_REQUIRED", "Thiếu Idempotency-Key");
    }

    private String authenticatedActor(String supplied) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) return SecurityActor.requireBoundActor(supplied);
        if (supplied == null || supplied.isBlank()) throw new DomainException("ACTOR_REQUIRED", "Thiếu actor đã xác thực");
        return supplied;
    }

    private RoomEquipmentDtos.Response toResponse(RoomEquipment e) {
        return new RoomEquipmentDtos.Response(e.getId(), e.getRoom().getId(), e.getName(), e.getOriginalValue(),
                e.getPurchasedOn(), e.getQuantity(), e.isActive());
    }
}
