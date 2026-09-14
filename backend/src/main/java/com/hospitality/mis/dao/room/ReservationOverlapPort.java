package com.hospitality.mis.dao.room;



import java.time.LocalDateTime;



/** Cổng chỉ đọc dùng để kiểm tra trùng khoảng thời gian đặt phòng khi xác định tình trạng sẵn có của phòng. */

@FunctionalInterface

public interface ReservationOverlapPort {

    /** Kiểm tra phòng có đặt phòng đang chiếm chỗ giao với khoảng thời gian đã cho hay không. */
    boolean hasOverlap(String roomId, LocalDateTime from, LocalDateTime to);

}
