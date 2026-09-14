package com.hospitality.mis.controller.operations;

import com.hospitality.mis.dto.operations.HousekeepingDtos;
import com.hospitality.mis.entity.operations.HousekeepingTaskStatus;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.operations.HousekeepingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/operations/housekeeping")
public class HousekeepingController {
    private final HousekeepingService service;
    public HousekeepingController(HousekeepingService service) { this.service = service; }

    @GetMapping("/tasks")
    @PreAuthorize("@departmentAccess.allows(authentication, 'HOUSEKEEPING_TASK_READ')")
    public List<HousekeepingDtos.Response> list(@RequestParam(required = false) String roomId,
                                                @RequestParam(required = false) String assignee,
                                                @RequestParam(required = false) HousekeepingTaskStatus status) {
        return service.list(roomId, assignee, status);
    }

    @PostMapping("/tasks")
    @PreAuthorize("@departmentAccess.allows(authentication, 'HOUSEKEEPING_TASK_WRITE')")
    public HousekeepingDtos.Response create(@Valid @RequestBody HousekeepingDtos.CreateRequest request) {
        return service.create(request, SecurityActor.currentActor());
    }

    @PatchMapping("/tasks/{id}")
    @PreAuthorize("@departmentAccess.allows(authentication, 'HOUSEKEEPING_TASK_WRITE')")
    public HousekeepingDtos.Response update(@PathVariable Long id, @Valid @RequestBody HousekeepingDtos.UpdateRequest request) {
        return service.update(id, request, SecurityActor.currentActor());
    }
}
