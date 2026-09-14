package com.hospitality.mis.controller.operations;
import com.hospitality.mis.dto.operations.EquipmentIncidentDtos;


import com.hospitality.mis.service.operations.EquipmentIncidentService;
import com.hospitality.mis.middleware.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Ghi nhận sự cố thiết bị phát sinh trong một đặt phòng.
 */
@RestController
@RequestMapping("/api/operations/reservations")
public class EquipmentIncidentController {
    /** Dịch vụ tạo sự cố, kiểm tra liên kết đặt phòng và áp dụng chống ghi trùng. */
    private final EquipmentIncidentService service;
    public EquipmentIncidentController(EquipmentIncidentService service) { this.service = service; }
    /**
     * Ghi nhận sự cố qua POST /api/operations/reservations/{reservationId}/equipment-incidents.
     * reservationId là path parameter, body tạo sự cố được {@code @Valid} kiểm tra, header bắt buộc
     * {@code Idempotency-Key} bảo đảm một thao tác ghi theo khóa; response là sự cố đã ghi.
     * Chỉ INCIDENT_WRITE được phép; đặt phòng không hợp lệ, khóa trùng hoặc lỗi miền do dịch vụ xử lý.
     */
    @PostMapping("/{reservationId}/equipment-incidents")

    @PreAuthorize("@departmentAccess.allows(authentication, 'INCIDENT_WRITE')")
    public EquipmentIncidentDtos.Response record(@PathVariable Long reservationId,
                                                  @Valid @RequestBody EquipmentIncidentDtos.CreateRequest request,
                                                  @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.record(reservationId, request, SecurityActor.currentActor(), idempotencyKey);
    }

    @PatchMapping("/incidents/{id}/handoff")
    @PreAuthorize("@departmentAccess.allows(authentication, 'INCIDENT_HANDOFF')")
    public EquipmentIncidentDtos.Response handoff(@PathVariable Long id, @Valid @RequestBody EquipmentIncidentDtos.HandoffRequest request) {
        return service.handoff(id, request, SecurityActor.currentActor());
    }
}
