package com.hospitality.mis.identity;

import com.hospitality.mis.identity.adapter.EmployeeRepository;
import com.hospitality.mis.identity.application.EmployeeService;
import com.hospitality.mis.identity.domain.Employee;
import com.hospitality.mis.identity.domain.EmployeeRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    EmployeeRepository employees;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    EmployeeService service;

    @Test
    void provisionHashesPasswordAndReturnsIdentityContract() {
        when(employees.existsById("ID01")).thenReturn(false);
        when(passwordEncoder.encode("initial-password")).thenReturn("bcrypt-hash");
        when(employees.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = service.provision("ID01", "Identity Employee", "initial-password",
                EmployeeRole.MANAGER, "0909000001", "Office");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employees).save(captor.capture());
        assertThat(result.getEmployeeId()).isEqualTo("ID01");
        assertThat(captor.getValue().getPassword()).isEqualTo("bcrypt-hash");
        assertThat(captor.getValue().getRole().name()).isEqualTo("MANAGER");
    }

    @Test
    void duplicateEmployeeIsRejectedBeforePasswordHashing() {
        when(employees.existsById("ID01")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.hospitality.mis.common.exception.DomainException.class,
                () -> service.provision("ID01", "Duplicate", "password", EmployeeRole.FRONT_DESK,
                        "0909000001", null));

        verify(passwordEncoder, never()).encode(any());
        verify(employees, never()).save(any(Employee.class));
    }

    @Test
    void resetPasswordHashesNewValue() {
        Employee existing = new Employee();
        existing.setEmployeeId("ID01");
        existing.setPassword("old-hash");
        when(employees.findById("ID01")).thenReturn(java.util.Optional.of(existing));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(employees.save(existing)).thenReturn(existing);

        service.resetPassword("ID01", "new-password");

        assertThat(existing.getPassword()).isEqualTo("new-hash");
        verify(employees).save(existing);
    }

    @Test
    void fifthFailedLoginLocksAccountAndSuccessfulLoginResetsAuditCounter() {
        Employee existing = new Employee();
        existing.setEmployeeId("ID01");
        when(employees.findForUpdateByEmployeeId("ID01")).thenReturn(java.util.Optional.of(existing));

        for (int attempt = 0; attempt < 5; attempt++) service.recordLoginFailure("ID01");
        assertThat(existing.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(existing.isAccountNonLocked()).isFalse();

        service.recordLoginSuccess("ID01");
        assertThat(existing.getFailedLoginAttempts()).isZero();
        assertThat(existing.getLastLoginAt()).isNotNull();
    }
}
