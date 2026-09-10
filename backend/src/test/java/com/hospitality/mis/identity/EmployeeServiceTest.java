package com.hospitality.mis.identity;



import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.dao.auth.CustomerAccountRepository;

import com.hospitality.mis.service.identity.EmployeeService;

import com.hospitality.mis.entity.identity.Employee;

import com.hospitality.mis.entity.identity.EmployeeRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;



import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;

import static org.mockito.Mockito.times;

import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)

class EmployeeServiceTest {

    @BeforeEach
    void authenticateAsDirector() {
        authenticateAs(EmployeeRole.DIRECTOR);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Mock

    EmployeeRepository employees;

    @Mock
    CustomerAccountRepository customerAccounts;



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
    void employeeCannotReuseCustomerPhone() {
        when(employees.existsById("ID02")).thenReturn(false);
        when(employees.existsByPhone("0909000002")).thenReturn(false);
        when(customerAccounts.existsByPhone("0909000002")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.hospitality.mis.common.exception.DomainException.class,
                () -> service.provision("ID02", "Duplicate phone", "password", EmployeeRole.FRONT_DESK,
                        "0909000002", null));

        verify(employees, never()).save(any(Employee.class));
    }



    @Test

    void resetPasswordHashesNewValue() {
        Employee existing = new Employee();
        existing.setEmployeeId("ID01");
        existing.setPassword("old-hash");
        existing.setRole(EmployeeRole.MANAGER);

        when(employees.findById("ID01")).thenReturn(java.util.Optional.of(existing));

        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        when(employees.save(existing)).thenReturn(existing);



        service.resetPassword("ID01", "new-password");



        assertThat(existing.getPassword()).isEqualTo("new-hash");

        verify(employees).save(existing);
    }

    @Test
    void managerCannotProvisionOrResetHigherRole() {
        authenticateAs(EmployeeRole.MANAGER);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.hospitality.mis.common.exception.DomainException.class,
                () -> service.provision("ID03", "Admin", "valid-password", EmployeeRole.ADMIN,
                        "0909000003", null));

        Employee director = new Employee();
        director.setEmployeeId("DIRECTOR");
        director.setRole(EmployeeRole.DIRECTOR);
        when(employees.findById("DIRECTOR")).thenReturn(java.util.Optional.of(director));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.hospitality.mis.common.exception.DomainException.class,
                () -> service.resetPassword("DIRECTOR", "valid-password"));

        verify(employees, never()).save(any(Employee.class));
    }

    @Test
    void provisioningCannotTargetCurrentActorAsARoleChange() {
        when(employees.existsById("actor")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.hospitality.mis.common.exception.DomainException.class,
                () -> service.provision("actor", "Actor", "valid-password", EmployeeRole.ADMIN,
                        "0909000005", null));

        verify(employees, never()).save(any(Employee.class));
    }

    @Test
    void managerCanProvisionAndResetHr() {
        authenticateAs(EmployeeRole.MANAGER);
        when(employees.existsById("HR01")).thenReturn(false);
        when(passwordEncoder.encode("valid-password")).thenReturn("bcrypt-hash");
        when(employees.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee provisioned = service.provision("HR01", "HR", "valid-password", EmployeeRole.HR,
                "0909000004", null);

        assertThat(provisioned.getRole()).isEqualTo(EmployeeRole.HR);

        when(employees.findById("HR01")).thenReturn(java.util.Optional.of(provisioned));
        when(employees.save(provisioned)).thenReturn(provisioned);
        service.resetPassword("HR01", "valid-password");
        verify(employees, times(2)).save(provisioned);
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

    private void authenticateAs(EmployeeRole role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("actor", "test",
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }
}
