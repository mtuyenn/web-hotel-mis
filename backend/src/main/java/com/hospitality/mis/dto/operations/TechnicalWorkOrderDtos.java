package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class TechnicalWorkOrderDtos {
    private TechnicalWorkOrderDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String roomId, Long equipmentId, String assignee, @NotBlank String priority,
                                LocalDateTime slaDueAt, String materials) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record UpdateRequest(@NotNull String status, String resultNote, String acceptanceNote,
                                String assignee, String materials) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, String roomId, Long equipmentId, String assignee, String priority,
                           LocalDateTime slaDueAt, String materials, String resultNote, String acceptanceNote,
                           String status, String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
