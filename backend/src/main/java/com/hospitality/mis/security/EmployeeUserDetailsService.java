package com.hospitality.mis.security;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.identity.application.EmployeeService;
import com.hospitality.mis.identity.domain.Employee;
import com.hospitality.mis.identity.domain.EmployeeRole;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {
    private static final Pattern BCRYPT_HASH = Pattern.compile(
            "^\\$2[aby]\\$(0[4-9]|[12]\\d|3[01])\\$[./A-Za-z0-9]{53}$");

    private final EmployeeService employees;

    public EmployeeUserDetailsService(EmployeeService employees) {
        this.employees = employees;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee;
        try {
            employee = employees.findRequired(username);
        } catch (DomainException exception) {
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

    public static boolean isBcryptHash(String encodedPassword) {
        return encodedPassword != null && BCRYPT_HASH.matcher(encodedPassword).matches();
    }

    public static String roleFor(EmployeeRole role) {
        if (role == null) throw new IllegalArgumentException("Employee role is required");
        return role.name();
    }
}
