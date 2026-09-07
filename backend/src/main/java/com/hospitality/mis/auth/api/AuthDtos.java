package com.hospitality.mis.auth.api;

import com.hospitality.mis.identity.domain.EmployeeRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(@NotBlank String employeeId, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record LogoutRequest(String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType,
                                long expiresIn, long refreshExpiresIn) {
    }

    public record ProvisionRequest(@NotBlank @Size(max = 10) String employeeId,
                                   @NotBlank String fullName,
                                   @NotBlank String password,
                                   @NotNull EmployeeRole role,
                                   @NotBlank String phone,
                                   String address) {
    }

    public record PasswordResetRequest(@NotBlank String password) { }

    public record EmployeeResponse(String employeeId, String fullName, EmployeeRole role,
                                   String phone, String address) {
    }
}
