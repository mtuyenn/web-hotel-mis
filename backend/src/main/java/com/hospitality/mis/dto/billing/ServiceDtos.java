package com.hospitality.mis.dto.billing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;



public final class ServiceDtos {

    private ServiceDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String id, @NotBlank String name, @NotNull @PositiveOrZero BigDecimal price,

                                String unit, @PositiveOrZero int openingStock, @PositiveOrZero int safetyThreshold) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record StockRequest(@NotNull @PositiveOrZero Integer quantity) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(String id, String name, BigDecimal price, String unit, int stock, int safetyThreshold,

                           boolean lowStock) {}

}
