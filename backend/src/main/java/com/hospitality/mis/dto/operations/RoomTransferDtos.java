package com.hospitality.mis.dto.operations;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** DTO lịch sử chuyển khách giữa hai phòng. */
public final class RoomTransferDtos {
    /** Namespace không trạng thái cho payload chuyển phòng. */
    private RoomTransferDtos() {}
    /** Request chuyển từ phòng nguồn sang phòng đích; hai mã phòng bắt buộc. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(
                                /** Mã phòng nguồn. */
                                @NotBlank String fromRoomId,
                                /** Mã phòng đích. */
                                @NotBlank String toRoomId,
                                /** Thời điểm chuyển, có thể để service mặc định. */
                                LocalDateTime transferredAt,
                                /** Lý do chuyển. */
                                String reason) {}
    /** Kết quả chuyển phòng gắn với reservation và thời điểm thực tế. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(
                           /** Khóa lịch sử chuyển. */
                           Long id,
                           /** Đặt phòng liên quan. */
                           Long reservationId,
                           /** Phòng nguồn. */
                           String fromRoomId,
                           /** Phòng đích. */
                           String toRoomId,
                           /** Thời điểm chuyển. */
                           LocalDateTime transferredAt,
                           /** Lý do. */
                           String reason) {}
}
