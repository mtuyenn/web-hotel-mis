package com.hospitality.mis.controller.auth;

import com.hospitality.mis.dto.auth.EmployeeAdminDtos;
import com.hospitality.mis.service.identity.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auth/employees")
public class EmployeeAdminController {
    private final EmployeeService service;
    public EmployeeAdminController(EmployeeService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_READ')")
    public List<EmployeeAdminDtos.Response> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_READ')")
    public EmployeeAdminDtos.Response detail(@PathVariable String employeeId) { return service.detail(employeeId); }

    @GetMapping("/{employeeId}/sessions")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_READ')")
    public List<EmployeeAdminDtos.SessionResponse> sessions(@PathVariable String employeeId) { return service.sessions(employeeId); }

    @DeleteMapping("/{employeeId}/sessions/{sessionId}")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_PROVISION') && @employeeService.canResetEmployee(authentication, #employeeId)")
    public void revokeSession(@PathVariable String employeeId, @PathVariable Long sessionId) { service.revokeSession(employeeId, sessionId); }

    @PatchMapping("/{employeeId}/status")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_PROVISION') && @employeeService.canResetEmployee(authentication, #employeeId)")
    public EmployeeAdminDtos.Response status(@PathVariable String employeeId, @Valid @RequestBody EmployeeAdminDtos.StatusRequest request) {
        return service.setEnabled(employeeId, request.enabled());
    }

    @PatchMapping("/{employeeId}/role")
    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_PROVISION') && @employeeService.canManageEmployeeRole(authentication, #employeeId, #request.role())")
    public EmployeeAdminDtos.Response role(@PathVariable String employeeId,
                                           @Valid @RequestBody EmployeeAdminDtos.RoleRequest request) {
        return service.setRole(employeeId, request.role());
    }
}
