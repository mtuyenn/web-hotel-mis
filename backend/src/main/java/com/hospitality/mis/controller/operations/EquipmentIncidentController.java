package com.hospitality.mis.controller.operations;
import com.hospitality.mis.dto.operations.EquipmentIncidentDtos;


import com.hospitality.mis.service.operations.EquipmentIncidentService;
import com.hospitality.mis.middleware.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/reservations")
public class EquipmentIncidentController {
    private final EquipmentIncidentService service;
    public EquipmentIncidentController(EquipmentIncidentService service) { this.service = service; }
    @PostMapping("/{reservationId}/equipment-incidents")

    @PreAuthorize("@departmentAccess.allows(authentication, 'INCIDENT_WRITE')")
    public EquipmentIncidentDtos.Response record(@PathVariable Long reservationId,
                                                  @Valid @RequestBody EquipmentIncidentDtos.CreateRequest request,
                                                  @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.record(reservationId, request, SecurityActor.currentActor(), idempotencyKey);
    }
}
