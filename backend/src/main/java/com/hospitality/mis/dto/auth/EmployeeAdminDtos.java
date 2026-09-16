package com.hospitality.mis.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.identity.EmployeeRole;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class EmployeeAdminDtos {
    private EmployeeAdminDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(String employeeId, String fullName, String phone, String address, EmployeeRole role,
                           boolean enabled, boolean accountNonLocked, int failedLoginAttempts,
                           Instant lastLoginAt, Instant lastFailedLoginAt) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record StatusRequest(@NotNull Boolean enabled) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record RoleRequest(@NotNull EmployeeRole role) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record SessionResponse(Long id, String employeeId, Instant issuedAt, Instant expiresAt, Instant revokedAt, String familyId) {}
}
