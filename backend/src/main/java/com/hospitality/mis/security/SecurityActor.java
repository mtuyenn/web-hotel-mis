package com.hospitality.mis.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityActor {
    private SecurityActor() {}

    public static String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Yêu cầu phải được xác thực");
        }
        return authentication.getName();
    }

    /** Rejects caller-supplied actor values that are not bound to the principal. */
    public static String requireBoundActor(String actor) {
        String current = currentActor();
        if (actor == null || actor.isBlank() || !current.equals(actor)) {
            throw new IllegalArgumentException("Actor không khớp principal hiện tại");
        }
        return current;
    }
}
