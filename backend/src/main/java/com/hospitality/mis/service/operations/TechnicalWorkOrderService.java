package com.hospitality.mis.service.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.TechnicalWorkOrderRepository;
import com.hospitality.mis.dao.room.RoomEquipmentRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.operations.TechnicalWorkOrderDtos;
import com.hospitality.mis.entity.operations.TechnicalWorkOrder;
import com.hospitality.mis.entity.operations.TechnicalWorkOrderStatus;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TechnicalWorkOrderService {
    private final TechnicalWorkOrderRepository orders; private final RoomRepository rooms;
    private final RoomEquipmentRepository equipment; private final AuditService audit;
    public TechnicalWorkOrderService(TechnicalWorkOrderRepository orders, RoomRepository rooms,
                                     RoomEquipmentRepository equipment, AuditService audit) {
        this.orders = orders; this.rooms = rooms; this.equipment = equipment; this.audit = audit;
    }
    @Transactional
    public TechnicalWorkOrderDtos.Response create(TechnicalWorkOrderDtos.CreateRequest request, String actor) {
        var room = rooms.findForUpdate(request.roomId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        TechnicalWorkOrder order = new TechnicalWorkOrder(); order.setRoom(room); order.setCreatedBy(actor);
        order.setAssignee(request.assignee()); order.setPriority(normalizePriority(request.priority())); order.setSlaDueAt(request.slaDueAt());
        order.setMaterials(request.materials()); room.setStatus(RoomStatus.MAINTENANCE); orders.save(order);
        audit.record(actor, "TECHNICAL_WORK_ORDER_CREATED", "TECHNICAL_WORK_ORDER", "new", null, request.roomId(), null);
        return toResponse(order);
    }
    @Transactional
    public TechnicalWorkOrderDtos.Response update(Long id, TechnicalWorkOrderDtos.UpdateRequest request, String actor) {
        TechnicalWorkOrder order = orders.findById(id).orElseThrow(() -> new DomainException("TECHNICAL_WORK_ORDER_NOT_FOUND", "Không tìm thấy work order"));
        TechnicalWorkOrderStatus next;
        try { next = TechnicalWorkOrderStatus.valueOf(request.status().trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new DomainException("INVALID_TECHNICAL_STATUS", "Trạng thái work order không hợp lệ"); }
        if (!order.getStatus().canTransitionTo(next)) throw new DomainException("INVALID_TECHNICAL_TRANSITION", "Chuyển trạng thái work order không hợp lệ");
        if (request.assignee() != null) order.setAssignee(request.assignee());
        if (request.materials() != null) order.setMaterials(request.materials());
        if (request.resultNote() != null) order.setResultNote(request.resultNote());
        if (request.acceptanceNote() != null) order.setAcceptanceNote(request.acceptanceNote());
        var before = order.getStatus(); order.setStatus(next); order.setUpdatedAt(LocalDateTime.now());
        audit.record(actor, "TECHNICAL_WORK_ORDER_STATUS_CHANGED", "TECHNICAL_WORK_ORDER", id.toString(), before.name(), next.name(), null);
        return toResponse(order);
    }
    @Transactional
    public TechnicalWorkOrderDtos.Response release(Long id, String actor) {
        TechnicalWorkOrder order = orders.findById(id).orElseThrow(() -> new DomainException("TECHNICAL_WORK_ORDER_NOT_FOUND", "Không tìm thấy work order"));
        if (order.getStatus() != TechnicalWorkOrderStatus.COMPLETED) throw new DomainException("TECHNICAL_ACCEPTANCE_REQUIRED", "Work order chưa được nghiệm thu");
        var room = rooms.findForUpdate(order.getRoom().getId()).orElseThrow(() -> new DomainException("ROOM_NOT_FOUND", "Không tìm thấy phòng"));
        if (room.getStatus() == RoomStatus.OCCUPIED) throw new DomainException("ROOM_OCCUPIED", "Không thể release phòng đang có khách");
        order.setStatus(TechnicalWorkOrderStatus.ROOM_RELEASED); order.setUpdatedAt(LocalDateTime.now()); room.setStatus(RoomStatus.READY);
        audit.record(actor, "TECHNICAL_ROOM_RELEASED", "TECHNICAL_WORK_ORDER", id.toString(), "COMPLETED", "ROOM_RELEASED", null);
        return toResponse(order);
    }
    @Transactional(readOnly = true)
    public List<TechnicalWorkOrderDtos.Response> list(String roomId, TechnicalWorkOrderStatus status) {
        List<TechnicalWorkOrder> data = roomId != null ? orders.findByRoomIdOrderByUpdatedAtDesc(roomId)
                : status != null ? orders.findByStatusOrderByUpdatedAtDesc(status) : orders.findAll();
        return data.stream().filter(x -> status == null || x.getStatus() == status).map(this::toResponse).toList();
    }
    private String normalizePriority(String value) {
        String p = value.trim().toUpperCase();
        if (!List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(p)) throw new DomainException("INVALID_TECHNICAL_PRIORITY", "Priority không hợp lệ");
        return p;
    }
    private TechnicalWorkOrderDtos.Response toResponse(TechnicalWorkOrder x) {
        return new TechnicalWorkOrderDtos.Response(x.getId(), x.getRoom().getId(), x.getEquipment() == null ? null : x.getEquipment().getId(),
                x.getAssignee(), x.getPriority(), x.getSlaDueAt(), x.getMaterials(), x.getResultNote(), x.getAcceptanceNote(),
                x.getStatus().name(), x.getCreatedBy(), x.getCreatedAt(), x.getUpdatedAt());
    }
}
