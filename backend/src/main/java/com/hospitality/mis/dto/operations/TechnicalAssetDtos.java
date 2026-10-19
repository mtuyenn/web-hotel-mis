package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TechnicalAssetDtos {
    private TechnicalAssetDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record TechnicalAssetResponse(String id, String name, String category, String locationType,
                                         String roomId, Integer floor, String location, String brandModel,
                                         LocalDate installedOn, LocalDate nextMaintenance, String status,
                                         BigDecimal originalValue, String note, boolean active) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record TechnicalAssetRequest(@NotBlank String id, @NotBlank String name, @NotBlank String category,
                                        @NotBlank String locationType, String roomId, Integer floor,
                                        @NotBlank String location, String brandModel, LocalDate installedOn,
                                        LocalDate nextMaintenance, String status, @PositiveOrZero BigDecimal originalValue,
                                        String note) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record TechnicalAssetStatusRequest(@NotBlank String status) {}
}
