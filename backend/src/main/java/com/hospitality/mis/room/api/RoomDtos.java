package com.hospitality.mis.room.api;

import com.hospitality.mis.room.domain.RoomStatus;

import java.math.BigDecimal;

public final class RoomDtos {
    private RoomDtos() {}
    public record Response(String id, String name, String roomTypeId, String roomTypeName, BigDecimal dailyPrice,
                           Integer floor, RoomStatus status) {}
    public record Availability(String roomId, String roomTypeId, String roomTypeName, BigDecimal dailyPrice,
                               Integer floor, boolean available) {}
}
