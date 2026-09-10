package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public final class MaintenanceDtos {
    private MaintenanceDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String id, @NotBlank String roomId,
                                @NotBlank String type, @NotNull LocalDate scheduledDate,
                                String description) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record StatusRequest(@NotBlank String status) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(String id, String roomId, String type, LocalDate scheduledDate,
                           String status, String description) {}
}
