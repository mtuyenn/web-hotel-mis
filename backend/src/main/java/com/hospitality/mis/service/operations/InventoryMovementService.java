package com.hospitality.mis.service.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.billing.ServiceRepository;
import com.hospitality.mis.dao.operations.InventoryMovementRepository;
import com.hospitality.mis.dto.operations.InventoryMovementDtos;
import com.hospitality.mis.entity.operations.InventoryMovement;
import org.springframework.stereotype.Service;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.governance.DurableIdempotencyService;
import com.hospitality.mis.service.reservation.IdempotencySupport;
import com.hospitality.mis.middleware.security.SecurityActor;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Clock;

/** Điều chỉnh tồn kho dịch vụ và lưu sổ chuyển động có actor, lý do. */
@Service
public class InventoryMovementService {
    /** Kho chuyển động và kho dịch vụ; service được khóa khi thay đổi tồn. */
    private final InventoryMovementRepository movements; private final ServiceRepository services;
    /** Audit số lượng tăng/giảm sau mỗi chuyển động. */
    private final AuditService audit;
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    private DurableIdempotencyService durableIdempotency;
    public InventoryMovementService(InventoryMovementRepository movements, ServiceRepository services, AuditService audit) { this.movements = movements; this.services = services; this.audit = audit; }
    @org.springframework.beans.factory.annotation.Autowired
    void setBusinessClock(Clock clock) { this.clock = clock; }
    @org.springframework.beans.factory.annotation.Autowired
    void setDurableIdempotency(DurableIdempotencyService durableIdempotency) { this.durableIdempotency = durableIdempotency; }
    /** Khóa dịch vụ, kiểm tra không âm, cập nhật tồn và ghi chuyển động. */
    @Transactional
    public InventoryMovementDtos.Response record(InventoryMovementDtos.CreateRequest request, String actor) {
        return record(request, actor, "direct-" + java.util.UUID.randomUUID());
    }

    @Transactional
    public InventoryMovementDtos.Response record(InventoryMovementDtos.CreateRequest request, String actor, String key) {
        String boundActor = SecurityActor.requireBoundActor(actor);
        String fingerprint = IdempotencySupport.fingerprint("INVENTORY|" + request.serviceId() + "|" + request.type()
                + "|" + request.quantity() + "|" + request.reason());
        if (durableIdempotency != null) {
            return durableIdempotency.execute("inventory-movement", key, boundActor, fingerprint,
                    InventoryMovementDtos.Response.class, () -> recordOnce(request, boundActor));
        }
        IdempotencySupport.requireKey(key);
        return recordOnce(request, boundActor);
    }

    private InventoryMovementDtos.Response recordOnce(InventoryMovementDtos.CreateRequest request, String actor) {
        if (actor == null || actor.isBlank()) throw new DomainException("ACTOR_REQUIRED", "Thiếu actor cập nhật tồn kho");
        var service = services.findWithLockById(request.serviceId()).orElseThrow(() -> new DomainException("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
        int signed = switch (request.type()) {
            case ISSUE, WASTE -> -request.quantity();
            case RECEIPT, RETURN, ADJUSTMENT -> request.quantity();
        };
        if (service.getStockQuantity() + signed < 0) throw new DomainException("INSUFFICIENT_STOCK", "Tồn kho không đủ");
        service.setStockQuantity(service.getStockQuantity() + signed);
        InventoryMovement m = new InventoryMovement(); m.setService(service); m.setType(request.type()); m.setQuantity(request.quantity()); m.setActorId(actor); m.setReason(request.reason()); m.setOccurredAt(LocalDateTime.now(clock));
        m = movements.save(m);
        audit.record(actor, "INVENTORY_MOVEMENT_RECORDED", "SERVICE", service.getId(), null, String.valueOf(signed), request.reason());
        return toResponse(m);
    }
    @Transactional(readOnly = true)
    /** Liệt kê chuyển động tồn kho của dịch vụ mới nhất trước. */
    public java.util.List<InventoryMovementDtos.Response> list(String serviceId) { return movements.findByServiceIdOrderByOccurredAtDesc(serviceId).stream().map(this::toResponse).toList(); }
    /** Chuyển chuyển động tồn kho thành DTO audit-friendly. */
    private InventoryMovementDtos.Response toResponse(InventoryMovement m) { return new InventoryMovementDtos.Response(m.getId(), m.getService().getId(), m.getType(), m.getQuantity(), m.getActorId(), m.getOccurredAt(), m.getReason()); }
}
