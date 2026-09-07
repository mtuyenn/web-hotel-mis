package com.hospitality.mis.operations.application;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.application.AuditService;
import com.hospitality.mis.operations.adapter.MaintenanceWorkOrderRepository;
import com.hospitality.mis.operations.api.MaintenanceDtos;
import com.hospitality.mis.operations.domain.BaoTri;
import com.hospitality.mis.operations.domain.TinhTrangBaoTri;
import com.hospitality.mis.room.adapter.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MaintenanceService {
    private final MaintenanceWorkOrderRepository orders;
    private final RoomRepository rooms;
    private final AuditService audit;

    public MaintenanceService(MaintenanceWorkOrderRepository orders, RoomRepository rooms, AuditService audit) {
        this.orders = orders; this.rooms = rooms; this.audit = audit;
    }

    @Transactional
    public MaintenanceDtos.Response create(MaintenanceDtos.CreateRequest request, String actor) {
        if (orders.existsById(request.id())) throw new DomainException("MAINTENANCE_EXISTS", "Mã phiếu bảo trì đã tồn tại");
        var room = rooms.findForUpdate(request.roomId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        var order = new BaoTri(); order.setMaBT(request.id()); order.setRoom(room);
        order.setLoaiBaoTri(request.type()); order.setNgayBaoTri(request.scheduledDate()); order.setMoTa(request.description());
        order.setTinhTrangBTri(TinhTrangBaoTri.CHUA_XU_LY); rooms.save(room); orders.save(order);
        audit.record(actor, "MAINTENANCE_CREATED", "MAINTENANCE_WORK_ORDER", request.id(), null, request.roomId(), null);
        return toResponse(order);
    }

    @Transactional
    public MaintenanceDtos.Response updateStatus(String id, MaintenanceDtos.StatusRequest request, String actor) {
        var order = orders.findById(id).orElseThrow(() -> new DomainException("MAINTENANCE_NOT_FOUND", "Không tìm thấy phiếu bảo trì"));
        TinhTrangBaoTri next;
        try { next = TinhTrangBaoTri.valueOf(request.status().trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new DomainException("INVALID_MAINTENANCE_STATUS", "Trạng thái bảo trì không hợp lệ"); }
        var before = order.getTinhTrangBTri().name(); order.setTinhTrangBTri(next);
        audit.record(actor, "MAINTENANCE_STATUS_CHANGED", "MAINTENANCE_WORK_ORDER", id, before, next.name(), null);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDtos.Response> byRoom(String roomId) { return orders.findByRoomIdOrderByNgayBaoTriDesc(roomId).stream().map(this::toResponse).toList(); }

    private MaintenanceDtos.Response toResponse(BaoTri o) { return new MaintenanceDtos.Response(o.getMaBT(), o.getRoom().getId(), o.getLoaiBaoTri(), o.getNgayBaoTri(), o.getTinhTrangBTri().name(), o.getMoTa()); }
}
