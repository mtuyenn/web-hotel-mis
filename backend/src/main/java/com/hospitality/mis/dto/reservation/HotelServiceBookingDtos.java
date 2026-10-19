package com.hospitality.mis.dto.reservation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** API and read-model types for hotel service bookings. Kept outside service/DAO layers. */
public final class HotelServiceBookingDtos {
    private HotelServiceBookingDtos() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Request(Long reservationId, String roomId, String serviceId, LocalDateTime scheduledAt,
                          Integer quantity, String mealPeriod, String note) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long reservationId, String roomId, String serviceId, String serviceName,
                           LocalDateTime scheduledAt, int quantity, int freeQuantity, BigDecimal unitPrice,
                           BigDecimal amountDue, String mealPeriod, String status, String note, LocalDateTime endsAt,
                           LocalDateTime paymentExpiresAt, BigDecimal paidAmount, String paymentReference,
                           String paymentResponse) {
        public Response(Long id, Long reservationId, String roomId, String serviceId, LocalDateTime scheduledAt,
                        int quantity, int freeQuantity, BigDecimal unitPrice, BigDecimal amountDue,
                        String mealPeriod, String status, String note) {
            this(id, reservationId, roomId, serviceId, serviceId, scheduledAt, quantity, freeQuantity, unitPrice,
                    amountDue, mealPeriod, status, note, scheduledAt.plusHours(1), null, BigDecimal.ZERO, null, null);
        }
    }

}
