package com.hospitality.mis.controller.operations;
import com.hospitality.mis.dto.operations.MaintenanceDtos;


import com.hospitality.mis.service.operations.MaintenanceService;
import com.hospitality.mis.middleware.security.SecurityActor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/operations/maintenance")
public class MaintenanceController {
    private final MaintenanceService service;
    public MaintenanceController(MaintenanceService service) { this.service = service; }
    @GetMapping("/room/{roomId}")
    @PreAuthorize("@departmentAccess.allows(authentication, 'MAINTENANCE_READ')")
    public List<MaintenanceDtos.Response> byRoom(@PathVariable String roomId) { return service.byRoom(roomId); }
    @PostMapping
    @PreAuthorize("@departmentAccess.allows(authentication, 'MAINTENANCE_WRITE')")
    public MaintenanceDtos.Response create(@Valid @RequestBody MaintenanceDtos.CreateRequest request) { return service.create(request, SecurityActor.currentActor()); }
    @PatchMapping("/{id}/status")
    @PreAuthorize("@departmentAccess.allows(authentication, 'MAINTENANCE_WRITE')")
    public MaintenanceDtos.Response status(@PathVariable String id, @Valid @RequestBody MaintenanceDtos.StatusRequest request) { return service.updateStatus(id, request, SecurityActor.currentActor()); }
}
