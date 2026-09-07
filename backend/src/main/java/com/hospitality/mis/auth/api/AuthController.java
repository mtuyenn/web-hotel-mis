package com.hospitality.mis.auth.api;

import com.hospitality.mis.auth.application.AuthService;
import com.hospitality.mis.security.SecurityActor;
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

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return service.refresh(request.refreshToken());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) AuthDtos.LogoutRequest request) {
        service.logout(SecurityActor.currentActor(), request == null ? null : request.refreshToken());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.EmployeeResponse provision(@Valid @RequestBody AuthDtos.ProvisionRequest request) {
        return service.provision(request);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/employees/{employeeId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable String employeeId,
                              @Valid @RequestBody AuthDtos.PasswordResetRequest request) {
        service.resetPassword(employeeId, request);
    }
}
