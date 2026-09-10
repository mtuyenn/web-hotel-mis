package com.hospitality.mis.service.operations;



import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.dao.operations.MaintenanceWorkOrderRepository;
import com.hospitality.mis.dto.operations.MaintenanceDtos;
import com.hospitality.mis.entity.operations.MaintenanceStatus;
import com.hospitality.mis.entity.operations.MaintenanceWorkOrder;
import com.hospitality.mis.dao.room.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MaintenanceService {
        // Maintenance work-order repository; all persistence goes through this DAO.
    private final MaintenanceWorkOrderRepository orders;
        // Room repository; row locks protect concurrent room-state changes.
    private final RoomRepository rooms;
        // Record the actor and before/after values for the audit trail.
    private final AuditService audit;

    public MaintenanceService(MaintenanceWorkOrderRepository orders, RoomRepository rooms, AuditService audit) {
        this.orders = orders; this.rooms = rooms; this.audit = audit;
    }

    @Transactional
    public MaintenanceDtos.Response create(MaintenanceDtos.CreateRequest request, String actor) {
        // Do not create a duplicate work-order identifier.
        if (orders.existsById(request.id())) throw new DomainException("MAINTENANCE_EXISTS", "Mã phiếu bảo trì đã tồn tại");
        // Lock the room in this transaction so concurrent operations cannot change it at once.
        var room = rooms.findForUpdate(request.roomId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        // An occupied room cannot be moved into maintenance.
        if (room.getStatus() == com.hospitality.mis.entity.room.RoomStatus.OCCUPIED)
            throw new DomainException("ROOM_OCCUPIED", "Không thể đưa phòng đang có khách vào bảo trì");
        var order = new MaintenanceWorkOrder(); order.setId(request.id()); order.setRoom(room);
        order.setMaintenanceType(request.type()); order.setScheduledDate(request.scheduledDate()); order.setDescription(request.description());
        // Opening a work order blocks the room from new reservations until completion.
        order.setStatus(MaintenanceStatus.CHUA_XU_LY); room.setStatus(com.hospitality.mis.entity.room.RoomStatus.MAINTENANCE); rooms.save(room); orders.save(order);
        audit.record(actor, "MAINTENANCE_CREATED", "MAINTENANCE_WORK_ORDER", request.id(), null, request.roomId(), null);
        return toResponse(order);
    }

    @Transactional
    public MaintenanceDtos.Response updateStatus(String id, MaintenanceDtos.StatusRequest request, String actor) {
        // Update an existing work order and expose business failures through standard codes.
        var order = orders.findById(id).orElseThrow(() -> new DomainException("MAINTENANCE_NOT_FOUND", "Không tìm thấy phiếu bảo trì"));
        MaintenanceStatus next;
        try { next = MaintenanceStatus.valueOf(request.status().trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new DomainException("INVALID_MAINTENANCE_STATUS", "Trạng thái bảo trì không hợp lệ"); }
        // Status may only progress through PENDING -> IN_PROGRESS -> COMPLETED.
        if (!allowedTransition(order.getStatus(), next))
            throw new DomainException("INVALID_MAINTENANCE_TRANSITION",
                    "Cannot move maintenance from " + order.getStatus() + " to " + next);
        var before = order.getStatus().name(); order.setStatus(next);
        var room = rooms.findForUpdate(order.getRoom().getId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Room not found"));
        // Completion returns the room to READY; other states keep it in MAINTENANCE.
        room.setStatus(next == MaintenanceStatus.DA_HOAN_THANH
                ? com.hospitality.mis.entity.room.RoomStatus.READY
                : com.hospitality.mis.entity.room.RoomStatus.MAINTENANCE);
        audit.record(actor, "MAINTENANCE_STATUS_CHANGED", "MAINTENANCE_WORK_ORDER", id, before, next.name(), null);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDtos.Response> byRoom(String roomId) { return orders.findByRoomIdOrderByScheduledDateDesc(roomId).stream().map(this::toResponse).toList(); }

    private MaintenanceDtos.Response toResponse(MaintenanceWorkOrder o) { return new MaintenanceDtos.Response(o.getId(), o.getRoom().getId(), o.getMaintenanceType(), o.getScheduledDate(), o.getStatus().name(), o.getDescription()); }

    private boolean allowedTransition(MaintenanceStatus current, MaintenanceStatus next) {
        // Repeating the same state is allowed so retries remain harmless.
        if (current == next) return true;
        return (current == MaintenanceStatus.CHUA_XU_LY && next == MaintenanceStatus.DANG_BAO_TRI)
                || (current == MaintenanceStatus.DANG_BAO_TRI && next == MaintenanceStatus.DA_HOAN_THANH);
    }
}
