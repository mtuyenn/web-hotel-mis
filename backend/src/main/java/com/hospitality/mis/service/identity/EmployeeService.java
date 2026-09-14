package com.hospitality.mis.service.identity;





import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.common.validation.PhoneNumberNormalizer;

import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.dao.auth.CustomerAccountRepository;

import com.hospitality.mis.entity.identity.Employee;

import com.hospitality.mis.entity.identity.EmployeeRole;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


/** Các ca sử dụng của ứng dụng thuộc ranh giới danh tính. */

@Service

public class EmployeeService {
    /** Ngưỡng khóa tài khoản sau số lần đăng nhập thất bại liên tiếp. */
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    /** Độ dài mật khẩu tối thiểu theo hợp đồng xác thực. */
    private static final int MIN_PASSWORD_LENGTH = 8;
    /** Độ dài tối đa tương thích với BCrypt. */
    private static final int MAX_PASSWORD_LENGTH = 72;
    /** Kho nhân viên, gồm các truy vấn có khóa cho bộ đếm đăng nhập. */
    private final EmployeeRepository employees;
    /** Kho tài khoản khách, dùng để kiểm tra trùng số điện thoại. */
    private final CustomerAccountRepository customerAccounts;

    /** Mã hóa mật khẩu trước khi lưu. */
    private final PasswordEncoder passwordEncoder;
    /** Ghi audit đăng nhập nếu được cấu hình. */
    private final AuditService audit;

    public EmployeeService(EmployeeRepository employees, CustomerAccountRepository customerAccounts,
                           PasswordEncoder passwordEncoder, AuditService audit) {
        this.employees = employees;
        this.customerAccounts = customerAccounts;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }



    @Transactional

    /** Tạo nhân viên sau khi kiểm tra quyền, mật khẩu và số điện thoại duy nhất. */
    public Employee provision(String employeeId, String fullName, String rawPassword,

                              EmployeeRole role, String phone, String address) {
        if (role == null) {
            throw new DomainException("POSITION_REQUIRED", "Phải chọn chức vụ nhân viên");

        }

        requireCurrentActorCanManage(role);

        if (!validPassword(rawPassword)) {
            throw new DomainException("PASSWORD_REQUIRED", "Phải cung cấp mật khẩu mới");
        }

        if (employees.existsById(employeeId)) {

            throw new DomainException("EMPLOYEE_EXISTS", "Mã nhân viên đã tồn tại");

        }

        String normalizedPhone = PhoneNumberNormalizer.normalize(phone);
        if (employees.existsByPhone(normalizedPhone) || customerAccounts.existsByPhone(normalizedPhone)) {
            throw new DomainException("PHONE_ALREADY_IN_USE", "Số điện thoại đã được sử dụng");
        }

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFullName(fullName);
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.setRole(role);
        employee.setPhone(normalizedPhone);
        employee.setAddress(address);
        return employees.save(employee);

    }



    @Transactional(readOnly = true)

    /** Tải nhân viên bắt buộc, dùng cho các luồng cần entity hiện hữu. */
    public Employee findRequired(String employeeId) {

        return employees.findById(employeeId)

                .orElseThrow(() -> new DomainException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));

    }



    @Transactional

    /** Đặt lại mật khẩu và mở khóa tài khoản theo quyền quản lý chức vụ. */
    public Employee resetPassword(String employeeId, String rawPassword) {
        Employee employee = employees.findById(employeeId)
                .orElseThrow(() -> new DomainException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));

        requireCurrentActorCanManage(employee.getRole());

        if (!validPassword(rawPassword)) {
            throw new DomainException("PASSWORD_REQUIRED", "Phải cung cấp mật khẩu mới");
        }

        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.unlockAfterPasswordReset();
        return employees.save(employee);
    }

    @Transactional
    /** Khóa bản ghi nhân viên, tăng bộ đếm lỗi và khóa khi đạt ngưỡng. */
    public void recordLoginFailure(String employeeId) {
        employees.findForUpdateByEmployeeId(employeeId).ifPresent(employee -> {
            employee.recordLoginFailure(Instant.now(), MAX_FAILED_LOGIN_ATTEMPTS);
            employees.save(employee);
            if (audit != null) audit.record(employeeId, "LOGIN_FAILED", "EMPLOYEE", employeeId, null,
                    String.valueOf(employee.getFailedLoginAttempts()), "Invalid credentials");
        });
    }

    @Transactional
    /** Khóa bản ghi nhân viên, xóa bộ đếm lỗi và ghi nhận đăng nhập thành công. */
    public void recordLoginSuccess(String employeeId) {
        employees.findForUpdateByEmployeeId(employeeId).ifPresent(employee -> {
            employee.recordLoginSuccess(Instant.now());
            employees.save(employee);
            if (audit != null) audit.record(employeeId, "LOGIN_SUCCEEDED", "EMPLOYEE", employeeId, null, null, null);
        });
    }

    /** Kiểm tra mật khẩu không trắng và nằm trong biên độ hệ thống. */
    private boolean validPassword(String password) {
        return password != null && !password.isBlank()
                && password.length() >= MIN_PASSWORD_LENGTH
                && password.length() <= MAX_PASSWORD_LENGTH;
    }

    /** Xác định authentication có được quản lý chức vụ mục tiêu không. */
    public boolean canManageRole(Authentication authentication, EmployeeRole targetRole) {
        EmployeeRole actorRole = roleOf(authentication);
        return actorRole != null && actorRole.canManage(targetRole);
    }

    /** Kiểm tra quyền reset theo chức vụ của nhân viên mục tiêu. */
    public boolean canResetEmployee(Authentication authentication, String employeeId) {
        Employee employee = employees.findById(employeeId).orElse(null);
        return employee == null || canManageRole(authentication, employee.getRole());
    }

    /** Bắt buộc actor đã xác thực có quyền quản lý chức vụ mục tiêu. */
    private void requireCurrentActorCanManage(EmployeeRole targetRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!canManageRole(authentication, targetRole)) {
            throw new DomainException("ACCESS_DENIED", "Không được quản lý chức vụ nhân viên này");
        }
        SecurityActor.currentPrincipal();
    }

    /** Ánh xạ authority ROLE_* của authentication sang enum chức vụ. */
    private EmployeeRole roleOf(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .map(this::parseRole)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /** Phân tích tên role, trả null cho authority không thuộc enum nhân viên. */
    private EmployeeRole parseRole(String role) {
        try {
            return EmployeeRole.valueOf(role);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
