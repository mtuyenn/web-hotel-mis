package com.hospitality.mis.controller.operations;

import com.hospitality.mis.dto.operations.InventoryMovementDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.operations.InventoryMovementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/services/{serviceId}/inventory-movements")
public class InventoryMovementController {
    private final InventoryMovementService service;
    public InventoryMovementController(InventoryMovementService service) { this.service = service; }
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'INVENTORY_READ')")
    public List<InventoryMovementDtos.Response> list(@PathVariable String serviceId) { return service.list(serviceId); }
    @PostMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'INVENTORY_WRITE')")
    public InventoryMovementDtos.Response record(@PathVariable String serviceId, @Valid @RequestBody InventoryMovementDtos.CreateRequest request) { if (!serviceId.equals(request.serviceId())) {
        throw new com.hospitality.mis.common.exception.DomainException("SERVICE_PATH_MISMATCH", "service_id must match the URL");
    }
    return service.record(request, SecurityActor.currentActor()); }
}
