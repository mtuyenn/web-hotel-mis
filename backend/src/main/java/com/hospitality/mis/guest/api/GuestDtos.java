package com.hospitality.mis.guest.api;

import com.hospitality.mis.guest.domain.MembershipTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class GuestDtos {
    private GuestDtos() {}

    public record CreateRequest(@NotBlank @Size(max = 100) String fullName, Integer birthYear,
                                @NotBlank @Size(max = 12) String identityNumber,
                                @NotBlank @Size(max = 15) @Pattern(regexp = "[0-9+ .-]{8,15}") String phone,
                                @Email @Size(max = 100) String email,
                                @Size(max = 255) String address) {}

    public record Response(Long id, String fullName, Integer birthYear, String identityNumber, String phone,
                           String email, String address, MembershipTier membershipTier, BigDecimal totalSpend,
                           int lateCancellationCount, boolean bookingBlocked) {}
}
