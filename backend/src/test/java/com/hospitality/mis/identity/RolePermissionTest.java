package com.hospitality.mis.identity;

import com.hospitality.mis.identity.domain.EmployeeRole;
import com.hospitality.mis.identity.domain.Permission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolePermissionTest {
    @Test
    void canonicalRolesExposeTheirPermissionPolicy() {
        assertThat(EmployeeRole.MANAGER.permissions())
                .contains(Permission.EMPLOYEE_PROVISION, Permission.EMPLOYEE_PASSWORD_RESET,
                        Permission.APPROVAL_APPROVE);
        assertThat(EmployeeRole.FRONT_DESK.permissions())
                .contains(Permission.RESERVATION_WRITE)
                .doesNotContain(Permission.EMPLOYEE_PROVISION, Permission.APPROVAL_APPROVE);
        assertThat(EmployeeRole.STAFF.permissions()).contains(Permission.RESERVATION_READ);
    }
}
