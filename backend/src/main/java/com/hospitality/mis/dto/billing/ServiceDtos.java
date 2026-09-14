package com.hospitality.mis.dto.billing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;



/** DTO quản lý dịch vụ bán thêm và tồn kho; JSON dùng tên trường snake_case. */
public final class ServiceDtos {

    /** Namespace không trạng thái cho các payload dịch vụ. */
    private ServiceDtos() {}

    /** Dữ liệu tạo dịch vụ và mức tồn kho ban đầu. Các annotation áp dụng validation ở biên API. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(
                                /** Mã dịch vụ duy nhất. */
                                @NotBlank String id,
                                /** Tên hiển thị của dịch vụ. */
                                @NotBlank String name,
                                /** Đơn giá không âm. */
                                @NotNull @PositiveOrZero BigDecimal price,

                                /** Đơn vị tính, có thể bỏ trống. */
                                String unit,
                                /** Tồn đầu kỳ không âm. */
                                @PositiveOrZero int openingStock,
                                /** Ngưỡng cảnh báo tồn thấp không âm. */
                                @PositiveOrZero int safetyThreshold) {}

    /** Request thay đổi tồn kho; quantity phải là số không âm và không được null. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record StockRequest(
                               /** Số lượng tồn/điều chỉnh được endpoint áp dụng. */
                               @NotNull @PositiveOrZero Integer quantity) {}

    /** Trạng thái dịch vụ để hiển thị giá, tồn kho và cảnh báo tồn thấp. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(
                           /** Mã dịch vụ. */
                           String id,
                           /** Tên dịch vụ. */
                           String name,
                           /** Đơn giá hiện tại. */
                           BigDecimal price,
                           /** Đơn vị tính. */
                           String unit,
                           /** Tồn kho hiện tại. */
                           int stock,
                           /** Ngưỡng cảnh báo tồn thấp. */
                           int safetyThreshold,

                           /** Có đang ở dưới hoặc bằng ngưỡng cảnh báo hay không. */
                           boolean lowStock) {}

}
