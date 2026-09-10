package com.hospitality.mis.controller.auth;
import com.hospitality.mis.dto.auth.AuthDtos;




import com.hospitality.mis.service.auth.AuthService;

import com.hospitality.mis.middleware.security.SecurityActor;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.bind.annotation.RestController;



@RestController

@RequestMapping("/api/auth")

public class AuthController {

    private final AuthService service;



    public AuthController(AuthService service) {

        this.service = service;

    }



    @PostMapping("/login")

    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {

        return service.login(request);

    }

    @PostMapping("/customers/login")
    public AuthDtos.TokenResponse customerLogin(
            @Valid @RequestBody com.hospitality.mis.dto.auth.CustomerAccountDtos.LoginRequest request) {
        return service.customerLogin(request);
    }



    @PostMapping("/refresh")

    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {

        return service.refresh(request.refreshToken());

    }



    @PreAuthorize("isAuthenticated()")

    @PostMapping("/logout")

    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void logout(@RequestBody(required = false) AuthDtos.LogoutRequest request) {

        service.logout(SecurityActor.currentPrincipal(), request == null ? null : request.refreshToken());

    }




    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)

    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_PROVISION') && @employeeService.canManageRole(authentication, #request.role())")
    public AuthDtos.EmployeeResponse provision(@Valid @RequestBody AuthDtos.ProvisionRequest request) {

        return service.provision(request);

    }




    @PostMapping("/employees/{employeeId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    @PreAuthorize("@departmentAccess.allows(authentication, 'EMPLOYEE_PASSWORD_RESET') && @employeeService.canResetEmployee(authentication, #employeeId)")
    public void resetPassword(@PathVariable String employeeId,
                              @Valid @RequestBody AuthDtos.PasswordResetRequest request) {
        service.resetPassword(employeeId, request, SecurityActor.currentPrincipal());

    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/customers/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetCustomerPassword(
            @Valid @RequestBody com.hospitality.mis.dto.auth.CustomerAccountDtos.PasswordResetRequest request) {
        service.resetCustomerPassword(SecurityActor.currentPrincipal(), request);
    }

}
