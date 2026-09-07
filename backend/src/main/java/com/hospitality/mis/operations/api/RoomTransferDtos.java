package com.hospitality.mis.operations.api;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class RoomTransferDtos {
    private RoomTransferDtos() {}
    public record CreateRequest(@NotBlank String fromRoomId, @NotBlank String toRoomId,
                                LocalDateTime transferredAt, String reason) {}
    public record Response(Long id, Long reservationId, String fromRoomId, String toRoomId,
                           LocalDateTime transferredAt, String reason) {}
}
