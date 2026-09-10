package com.hospitality.mis.middleware.security;

import com.hospitality.mis.entity.identity.EmployeeRole;
import com.hospitality.mis.entity.identity.Permission;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("departmentAccess")
public class DepartmentAccess {
    public boolean allows(Authentication authentication, String capability) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return false;
        Permission permission = Permission.valueOf(capability);
        return authentication.getAuthorities().stream().anyMatch(authority -> {
            String name = authority.getAuthority();
            if (!name.startsWith("ROLE_")) return false;
            try {
                return EmployeeRole.valueOf(name.substring(5)).permissions().contains(permission);
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        });
    }
}
