package com.hospitality.mis.controller.identity;

import com.hospitality.mis.dto.operations.BackofficeDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.identity.AttendanceLeaveService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/** HR attendance and leave endpoints. */
@RestController
public class AttendanceLeaveController {
    private final AttendanceLeaveService service;
    public AttendanceLeaveController(AttendanceLeaveService service) { this.service = service; }

    @GetMapping("/api/hr/attendance")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_READ')")
    public java.util.List<BackofficeDtos.AttendanceResponse> attendance(@RequestParam(required = false) LocalDate date) { return service.attendance(date); }

    @PostMapping("/api/hr/attendance/import")
    @PreAuthorize("@departmentAccess.allows(authentication, 'SHIFT_WRITE')")
    public java.util.List<BackofficeDtos.AttendanceResponse> importAttendance(@Valid @RequestBody BackofficeDtos.AttendanceImportRequest request) { return service.importAttendance(request, SecurityActor.currentActor()); }

    @GetMapping("/api/hr/leave-requests")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_READ')")
    public java.util.List<BackofficeDtos.LeaveResponse> leaves(@RequestParam(required = false) String status) { return service.leaves(status); }

    @PostMapping("/api/hr/leave-requests")
    @PreAuthorize("@departmentAccess.allows(authentication, 'SHIFT_WRITE')")
    public BackofficeDtos.LeaveResponse createLeave(@Valid @RequestBody BackofficeDtos.LeaveRequest request) { return service.createLeave(request, SecurityActor.currentActor()); }

    @PostMapping("/api/hr/leave-requests/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','DIRECTOR','ADMIN')")
    public BackofficeDtos.LeaveResponse approveLeave(@PathVariable long id) { return service.decideLeave(id, true, SecurityActor.currentActor()); }

    @PostMapping("/api/hr/leave-requests/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','DIRECTOR','ADMIN')")
    public BackofficeDtos.LeaveResponse rejectLeave(@PathVariable long id) { return service.decideLeave(id, false, SecurityActor.currentActor()); }
}
