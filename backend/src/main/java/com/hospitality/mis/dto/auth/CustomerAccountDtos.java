package com.hospitality.mis.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import com.hospitality.mis.dto.guest.GuestDtos;

public final class CustomerAccountDtos {
    private CustomerAccountDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long guestId, String phone, boolean enabled, boolean accountNonLocked) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record MeResponse(Response account, GuestDtos.Response guest) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record LoginRequest(@NotBlank String phone, @NotBlank String password) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record RegisterRequest(@NotBlank String phone, @NotBlank String password,
                                  @NotBlank String fullName, @NotBlank String identityNumber) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record PasswordResetRequest(@NotBlank String password) {}
}
