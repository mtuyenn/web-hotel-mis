package com.hospitality.mis.operations.api;

import com.hospitality.mis.operations.application.EquipmentIncidentService;
import com.hospitality.mis.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/reservations")
public class EquipmentIncidentController {
    private final EquipmentIncidentService service;
    public EquipmentIncidentController(EquipmentIncidentService service) { this.service = service; }
    @PostMapping("/{reservationId}/equipment-incidents")
    @PreAuthorize("hasAnyRole('MANAGER','FRONT_DESK','HOUSEKEEPING')")
    public EquipmentIncidentDtos.Response record(@PathVariable Long reservationId,
                                                  @Valid @RequestBody EquipmentIncidentDtos.CreateRequest request) {
        return service.record(reservationId, request, SecurityActor.currentActor());
    }
}
