package com.hospitality.mis.controller.operations;

import com.hospitality.mis.dto.operations.HousekeepingDtos;
import com.hospitality.mis.dto.operations.HousekeepingChecklistDtos;
import com.hospitality.mis.entity.operations.HousekeepingTaskStatus;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.operations.HousekeepingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.hospitality.mis.service.operations.HousekeepingChecklistService;

@RestController
@RequestMapping("/api/operations/housekeeping")
public class HousekeepingController {
    private final HousekeepingService service;
    private final HousekeepingChecklistService checklists;
    public HousekeepingController(HousekeepingService service, HousekeepingChecklistService checklists) { this.service = service; this.checklists = checklists; }

    @GetMapping("/checklist-templates") @PreAuthorize("@departmentAccess.allows(authentication, 'HOUSEKEEPING_TASK_READ')")
    public List<HousekeepingChecklistDtos.TemplateResponse> templates() { return checklists.templates(); }
    @PostMapping("/checklist-templates") @PreAuthorize("@departmentAccess.allows(authentication, 'HOUSEKEEPING_TASK_WRITE')")
    public HousekeepingChecklistDtos.TemplateResponse createTemplate(@Valid @RequestBody HousekeepingChecklistDtos.TemplateRequest request) { return checklists.createTemplate(request, SecurityActor.currentActor()); }
    @GetMapping("/tasks/{id}/checklist-results") @PreAuthorize("@departmentAccess.allows(authentication, 'HOUSEKEEPING_TASK_READ')")
    public List<HousekeepingChecklistDtos.ResultResponse> results(@PathVariable Long id) { return checklists.results(id); }
    @PostMapping("/tasks/{id}/checklist-results") @PreAuthorize("@departmentAccess.allows(authentication, 'HOUSEKEEPING_TASK_WRITE')")
    public HousekeepingChecklistDtos.ResultResponse addResult(@PathVariable Long id, @Valid @RequestBody HousekeepingChecklistDtos.ResultRequest request) { return checklists.addResult(id, request, SecurityActor.currentActor()); }

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
