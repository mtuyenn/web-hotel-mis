package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import java.time.LocalDate;
import com.hospitality.mis.entity.operations.IncidentSeverity;
import com.hospitality.mis.entity.operations.IncidentHandoffStatus;



/** DTO sự cố thiết bị trong phòng và giá trị dùng tính bồi thường. */
public final class EquipmentIncidentDtos {

    /** Namespace không trạng thái cho payload sự cố thiết bị. */
    private EquipmentIncidentDtos() {}

    /** Request tạo sự cố; giá trị và số lượng dương để tính bồi thường. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(
                                /** Mã phòng nơi xảy ra sự cố. */
                                @NotBlank String roomId,
                                /** Tên thiết bị bị ảnh hưởng. */
                                @NotBlank String equipmentName,
                                /** ID registry được ưu tiên; giá/ngày mua luôn được đọc từ registry. */
                                Long equipmentId,
                                /** Trường tương thích ngược, không được tin dùng để tính tiền. */
                                BigDecimal originalValue,
                                /** Trường tương thích ngược, không được tin dùng để tính tiền. */
                                LocalDate purchasedAt,
                                /** Số lượng thiết bị bị ảnh hưởng. */
                                @Positive int quantity,
                                IncidentSeverity severity) {
        public CreateRequest(String roomId, String equipmentName, BigDecimal originalValue, LocalDate purchasedAt, int quantity) {
            this(roomId, equipmentName, null, originalValue, purchasedAt, quantity, null);
        }
        public CreateRequest(String roomId, String equipmentName, BigDecimal originalValue, LocalDate purchasedAt,
                             int quantity, IncidentSeverity severity) {
            this(roomId, equipmentName, null, originalValue, purchasedAt, quantity, severity);
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record HandoffRequest(@NotNull IncidentHandoffStatus status, String note) {}

    /** Kết quả sự cố cùng số tiền bồi thường đã tính. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(
                           /** Khóa sự cố. */
                           Long id,
                           /** Mã phòng. */
                           String roomId,
                           /** Tên thiết bị. */
                           String equipmentName,
                           /** Mức bồi thường đã tính. */
                           BigDecimal compensation, IncidentSeverity severity, IncidentHandoffStatus handoffStatus, String handoffNote) {
        public Response(Long id, String roomId, String equipmentName, BigDecimal compensation) {
            this(id, roomId, equipmentName, compensation, IncidentSeverity.MEDIUM, IncidentHandoffStatus.OPEN, null);
        }
    }

}
