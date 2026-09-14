package com.hospitality.mis.service.identity;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.dao.identity.EmployeeShiftRepository;
import com.hospitality.mis.dto.identity.EmployeeShiftDtos;
import com.hospitality.mis.entity.identity.EmployeeShift;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
@Service
public class EmployeeShiftService {
    private final EmployeeShiftRepository shifts; private final EmployeeRepository employees; private final AuditService audit;
    public EmployeeShiftService(EmployeeShiftRepository shifts, EmployeeRepository employees, AuditService audit) { this.shifts = shifts; this.employees = employees; this.audit = audit; }
    @Transactional
    public EmployeeShiftDtos.Response assign(EmployeeShiftDtos.Request request, String actor) {
        if (!request.startsAt().isBefore(request.endsAt())) throw new DomainException("INVALID_SHIFT_INTERVAL", "Giờ bắt đầu phải trước giờ kết thúc");
        var employee = employees.findById(request.employeeId()).orElseThrow(() -> new DomainException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
        var shift = new EmployeeShift(); shift.setEmployee(employee); shift.setShiftDate(request.shiftDate()); shift.setShiftCode(request.shiftCode()); shift.setStartsAt(request.startsAt()); shift.setEndsAt(request.endsAt()); shift.setCreatedBy(actor);
        shifts.save(shift); audit.record(actor, "EMPLOYEE_SHIFT_ASSIGNED", "EMPLOYEE_SHIFT", "new", null, request.employeeId(), null); return toResponse(shift);
    }
    @Transactional(readOnly = true)
    public List<EmployeeShiftDtos.Response> list(LocalDate date, String employeeId) {
        LocalDate selected = date == null ? LocalDate.now() : date;
        var data = employeeId == null ? shifts.findByShiftDateOrderByStartsAtAsc(selected) : shifts.findByEmployeeEmployeeIdAndShiftDateOrderByStartsAtAsc(employeeId, selected);
        return data.stream().map(this::toResponse).toList();
    }
    private EmployeeShiftDtos.Response toResponse(EmployeeShift x) { return new EmployeeShiftDtos.Response(x.getId(), x.getEmployee().getEmployeeId(), x.getShiftDate(), x.getShiftCode(), x.getStartsAt(), x.getEndsAt(), x.getStatus().name(), x.getCreatedBy()); }
}
