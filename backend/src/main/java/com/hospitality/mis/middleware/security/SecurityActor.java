package com.hospitality.mis.middleware.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Access to the authenticated actor; request headers and body values are never actors. */
public final class SecurityActor {
    private SecurityActor() {
    }

    public static Principal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Yêu cầu phải được xác thực");
        }
        if (authentication instanceof JwtAuthenticationToken jwt) {
            String type = jwt.getToken().getClaimAsString("principal_type");
            String id = jwt.getToken().getClaimAsString("principal_id");
            if (type == null || id == null || id.isBlank() || !id.equals(authentication.getName())) {
                throw new AuthenticationCredentialsNotFoundException("JWT principal không hợp lệ");
            }
            return new Principal(type, id);
        }
        return new Principal("EMPLOYEE", authentication.getName());
    }

    public static String currentActor() {
        return currentPrincipal().id();
    }

    public static String requireBoundActor(String actor) {
        String current = currentActor();
        if (actor == null || actor.isBlank() || !current.equals(actor)) {
            throw new AccessDeniedException("Actor không khớp principal hiện tại");
        }
        return current;
    }

    public record Principal(String type, String id) {
        public Principal {
            if (type == null || type.isBlank() || id == null || id.isBlank()) {
                throw new IllegalArgumentException("Principal type and id are required");
            }
        }

        public boolean isEmployee() {
            return "EMPLOYEE".equals(type);
        }

        public boolean isCustomer() {
            return "CUSTOMER".equals(type);
        }
    }
}
