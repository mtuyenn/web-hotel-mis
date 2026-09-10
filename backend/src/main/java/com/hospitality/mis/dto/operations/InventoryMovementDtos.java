package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.operations.InventoryMovement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public final class InventoryMovementDtos {
    private InventoryMovementDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String serviceId, @NotNull InventoryMovement.MovementType type,
                                @Positive int quantity, String reason) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, String serviceId, InventoryMovement.MovementType type, int quantity,
                           String actorId, LocalDateTime occurredAt, String reason) {}
}
