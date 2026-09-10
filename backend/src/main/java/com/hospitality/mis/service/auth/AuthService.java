package com.hospitality.mis.service.auth;

import com.hospitality.mis.dao.auth.CustomerAccountRepository;
import com.hospitality.mis.dao.auth.RefreshTokenRepository;
import com.hospitality.mis.dto.auth.AuthDtos;
import com.hospitality.mis.dto.auth.CustomerAccountDtos;
import com.hospitality.mis.entity.auth.CustomerAccount;
import com.hospitality.mis.entity.auth.RefreshToken;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.middleware.security.EmployeeUserDetailsService;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.identity.EmployeeService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final CustomerAccountRepository customerAccounts;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                       EmployeeService employeeService, RefreshTokenRepository refreshTokens,
                       JwtTokenService tokenService, CustomerAccountRepository customerAccounts,
                       PasswordEncoder passwordEncoder, AuditService audit) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.employeeService = employeeService;
        this.refreshTokens = refreshTokens;
        this.tokenService = tokenService;
        this.customerAccounts = customerAccounts;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    @Transactional(noRollbackFor = AuthFailureException.class)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.employeeId(), request.password()));
            UserDetails user = (UserDetails) authentication.getPrincipal();
            employeeService.recordLoginSuccess(user.getUsername());
            return issueAndStore(JwtTokenService.PrincipalType.EMPLOYEE, user.getUsername(), user,
                    tokenService.generateFamilyId());
        } catch (DisabledException exception) {
            throw new AuthFailureException("ACCOUNT_DISABLED");
        } catch (LockedException exception) {
            throw new AuthFailureException("ACCOUNT_LOCKED");
        } catch (AuthenticationException exception) {
            try {
                employeeService.recordLoginFailure(request.employeeId());
            } catch (RuntimeException ignored) {
                // Authentication failures must remain indistinguishable to the client.
            }
            throw new AuthFailureException();
        }
    }

    @Transactional(noRollbackFor = AuthFailureException.class)
    public AuthDtos.TokenResponse customerLogin(CustomerAccountDtos.LoginRequest request) {
        CustomerAccount account = customerAccounts.findByPhone(request.phone()).orElse(null);
        if (account == null || !EmployeeUserDetailsService.isBcryptHash(account.getPassword())
                || !passwordEncoder.matches(request.password(), account.getPassword())) {
            audit.record("SYSTEM", "LOGIN_FAILED", "CUSTOMER_ACCOUNT",
                    request.phone(), null, null, "Invalid credentials");
            throw new AuthFailureException();
        }
        if (!account.isEnabled()) {
            audit.record("customer:" + account.getId(), "LOGIN_FAILED", "CUSTOMER_ACCOUNT",
                    String.valueOf(account.getId()), null, null, "Account disabled");
            throw new AuthFailureException("ACCOUNT_DISABLED");
        }
        if (!account.isAccountNonLocked()) {
            audit.record("customer:" + account.getId(), "LOGIN_FAILED", "CUSTOMER_ACCOUNT",
                    String.valueOf(account.getId()), null, null, "Account locked");
            throw new AuthFailureException("ACCOUNT_LOCKED");
        }
        UserDetails user = User.withUsername(String.valueOf(account.getId())).password(account.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")).build();
        audit.record("customer:" + account.getId(), "LOGIN_SUCCEEDED", "CUSTOMER_ACCOUNT",
                String.valueOf(account.getId()), null, null, null);
        return issueAndStore(JwtTokenService.PrincipalType.CUSTOMER, String.valueOf(account.getId()), user,
                tokenService.generateFamilyId());
    }

    @Transactional(noRollbackFor = AuthFailureException.class)
    public AuthDtos.TokenResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) throw new AuthFailureException();
        Instant now = Instant.now();
        RefreshToken current = refreshTokens.findForUpdate(JwtTokenService.hash(rawRefreshToken))
                .orElseThrow(AuthFailureException::new);
        if (current.getRevokedAt() != null) {
            revokeFamily(current.getFamilyId(), now);
            audit.record(current.auditActor(), "REFRESH_REPLAY_DETECTED", "REFRESH_TOKEN",
                    current.getFamilyId(), null, null, "Revoked refresh token replay");
            throw new AuthFailureException();
        }
        if (!now.isBefore(current.getExpiresAt())) {
            current.revoke(now, null);
            refreshTokens.save(current);
            throw new AuthFailureException();
        }

        UserDetails user;
        JwtTokenService.PrincipalType type = current.principalType();
        try {
            if (type == JwtTokenService.PrincipalType.EMPLOYEE) {
                user = userDetailsService.loadUserByUsername(current.getPrincipalId());
            } else {
                CustomerAccount account = customerAccounts.findById(current.getCustomerAccountId())
                        .orElseThrow(AuthFailureException::new);
                user = customerUser(account);
            }
        } catch (DisabledException exception) {
            revokeCurrent(current, now);
            throw new AuthFailureException("ACCOUNT_DISABLED");
        } catch (LockedException exception) {
            revokeCurrent(current, now);
            throw new AuthFailureException("ACCOUNT_LOCKED");
        } catch (AuthenticationException exception) {
            revokeCurrent(current, now);
            throw new AuthFailureException();
        }
        if (!user.isEnabled()) {
            revokeCurrent(current, now);
            throw new AuthFailureException("ACCOUNT_DISABLED");
        }
        if (!user.isAccountNonLocked()) {
            revokeCurrent(current, now);
            throw new AuthFailureException("ACCOUNT_LOCKED");
        }

        String principalId = current.getPrincipalId();
        JwtTokenService.IssuedTokens issued = tokenService.issue(type, principalId, user.getAuthorities(),
                current.getFamilyId());
        current.revoke(now, issued.refreshTokenHash());
        refreshTokens.save(current);
        saveRefreshToken(type, principalId, issued, current.getFamilyId());
        audit.record(current.auditActor(), "REFRESH_ROTATED", "REFRESH_TOKEN", current.getFamilyId(),
                null, issued.refreshTokenHash(), null);
        return issued.response();
    }

    @Transactional
    public void logout(SecurityActor.Principal actor, String rawRefreshToken) {
        Instant now = Instant.now();
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            if (actor.isEmployee()) refreshTokens.revokeAllForEmployee(actor.id(), now);
            else if (actor.isCustomer()) refreshTokens.revokeAllForCustomer(Long.valueOf(actor.id()), now);
            else throw new AuthFailureException();
            audit.record(actor.isCustomer() ? "customer:" + actor.id() : actor.id(), "LOGOUT", "REFRESH_TOKEN", actor.id(), null, null, null);
            return;
        }
        refreshTokens.findForUpdate(JwtTokenService.hash(rawRefreshToken)).ifPresent(token -> {
            if (actor.type().equals(token.getPrincipalType()) && actor.id().equals(token.getPrincipalId())) {
                revokeFamily(token.getFamilyId(), now);
                audit.record(actor.isCustomer() ? "customer:" + actor.id() : actor.id(), "LOGOUT", "REFRESH_TOKEN", token.getFamilyId(), null, null, null);
            }
        });
    }

    @Transactional
    public AuthDtos.EmployeeResponse provision(AuthDtos.ProvisionRequest request) {
        Employee employee = employeeService.provision(request.employeeId(), request.fullName(), request.password(),
                request.role(), request.phone(), request.address());
        audit.record(SecurityActor.currentActor(), "EMPLOYEE_PROVISIONED", "EMPLOYEE", employee.getEmployeeId(),
                null, employee.getRole().name(), null);
        return new AuthDtos.EmployeeResponse(employee.getEmployeeId(), employee.getFullName(), employee.getRole(),
                employee.getPhone(), employee.getAddress());
    }

    @Transactional
    public void resetPassword(String employeeId, AuthDtos.PasswordResetRequest request,
                              SecurityActor.Principal actor) {
        if (!actor.isEmployee()) throw new AuthFailureException();
        Employee employee = employeeService.resetPassword(employeeId, request == null ? null : request.password());
        refreshTokens.revokeAllForEmployee(employeeId, Instant.now());
        audit.record(actor.id(), "PASSWORD_RESET", "EMPLOYEE", employee.getEmployeeId(), null, null, null);
    }

    @Transactional
    public void resetCustomerPassword(SecurityActor.Principal actor,
                                      CustomerAccountDtos.PasswordResetRequest request) {
        if (!actor.isCustomer()) throw new AuthFailureException();
        CustomerAccount account = customerAccounts.findById(Long.valueOf(actor.id()))
                .orElseThrow(AuthFailureException::new);
        if (request == null || request.password() == null || request.password().length() < 8
                || request.password().length() > 72) {
            throw new AuthFailureException("PASSWORD_INVALID");
        }
        account.setPassword(passwordEncoder.encode(request.password()));
        customerAccounts.save(account);
        refreshTokens.revokeAllForCustomer(account.getId(), Instant.now());
        audit.record("customer:" + actor.id(), "PASSWORD_RESET", "CUSTOMER_ACCOUNT", String.valueOf(account.getId()),
                null, null, null);
    }

    private AuthDtos.TokenResponse issueAndStore(JwtTokenService.PrincipalType type, String principalId,
                                                 UserDetails user, String familyId) {
        JwtTokenService.IssuedTokens issued = tokenService.issue(type, principalId, user.getAuthorities(), familyId);
        saveRefreshToken(type, principalId, issued, familyId);
        return issued.response();
    }

    private void saveRefreshToken(JwtTokenService.PrincipalType type, String principalId,
                                  JwtTokenService.IssuedTokens issued, String familyId) {
        if (type == JwtTokenService.PrincipalType.CUSTOMER) {
            refreshTokens.save(RefreshToken.issueCustomer(Long.valueOf(principalId), issued.refreshTokenHash(),
                    familyId, issued.issuedAt(), issued.refreshExpiresAt()));
        } else {
            refreshTokens.save(RefreshToken.issue(principalId, issued.refreshTokenHash(), familyId,
                    issued.issuedAt(), issued.refreshExpiresAt()));
        }
    }

    private UserDetails customerUser(CustomerAccount account) {
        return User.withUsername(String.valueOf(account.getId())).password(account.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                .accountLocked(!account.isAccountNonLocked()).disabled(!account.isEnabled()).build();
    }

    private void revokeCurrent(RefreshToken token, Instant now) {
        token.revoke(now, null);
        refreshTokens.save(token);
    }

    private void revokeFamily(String familyId, Instant at) {
        refreshTokens.findAllByFamilyId(familyId).forEach(token -> {
            if (token.getRevokedAt() == null) token.revoke(at, null);
        });
        refreshTokens.flush();
    }

}
