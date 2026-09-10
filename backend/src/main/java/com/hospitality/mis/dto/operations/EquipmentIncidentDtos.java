package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import java.time.LocalDate;



public final class EquipmentIncidentDtos {

    private EquipmentIncidentDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String roomId, @NotBlank String equipmentName, @NotNull @Positive BigDecimal originalValue,

                                @NotNull LocalDate purchasedAt, @Positive int quantity) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, String roomId, String equipmentName, BigDecimal compensation) {}

}
