package com.hospitality.mis.controller.identity;
import com.hospitality.mis.dto.identity.EmployeeShiftDtos;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.identity.EmployeeShiftService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/api/hr/shifts")
public class EmployeeShiftController {
    private final EmployeeShiftService service;
    public EmployeeShiftController(EmployeeShiftService service) { this.service = service; }
    @GetMapping @PreAuthorize("@departmentAccess.allows(authentication, 'SHIFT_READ')")
    public List<EmployeeShiftDtos.Response> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                 @RequestParam(required = false) String employeeId) { return service.list(date, employeeId); }
    @PostMapping @PreAuthorize("@departmentAccess.allows(authentication, 'SHIFT_WRITE')")
    public EmployeeShiftDtos.Response assign(@Valid @RequestBody EmployeeShiftDtos.Request request) { return service.assign(request, SecurityActor.currentActor()); }
}
