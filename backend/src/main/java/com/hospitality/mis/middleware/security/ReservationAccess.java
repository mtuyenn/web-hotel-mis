package com.hospitality.mis.middleware.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/** Operational authority follows the current shift actor, not the booking creator. */
public final class ReservationAccess {
    private ReservationAccess() {}

    public static void requireOperator(String actor) {
        SecurityActor.requireBoundActor(actor);
        if (!hasRole("FRONT_DESK", "MANAGER")) {
            throw new AccessDeniedException("Chỉ lễ tân hoặc quản lý được thay đổi đặt phòng");
        }
    }

    public static boolean hasGlobalRead() {
        SecurityActor.currentActor();
        return hasRole("FRONT_DESK", "MANAGER", "ADMIN", "DIRECTOR");
    }

    private static boolean hasRole(String... roles) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> java.util.Arrays.stream(roles)
                        .anyMatch(role -> authority.getAuthority().equals("ROLE_" + role)));
    }
}
