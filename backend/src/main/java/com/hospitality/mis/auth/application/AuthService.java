package com.hospitality.mis.auth.application;

import com.hospitality.mis.auth.adapter.RefreshTokenRepository;
import com.hospitality.mis.auth.api.AuthDtos;
import com.hospitality.mis.auth.domain.RefreshToken;
import com.hospitality.mis.identity.application.EmployeeService;
import com.hospitality.mis.identity.domain.Employee;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmployeeService employeeService;
    private final RefreshTokenRepository refreshTokens;
    private final JwtTokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       EmployeeService employeeService,
                       RefreshTokenRepository refreshTokens,
                       JwtTokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.employeeService = employeeService;
        this.refreshTokens = refreshTokens;
        this.tokenService = tokenService;
    }

    @Transactional(noRollbackFor = AuthFailureException.class)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.employeeId(), request.password()));
        } catch (AuthenticationException exception) {
            employeeService.recordLoginFailure(request.employeeId());
            throw new AuthFailureException();
        }
        UserDetails user = (UserDetails) authentication.getPrincipal();
        employeeService.recordLoginSuccess(user.getUsername());
        return issueAndStore(user, tokenService.generateFamilyId());
    }

    @Transactional(noRollbackFor = AuthFailureException.class)
    public AuthDtos.TokenResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AuthFailureException();
        }
        Instant now = Instant.now();
        RefreshToken current = refreshTokens.findForUpdate(JwtTokenService.hash(rawRefreshToken))
                .orElseThrow(AuthFailureException::new);
        if (current.getRevokedAt() != null) {
            revokeFamily(current.getFamilyId(), now);
            throw new AuthFailureException();
        }
        if (!now.isBefore(current.getExpiresAt())) {
            current.revoke(now, null);
            refreshTokens.save(current);
            throw new AuthFailureException();
        }

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(current.getEmployeeId());
        } catch (AuthenticationException exception) {
            current.revoke(now, null);
            refreshTokens.save(current);
            throw new AuthFailureException();
        }
        JwtTokenService.IssuedTokens issued = tokenService.issue(
                user.getUsername(), user.getAuthorities(), current.getFamilyId());
        current.revoke(now, issued.refreshTokenHash());
        refreshTokens.save(current);
        refreshTokens.save(RefreshToken.issue(current.getEmployeeId(), issued.refreshTokenHash(),
                current.getFamilyId(), issued.issuedAt(), issued.refreshExpiresAt()));
        return issued.response();
    }

    @Transactional
    public void logout(String employeeId, String rawRefreshToken) {
        Instant now = Instant.now();
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            refreshTokens.revokeAllForEmployee(employeeId, now);
            return;
        }
        refreshTokens.findForUpdate(JwtTokenService.hash(rawRefreshToken)).ifPresent(token -> {
            if (employeeId.equals(token.getEmployeeId())) {
                revokeFamily(token.getFamilyId(), now);
            }
        });
    }

    @Transactional
    public AuthDtos.EmployeeResponse provision(AuthDtos.ProvisionRequest request) {
        Employee employee = employeeService.provision(request.employeeId(), request.fullName(), request.password(),
                request.role(), request.phone(), request.address());
        return toResponse(employee);
    }

    @Transactional
    public void resetPassword(String employeeId, AuthDtos.PasswordResetRequest request) {
        String password = request == null ? null : request.password();
        employeeService.resetPassword(employeeId, password);
        refreshTokens.revokeAllForEmployee(employeeId, Instant.now());
    }

    private AuthDtos.TokenResponse issueAndStore(UserDetails user, String familyId) {
        JwtTokenService.IssuedTokens issued = tokenService.issue(user.getUsername(), user.getAuthorities(), familyId);
        refreshTokens.save(RefreshToken.issue(user.getUsername(), issued.refreshTokenHash(), familyId,
                issued.issuedAt(), issued.refreshExpiresAt()));
        return issued.response();
    }

    private void revokeFamily(String familyId, Instant at) {
        refreshTokens.findAllByFamilyId(familyId).forEach(token -> {
            if (token.getRevokedAt() == null) {
                token.revoke(at, null);
            }
        });
        refreshTokens.flush();
    }

    private AuthDtos.EmployeeResponse toResponse(Employee employee) {
        return new AuthDtos.EmployeeResponse(employee.getEmployeeId(), employee.getFullName(), employee.getRole(),
                employee.getPhone(), employee.getAddress());
    }
}
