package com.hospitality.mis.service.room;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dao.room.RoomTypeRepository;
import com.hospitality.mis.dto.room.RoomAdminDtos;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomTypeCatalogStatus;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RoomAdminService {
    private final RoomRepository rooms; private final RoomTypeRepository types; private final AuditService audit;
    private final RoomService roomService;
    public RoomAdminService(RoomRepository rooms, RoomTypeRepository types, AuditService audit, RoomService roomService) { this.rooms = rooms; this.types = types; this.audit = audit; this.roomService = roomService; }
    @Transactional(readOnly = true)
    public List<RoomAdminDtos.Response> list() { return rooms.findAll().stream().map(this::toResponse).toList(); }
    @Transactional
    public RoomAdminDtos.Response create(RoomAdminDtos.Request request, String actor) {
        if (rooms.existsById(request.id())) throw new DomainException("ROOM_EXISTS", "Mã phòng đã tồn tại");
        var type = types.findById(request.roomTypeId()).filter(x -> x.getCatalogStatus() == RoomTypeCatalogStatus.ACTIVE)
                .orElseThrow(() -> new DomainException("ROOM_TYPE_NOT_ACTIVE", "Chỉ loại phòng ACTIVE mới được gán phòng"));
        Room room = new Room(); room.setId(request.id()); room.setName(request.name()); room.setFloor(request.floor()); room.setDescription(request.description()); room.setRoomType(type);
        if (request.status() != null && request.status() != RoomStatus.READY)
            throw new DomainException("INVALID_INITIAL_ROOM_STATUS", "Phòng mới chỉ được tạo ở trạng thái READY");
        room.setStatus(RoomStatus.READY); rooms.save(room);
        audit.record(actor, "ROOM_CREATED", "ROOM", request.id(), null, request.roomTypeId(), null); return toResponse(room);
    }
    @Transactional
    public RoomAdminDtos.Response update(String id, RoomAdminDtos.Request request, String actor) {
        Room room = rooms.findForUpdate(id).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        var type = types.findById(request.roomTypeId()).filter(x -> x.getCatalogStatus() == RoomTypeCatalogStatus.ACTIVE)
                .orElseThrow(() -> new DomainException("ROOM_TYPE_NOT_ACTIVE", "Chỉ loại phòng ACTIVE mới được gán phòng"));
        room.setName(request.name()); room.setFloor(request.floor()); room.setDescription(request.description()); room.setRoomType(type);
        if (request.status() != null && request.status() != room.getStatus())
            roomService.updateStatus(id, request.status(), actor);
        audit.record(actor, "ROOM_UPDATED", "ROOM", id, null, request.roomTypeId(), null); return toResponse(room);
    }
    private RoomAdminDtos.Response toResponse(Room room) { return new RoomAdminDtos.Response(room.getId(), room.getName(), room.getRoomType().getId(), room.getRoomType().getName(), room.getFloor(), room.getDescription(), room.getStatus()); }
}
