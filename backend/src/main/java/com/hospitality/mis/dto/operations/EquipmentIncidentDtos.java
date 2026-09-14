package com.hospitality.mis.dto.operations;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import java.time.LocalDate;



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
                                /** Giá trị gốc dùng tính bồi thường. */
                                @NotNull @Positive BigDecimal originalValue,

                                /** Ngày mua dùng tính thời gian sử dụng. */
                                @NotNull LocalDate purchasedAt,
                                /** Số lượng thiết bị bị ảnh hưởng. */
                                @Positive int quantity) {}

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
                           BigDecimal compensation) {}

}
