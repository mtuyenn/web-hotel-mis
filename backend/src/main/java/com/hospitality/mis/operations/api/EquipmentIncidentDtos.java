package com.hospitality.mis.operations.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class EquipmentIncidentDtos {
    private EquipmentIncidentDtos() {}
    public record CreateRequest(@NotBlank String roomId, @NotBlank String equipmentName, @NotNull @Positive BigDecimal originalValue,
                                @NotNull LocalDate purchasedAt, @Positive int quantity) {}
    public record Response(Long id, String roomId, String equipmentName, BigDecimal compensation) {}
}
