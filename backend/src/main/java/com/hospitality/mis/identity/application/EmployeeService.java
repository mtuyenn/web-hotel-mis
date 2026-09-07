package com.hospitality.mis.identity.application;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.identity.adapter.EmployeeRepository;
import com.hospitality.mis.identity.domain.Employee;
import com.hospitality.mis.identity.domain.EmployeeRole;
import com.hospitality.mis.governance.application.AuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Application use cases owned by the identity boundary. */
@Service
public class EmployeeService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private final EmployeeRepository employees;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    public EmployeeService(EmployeeRepository employees, PasswordEncoder passwordEncoder, AuditService audit) {
        this.employees = employees;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    @Transactional
    public Employee provision(String employeeId, String fullName, String rawPassword,
                              EmployeeRole role, String phone, String address) {
        if (role == null) {
            throw new DomainException("POSITION_REQUIRED", "Phải chọn chức vụ nhân viên");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainException("PASSWORD_REQUIRED", "Phải cung cấp mật khẩu mới");
        }
        if (employees.existsById(employeeId)) {
            throw new DomainException("EMPLOYEE_EXISTS", "Mã nhân viên đã tồn tại");
        }
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFullName(fullName);
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.setRole(role);
        employee.setPhone(phone);
        employee.setAddress(address);
        return employees.save(employee);
    }

    @Transactional(readOnly = true)
    public Employee findRequired(String employeeId) {
        return employees.findById(employeeId)
                .orElseThrow(() -> new DomainException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
    }

    @Transactional
    public Employee resetPassword(String employeeId, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainException("PASSWORD_REQUIRED", "Phải cung cấp mật khẩu mới");
        }
        Employee employee = employees.findById(employeeId)
                .orElseThrow(() -> new DomainException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.unlockAfterPasswordReset();
        return employees.save(employee);
    }

    @Transactional
    public void recordLoginFailure(String employeeId) {
        employees.findForUpdateByEmployeeId(employeeId).ifPresent(employee -> {
            employee.recordLoginFailure(Instant.now(), MAX_FAILED_LOGIN_ATTEMPTS);
            employees.save(employee);
            if (audit != null) audit.record(employeeId, "LOGIN_FAILED", "EMPLOYEE", employeeId, null,
                    String.valueOf(employee.getFailedLoginAttempts()), "Invalid credentials");
        });
    }

    @Transactional
    public void recordLoginSuccess(String employeeId) {
        employees.findForUpdateByEmployeeId(employeeId).ifPresent(employee -> {
            employee.recordLoginSuccess(Instant.now());
            employees.save(employee);
            if (audit != null) audit.record(employeeId, "LOGIN_SUCCEEDED", "EMPLOYEE", employeeId, null, null, null);
        });
    }
}
