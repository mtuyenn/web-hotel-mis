package com.hospitality.mis.entity.guest;



import java.util.Objects;



/** Chính sách đủ điều kiện đặt phòng thuộc ngữ cảnh giới hạn khách. */

public final class BookingPolicy {

    /** Số lần hủy muộn từ đó khách bị chặn đặt phòng theo mặc định. */
    public static final int DEFAULT_BLOCK_AT_LATE_CANCELLATION_COUNT = 4;



    /** Ngưỡng cấu hình; phải là số dương. */
    private final int blockAtLateCancellationCount;



    /** Tạo chính sách và kiểm tra ngưỡng chặn hợp lệ. */
    public BookingPolicy(int blockAtLateCancellationCount) {

        if (blockAtLateCancellationCount <= 0) {

            throw new IllegalArgumentException("blockAtLateCancellationCount must be positive");

        }

        this.blockAtLateCancellationCount = blockAtLateCancellationCount;

    }



    /** Tạo chính sách mặc định của hệ thống. */
    public static BookingPolicy defaults() {

        return new BookingPolicy(DEFAULT_BLOCK_AT_LATE_CANCELLATION_COUNT);

    }



    public int blockAtLateCancellationCount() {

        return blockAtLateCancellationCount;

    }



    /** Kiểm tra khách có đang bị chặn đặt phòng hay không. */
    public boolean allows(Guest guest) {

        return !Objects.requireNonNull(guest, "guest").isBookingBlocked();

    }



    /** Ghi nhận hủy muộn vào khách và áp dụng ngưỡng chặn cấu hình. */
    public void recordLateCancellation(Guest guest) {

        Objects.requireNonNull(guest, "guest")

                .recordLateCancellation(blockAtLateCancellationCount);

    }

}
