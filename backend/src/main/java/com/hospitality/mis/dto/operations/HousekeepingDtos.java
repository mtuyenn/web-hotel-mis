package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class HousekeepingDtos {
    private HousekeepingDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String roomId, String assignee, String note) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record UpdateRequest(@NotNull String status, Boolean checklistComplete, Boolean blockingIncident, String note, String assignee) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, String roomId, String assignee, String status, boolean checklistComplete,
                           boolean blockingIncident, String note, String assignedBy, LocalDateTime updatedAt) {}
}
