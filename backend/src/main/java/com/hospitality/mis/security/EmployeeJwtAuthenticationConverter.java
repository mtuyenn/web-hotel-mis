package com.hospitality.mis.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Re-resolves the employee for each access token so authorities and account
 * status come from the identity store, rather than from an untrusted claim.
 */
@Component
public class EmployeeJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final UserDetailsService userDetailsService;

    public EmployeeJwtAuthenticationConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException("JWT subject is required");
        }
        String tokenType = jwt.getClaimAsString("token_type");
        if (tokenType != null && !"access".equals(tokenType)) {
            throw new BadCredentialsException("JWT token type is not valid");
        }
        try {
            UserDetails user = userDetailsService.loadUserByUsername(subject);
            return user.getAuthorities().stream().map(authority -> (GrantedAuthority) authority).toList();
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw new BadCredentialsException("JWT subject is not valid", exception);
        }
    }
}
