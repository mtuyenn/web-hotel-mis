package com.hospitality.mis.dto.reservation;
import com.hospitality.mis.entity.billing.PaymentMethod;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.entity.reservation.CancellationOutcome;
import jakarta.validation.Valid;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotEmpty;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.PositiveOrZero;



import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.List;



public final class ReservationDtos {

    private ReservationDtos() {}



    public enum RentalType { PACKAGE, HOURLY }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record PageResponse(List<Response> items, int page, int size, long totalElements, int totalPages) {}



    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record RoomStay(@NotBlank String roomId, @NotNull LocalDateTime expectedCheckIn,
                           @NotNull LocalDateTime expectedCheckOut) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CreateRequest(@NotNull Long guestId, @NotBlank String employeeId,
                                @PositiveOrZero BigDecimal deposit, @NotNull RentalType rentalType,
                                @NotEmpty @Valid List<RoomStay> rooms, String idempotencyKey) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CheckInRequest(LocalDateTime at) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CheckOutRequest(LocalDateTime at, com.hospitality.mis.entity.billing.PaymentMethod paymentMethod) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CancelRequest(@NotBlank String reason) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ExtendRequest(@NotNull LocalDateTime newExpectedCheckOut) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record AddServiceRequest(@NotBlank String serviceId, @NotNull @jakarta.validation.constraints.Positive Integer quantity,
                                    LocalDateTime usedAt) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record RoomLine(String roomId, LocalDateTime expectedCheckIn, LocalDateTime expectedCheckOut,
                           LocalDateTime actualCheckIn, LocalDateTime actualCheckOut) {}
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Response(Long id, Long guestId, String employeeId, ReservationStatus status, RentalType rentalType,
                           BigDecimal deposit, LocalDateTime bookedAt, LocalDateTime actualCheckIn,
                           LocalDateTime actualCheckOut, List<RoomLine> rooms,
                           String cancellationReason, CancellationOutcome cancellationOutcome) {
        public Response(Long id, Long guestId, String employeeId, ReservationStatus status, RentalType rentalType,
                        BigDecimal deposit, LocalDateTime bookedAt, LocalDateTime actualCheckIn,
                        LocalDateTime actualCheckOut, List<RoomLine> rooms) {
            this(id, guestId, employeeId, status, rentalType, deposit, bookedAt, actualCheckIn,
                    actualCheckOut, rooms, null, null);
        }
    }

}
