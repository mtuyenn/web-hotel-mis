package com.hospitality.mis.dto.guest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.guest.MembershipTier;
import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Pattern;

import jakarta.validation.constraints.Size;



import java.math.BigDecimal;



/** DTO hồ sơ khách và các ràng buộc dữ liệu nhận diện khi tạo mới. */
public final class GuestDtos {

    /** Namespace không trạng thái cho payload khách. */
    private GuestDtos() {}



    /** Request tạo khách; validation bảo vệ độ dài, số điện thoại và email ở biên API. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(
                                /** Họ tên khách, tối đa 100 ký tự và không được trống. */
                                @NotBlank @Size(max = 100) String fullName,
                                /** Năm sinh tùy chọn. */
                                Integer birthYear,
                                /** Số giấy tờ nhận diện, bắt buộc và tối đa 12 ký tự. */
                                @NotBlank @Size(max = 12) String identityNumber,
                                /** Số điện thoại bắt buộc theo pattern hiện hành. */
                                @NotBlank @Size(max = 15) @Pattern(regexp = "[0-9+ .-]{8,15}") String phone,
                                /** Email tùy chọn nhưng phải đúng định dạng nếu có. */
                                @Email @Size(max = 100) String email,
                                /** Địa chỉ tùy chọn, tối đa 255 ký tự. */
                                @Size(max = 255) String address) {}

    /** Hồ sơ khách kèm chỉ số chi tiêu và các cờ hạn chế đặt phòng. */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(
                           /** Khóa khách. */
                           Long id,
                           /** Họ tên. */
                           String fullName,
                           /** Năm sinh nếu có. */
                           Integer birthYear,
                           /** Số giấy tờ. */
                           String identityNumber,
                           /** Số điện thoại. */
                           String phone,
                           /** Email liên hệ. */
                           String email,
                           /** Địa chỉ. */
                           String address,
                           /** Hạng thành viên hiện tại. */
                           MembershipTier membershipTier,
                           /** Tổng chi tiêu dùng xét hạng hoặc báo cáo. */
                           BigDecimal totalSpend,
                           /** Số lần hủy muộn. */
                           int lateCancellationCount,
                           /** Số lần trả phòng muộn. */
                           int lateCheckoutCount,
                           /** Có đang bị chặn tạo booking mới hay không. */
                           boolean bookingBlocked) {}
}
