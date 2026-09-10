package com.hospitality.mis.dto.room;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class RoomEquipmentDtos {
    private RoomEquipmentDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String roomId, @NotBlank String name,
                                @NotNull @Positive BigDecimal originalValue, @NotNull LocalDate purchasedOn,
                                @Positive int quantity) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, String roomId, String name, BigDecimal originalValue,
                           LocalDate purchasedOn, int quantity, boolean active) {}
}
