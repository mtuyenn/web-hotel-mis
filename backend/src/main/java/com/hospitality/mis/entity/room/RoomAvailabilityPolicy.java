package com.hospitality.mis.entity.room;



import java.time.LocalDateTime;

import java.util.Objects;



/** Quy tắc khả dụng thuộc ngữ cảnh giới hạn phòng. */

public final class RoomAvailabilityPolicy {

    /** Kiểm tra khoảng thời gian nửa kín [from, to) có thứ tự hợp lệ. */
    public void validateInterval(LocalDateTime from, LocalDateTime to) {

        if (from == null || to == null || !from.isBefore(to)) {

            throw new IllegalArgumentException("from must be before to");

        }

    }



    /**

     * Phòng chỉ khả dụng khi trạng thái vận hành cho phép phân bổ

     * và bộ chuyển đổi đặt phòng không phát hiện khoảng thời gian nửa kín bị chồng lấp.

     */

    /** Kết hợp trạng thái phòng và kết quả kiểm tra chồng lấp để quyết định khả dụng. */
    public boolean isAvailable(Room room, boolean hasOverlappingReservation) {

        Objects.requireNonNull(room, "room");

        return !room.getStatus().blocksAvailability() && !hasOverlappingReservation;

    }



    /** Biến thể dùng trực tiếp trạng thái khi chưa cần tải entity phòng. */
    public boolean isAvailable(RoomStatus status, boolean hasOverlappingReservation) {

        Objects.requireNonNull(status, "status");

        return !status.blocksAvailability() && !hasOverlappingReservation;

    }

}
