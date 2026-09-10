package com.hospitality.mis.entity.identity;

import java.util.EnumSet;
import java.util.Set;
import static com.hospitality.mis.entity.identity.Permission.*;

/** Single capability matrix used by JWT authorities and endpoint authorization. */
public enum EmployeeRole {
    ADMIN(Permission.values()),
    DIRECTOR(Permission.values()),
    MANAGER(Permission.values()),
    FRONT_DESK(ROOM_READ, ROOM_WRITE, EQUIPMENT_READ, GUEST_READ, GUEST_WRITE,
            RESERVATION_READ, RESERVATION_CREATE, RESERVATION_WRITE, RESERVATION_CHECKOUT,
            RESERVATION_SERVICE_WRITE, INCIDENT_WRITE, BILLING_READ, PAYMENT_WRITE,
            SERVICE_READ, INVENTORY_READ, INVENTORY_WRITE, MAINTENANCE_READ, APPROVAL_REQUEST),
    ACCOUNTING(EMPLOYEE_READ, RESERVATION_READ,
            BILLING_READ, BILLING_WRITE, PAYMENT_WRITE, SERVICE_READ, SERVICE_WRITE,
            INVENTORY_READ, INVENTORY_WRITE, FINANCE_READ, FINANCE_WRITE, APPROVAL_REQUEST, AUDIT_READ),
    HOUSEKEEPING(ROOM_READ, ROOM_WRITE, EQUIPMENT_READ, RESERVATION_READ,
            INCIDENT_WRITE, SERVICE_READ, INVENTORY_READ,
            INVENTORY_WRITE, MAINTENANCE_READ, MAINTENANCE_WRITE),
    TECHNICAL(ROOM_READ, ROOM_WRITE, EQUIPMENT_READ, EQUIPMENT_WRITE, RESERVATION_READ,
            MAINTENANCE_READ, MAINTENANCE_WRITE),
    KITCHEN(SERVICE_READ, SERVICE_WRITE, INVENTORY_READ, INVENTORY_WRITE),
    STAFF(ROOM_READ, RESERVATION_READ),
    HR(EMPLOYEE_READ);

    private final Set<Permission> permissions;
    EmployeeRole(Permission... permissions) {
        EnumSet<Permission> grants = EnumSet.noneOf(Permission.class);
        for (Permission permission : permissions) grants.add(permission);
        if (name().equals("ADMIN") || name().equals("DIRECTOR")) {
            grants.removeAll(EnumSet.of(RESERVATION_CREATE, RESERVATION_WRITE, RESERVATION_CHECKOUT, RESERVATION_SERVICE_WRITE));
        }
        this.permissions = Set.copyOf(grants);
    }
    public Set<Permission> permissions() { return permissions; }

    public boolean canManage(EmployeeRole targetRole) {
        if (targetRole == null) return false;
        return switch (this) {
            case DIRECTOR -> true;
            case ADMIN -> targetRole != DIRECTOR;
            case MANAGER -> switch (targetRole) {
                case HR, FRONT_DESK, HOUSEKEEPING, TECHNICAL, KITCHEN, ACCOUNTING, STAFF -> true;
                default -> false;
            };
            default -> false;
        };
    }
}
