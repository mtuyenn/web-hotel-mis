package com.hospitality.mis.dto.billing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Hợp đồng callback tối thiểu, độc lập với provider cụ thể. */
public final class DepositPaymentWebhookDtos {
    private DepositPaymentWebhookDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Request(
            @NotBlank String providerEventId,
            @NotBlank String paymentCode,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String reference,
            @NotBlank String status) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(boolean accepted, Long reservationId, String reservationStatus,
                           String paymentStatus) {}
}
