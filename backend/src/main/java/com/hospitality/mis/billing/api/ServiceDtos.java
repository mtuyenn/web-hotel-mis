package com.hospitality.mis.billing.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public final class ServiceDtos {
    private ServiceDtos() {}
    public record CreateRequest(@NotBlank String id, @NotBlank String name, @NotNull @PositiveOrZero BigDecimal price,
                                String unit, @PositiveOrZero int openingStock, @PositiveOrZero int safetyThreshold) {}
    public record StockRequest(@NotNull @PositiveOrZero Integer quantity) {}
    public record Response(String id, String name, BigDecimal price, String unit, int stock, int safetyThreshold,
                           boolean lowStock) {}
}
