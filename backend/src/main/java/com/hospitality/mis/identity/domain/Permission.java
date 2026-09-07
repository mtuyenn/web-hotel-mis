package com.hospitality.mis.identity.domain;

/**
 * Capabilities that can be granted by an identity role.
 *
 * <p>The current schema stores the employee's position, not a permission
 * table.  Permissions therefore remain a domain policy derived from the
 * position; adding this enum does not require a database migration.</p>
 */
public enum Permission {
    EMPLOYEE_READ,
    EMPLOYEE_PROVISION,
    EMPLOYEE_PASSWORD_RESET,
    ROOM_READ,
    ROOM_WRITE,
    GUEST_READ,
    GUEST_WRITE,
    RESERVATION_READ,
    RESERVATION_WRITE,
    BILLING_READ,
    BILLING_WRITE,
    SERVICE_READ,
    SERVICE_WRITE,
    APPROVAL_REQUEST,
    APPROVAL_APPROVE,
    AUDIT_READ
}
