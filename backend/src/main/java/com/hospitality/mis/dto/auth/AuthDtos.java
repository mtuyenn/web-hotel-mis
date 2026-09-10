package com.hospitality.mis.dto.auth;



import com.hospitality.mis.entity.identity.EmployeeRole;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



public final class AuthDtos {

    private AuthDtos() {

    }



    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record LoginRequest(@NotBlank String employeeId, @NotBlank String password) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record LogoutRequest(String refreshToken) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record TokenResponse(String accessToken, String refreshToken, String tokenType,
                                long expiresIn, long refreshExpiresIn) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ProvisionRequest(@NotBlank @Size(max = 10) String employeeId,
                                   @NotBlank String fullName,
                                   @NotBlank @Size(min = 8, max = 72) String password,
                                   @NotNull EmployeeRole role,
                                   @NotBlank String phone,
                                   String address) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record PasswordResetRequest(@NotBlank @Size(min = 8, max = 72) String password) { }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record EmployeeResponse(String employeeId, String fullName, EmployeeRole role,
                                   String phone, String address) {
    }

}
