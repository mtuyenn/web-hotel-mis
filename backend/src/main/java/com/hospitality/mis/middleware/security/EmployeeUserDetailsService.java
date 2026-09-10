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
public class EmployeeUserDetailsService implements UserDetailsService {
    private static final Pattern BCRYPT_HASH = Pattern.compile(
            "^\\$2[aby]\\$(0[4-9]|[12]\\d|3[01])\\$[./A-Za-z0-9]{53}$");

    private final EmployeeService employees;
    private final CustomerAccountRepository customerAccounts;

    public EmployeeUserDetailsService(EmployeeService employees, CustomerAccountRepository customerAccounts) {
        this.employees = employees;
        this.customerAccounts = customerAccounts;
    }

    /** The canonical employee login identifier is employee_id. */
    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        return loadEmployeeById(employeeId);
    }

    public UserDetails loadEmployeeById(String employeeId) throws UsernameNotFoundException {
        Employee employee;
        try {
            employee = employees.findRequired(employeeId);
        } catch (DomainException exception) {
            // Deliberately do not resolve a customer after an employee miss.
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
        return User.withUsername(employee.getEmployeeId())
                .password(encodedPassword)
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!employee.isAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!employee.isEnabled())
                .build();
    }

    public UserDetails loadCustomerById(Long customerAccountId) throws UsernameNotFoundException {
        if (customerAccountId == null) throw new UsernameNotFoundException("Customer principal id is required");
        CustomerAccount customer = customerAccounts.findById(customerAccountId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản khách hàng"));
        if (!isBcryptHash(customer.getPassword())) {
            throw new UsernameNotFoundException("Thông tin xác thực không hợp lệ");
        }
        return User.withUsername(String.valueOf(customer.getId()))
                .password(customer.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                .accountLocked(!customer.isAccountNonLocked())
                .disabled(!customer.isEnabled())
                .build();
    }

    public static boolean isBcryptHash(String encodedPassword) {
        return encodedPassword != null && BCRYPT_HASH.matcher(encodedPassword).matches();
    }

    public static String roleFor(EmployeeRole role) {
        if (role == null) throw new IllegalArgumentException("Employee role is required");
        return role.name();
    }
}
