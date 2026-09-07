package com.hospitality.mis.operations.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public final class MaintenanceDtos {
    private MaintenanceDtos() {}
    public record CreateRequest(@NotBlank String id, @NotBlank String roomId,
                                @NotBlank String type, @NotNull LocalDate scheduledDate,
                                String description) {}
    public record StatusRequest(@NotBlank String status) {}
    public record Response(String id, String roomId, String type, LocalDate scheduledDate,
                           String status, String description) {}
}
