package com.hospitality.mis.service.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.billing.ServiceRepository;
import com.hospitality.mis.dao.operations.InventoryMovementRepository;
import com.hospitality.mis.dto.operations.InventoryMovementDtos;
import com.hospitality.mis.entity.operations.InventoryMovement;
import org.springframework.stereotype.Service;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.middleware.security.SecurityActor;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class InventoryMovementService {
    private final InventoryMovementRepository movements; private final ServiceRepository services;
    private final AuditService audit;
    public InventoryMovementService(InventoryMovementRepository movements, ServiceRepository services, AuditService audit) { this.movements = movements; this.services = services; this.audit = audit; }
    @Transactional
    public InventoryMovementDtos.Response record(InventoryMovementDtos.CreateRequest request, String actor) {
        if (actor == null || actor.isBlank()) throw new DomainException("ACTOR_REQUIRED", "Thiếu actor cập nhật tồn kho");
        var service = services.findWithLockById(request.serviceId()).orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
        int signed = request.type() == InventoryMovement.MovementType.ISSUE ? -request.quantity() : request.quantity();
        if (service.getStockQuantity() + signed < 0) throw new DomainException("INSUFFICIENT_STOCK", "Tồn kho không đủ");
        service.setStockQuantity(service.getStockQuantity() + signed);
        InventoryMovement m = new InventoryMovement(); m.setService(service); m.setType(request.type()); m.setQuantity(request.quantity()); m.setActorId(actor); m.setReason(request.reason()); m.setOccurredAt(LocalDateTime.now());
        m = movements.save(m);
        audit.record(actor, "INVENTORY_MOVEMENT_RECORDED", "SERVICE", service.getId(), null, String.valueOf(signed), request.reason());
        return toResponse(m);
    }
    @Transactional(readOnly = true)
    public java.util.List<InventoryMovementDtos.Response> list(String serviceId) { return movements.findByServiceIdOrderByOccurredAtDesc(serviceId).stream().map(this::toResponse).toList(); }
    private InventoryMovementDtos.Response toResponse(InventoryMovement m) { return new InventoryMovementDtos.Response(m.getId(), m.getService().getId(), m.getType(), m.getQuantity(), m.getActorId(), m.getOccurredAt(), m.getReason()); }
}
