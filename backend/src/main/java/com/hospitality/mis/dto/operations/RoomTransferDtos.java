package com.hospitality.mis.dto.operations;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class RoomTransferDtos {
    private RoomTransferDtos() {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotBlank String fromRoomId, @NotBlank String toRoomId,
                                LocalDateTime transferredAt, String reason) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long reservationId, String fromRoomId, String toRoomId,
                           LocalDateTime transferredAt, String reason) {}
}
