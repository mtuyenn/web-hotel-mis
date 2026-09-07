package com.hospitality.mis.identity.domain;

import java.util.EnumSet;
import java.util.Set;

/** Canonical authorization role owned by identity. */
public enum EmployeeRole {
    ADMIN(Permission.values()),
    MANAGER(Permission.values()),
    FRONT_DESK(Permission.ROOM_READ, Permission.GUEST_READ, Permission.GUEST_WRITE,
            Permission.RESERVATION_READ, Permission.RESERVATION_WRITE, Permission.BILLING_READ,
            Permission.SERVICE_READ, Permission.APPROVAL_REQUEST),
    HOUSEKEEPING(Permission.ROOM_READ, Permission.RESERVATION_READ, Permission.SERVICE_READ),
    ACCOUNTING(Permission.EMPLOYEE_READ, Permission.BILLING_READ, Permission.BILLING_WRITE,
            Permission.SERVICE_READ, Permission.APPROVAL_REQUEST),
    TECHNICAL(Permission.ROOM_READ, Permission.ROOM_WRITE, Permission.RESERVATION_READ),
    KITCHEN(Permission.SERVICE_READ, Permission.SERVICE_WRITE),
    DIRECTOR(Permission.values()),
    STAFF(Permission.ROOM_READ, Permission.GUEST_READ, Permission.RESERVATION_READ);

    private final Set<Permission> permissions;
    EmployeeRole(Permission... permissions) {
        EnumSet<Permission> set = EnumSet.noneOf(Permission.class);
        for (Permission permission : permissions) set.add(permission);
        this.permissions = Set.copyOf(set);
    }
    public Set<Permission> permissions() { return permissions; }
}
