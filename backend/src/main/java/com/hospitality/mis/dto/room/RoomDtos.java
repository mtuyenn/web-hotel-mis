package com.hospitality.mis.dto.room;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


import com.hospitality.mis.entity.room.RoomStatus;



import java.math.BigDecimal;



public final class RoomDtos {

    private RoomDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(String id, String name, String roomTypeId, String roomTypeName, BigDecimal dailyPrice,

                           Integer floor, RoomStatus status) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Availability(String roomId, String roomTypeId, String roomTypeName, BigDecimal dailyPrice,

                               Integer floor, boolean available) {}

}
