package com.hospitality.mis.middleware.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/** Resolves the exact principal named by a signed access token. */
@Component
public class EmployeeJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final EmployeeUserDetailsService users;
    private final com.hospitality.mis.dao.auth.RefreshTokenRepository sessions;

    public EmployeeJwtAuthenticationConverter(EmployeeUserDetailsService users,
            com.hospitality.mis.dao.auth.RefreshTokenRepository sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String subject = jwt.getSubject();
        String principalType = jwt.getClaimAsString("principal_type");
        String principalId = jwt.getClaimAsString("principal_id");
        if (subject == null || subject.isBlank() || principalType == null || principalType.isBlank()
                || principalId == null || principalId.isBlank() || !subject.equals(principalId)) {
            throw new BadCredentialsException("JWT principal is required");
        }
        if (!"access".equals(jwt.getClaimAsString("token_type"))) {
            throw new BadCredentialsException("JWT token type is not valid");
        }
        String sessionId = jwt.getClaimAsString("session_id");
        if (sessionId == null || sessionId.isBlank()
                || sessions.findByFamilyIdAndRevokedAtIsNullAndExpiresAtAfter(sessionId, java.time.Instant.now())
                .stream().noneMatch(token -> principalType.equals(token.getPrincipalType())
                        && principalId.equals(token.getPrincipalId()))) {
            throw new BadCredentialsException("JWT session is not active");
        }

        UserDetails user;
        try {
            if ("EMPLOYEE".equals(principalType)) {
                user = users.loadEmployeeById(principalId);
            } else if ("CUSTOMER".equals(principalType)) {
                user = users.loadCustomerById(Long.valueOf(principalId));
            } else {
                throw new BadCredentialsException("JWT principal type is not valid");
            }
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException("JWT customer principal id is not valid", exception);
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw new BadCredentialsException("JWT principal is not valid", exception);
        }
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new BadCredentialsException("JWT account is not active");
        }
        return user.getAuthorities().stream().map(authority -> (GrantedAuthority) authority).toList();
    }
}
