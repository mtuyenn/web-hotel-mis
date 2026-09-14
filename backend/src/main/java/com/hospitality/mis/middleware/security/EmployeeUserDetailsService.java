package com.hospitality.mis.middleware.security;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.auth.CustomerAccountRepository;
import com.hospitality.mis.entity.auth.CustomerAccount;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.identity.EmployeeRole;
import com.hospitality.mis.service.identity.EmployeeService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
/** Cung cấp UserDetails chuẩn hóa cho cả employee và customer khi xác thực hoặc tái xác minh JWT. */
public class EmployeeUserDetailsService implements UserDetailsService {
    /** Mẫu hợp lệ của BCrypt; từ chối dữ liệu mật khẩu không có định dạng hash mong đợi. */
    private static final Pattern BCRYPT_HASH = Pattern.compile(
            "^\\$2[aby]\\$(0[4-9]|[12]\\d|3[01])\\$[./A-Za-z0-9]{53}$");

    /** Nguồn dữ liệu employee và trạng thái tài khoản/role của nhân viên. */
    private final EmployeeService employees;
    /** Nguồn dữ liệu tài khoản khách hàng, tách biệt khỏi định danh nhân viên. */
    private final CustomerAccountRepository customerAccounts;

    /** Nhận các nguồn dữ liệu chính thức để không fallback chéo giữa employee và customer. */
    public EmployeeUserDetailsService(EmployeeService employees, CustomerAccountRepository customerAccounts) {
        this.employees = employees;
        this.customerAccounts = customerAccounts;
    }

    /** Định danh đăng nhập chính thức của nhân viên là employee_id. */
    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        return loadEmployeeById(employeeId);
    }

    /** Nạp employee theo định danh chính thức, kiểm tra hash và phản ánh lock/enable vào UserDetails. */
    public UserDetails loadEmployeeById(String employeeId) throws UsernameNotFoundException {
        Employee employee;
        try {
            employee = employees.findRequired(employeeId);
        } catch (DomainException exception) {
            // Cố ý không tìm kiếm khách hàng sau khi không tìm thấy nhân viên.
            throw new UsernameNotFoundException("Không tìm thấy nhân viên", exception);
        }
        String encodedPassword = employee.getPassword();
        if (!isBcryptHash(encodedPassword)) {
            throw new UsernameNotFoundException("Thông tin xác thực không hợp lệ");
        }
        var authorities = Stream.concat(
                        Stream.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())),
                        employee.getPermissions().stream()
                                .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission.name())))
                .toList();
        // accountLocked/disabled phải phản ánh trạng thái hiện tại để token cũ không giữ quyền sau khi khóa.
        return User.withUsername(employee.getEmployeeId())
                .password(encodedPassword)
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!employee.isAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!employee.isEnabled())
                .build();
    }

    /** Nạp customer theo ID số; không dùng employee lookup vì hai loại principal có lifecycle riêng. */
    public UserDetails loadCustomerById(Long customerAccountId) throws UsernameNotFoundException {
        if (customerAccountId == null) throw new UsernameNotFoundException("Customer principal id is required");
        CustomerAccount customer = customerAccounts.findById(customerAccountId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản khách hàng"));
        if (!isBcryptHash(customer.getPassword())) {
            throw new UsernameNotFoundException("Thông tin xác thực không hợp lệ");
        }
        // Khóa hoặc vô hiệu hóa customer cũng phải làm JWT revalidation thất bại.
        return User.withUsername(String.valueOf(customer.getId()))
                .password(customer.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                .accountLocked(!customer.isAccountNonLocked())
                .disabled(!customer.isEnabled())
                .build();
    }

    /** Kiểm tra hình dạng BCrypt trước khi đưa hash vào Spring Security. */
    public static boolean isBcryptHash(String encodedPassword) {
        return encodedPassword != null && BCRYPT_HASH.matcher(encodedPassword).matches();
    }

    /** Chuẩn hóa tên role từ enum; role null là input không hợp lệ và bị từ chối ngay. */
    public static String roleFor(EmployeeRole role) {
        if (role == null) throw new IllegalArgumentException("Employee role is required");
        return role.name();
    }
}
