package com.hospitality.mis.reservation.api;

import com.hospitality.mis.reservation.domain.ReservationStatus;
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

    public record RoomStay(@NotBlank String roomId, @NotNull LocalDateTime expectedCheckIn,
                           @NotNull LocalDateTime expectedCheckOut) {}

    public record CreateRequest(@NotNull Long guestId, @NotBlank String employeeId,
                                @PositiveOrZero BigDecimal deposit, @NotNull RentalType rentalType,
                                @NotEmpty @Valid List<RoomStay> rooms, String idempotencyKey) {}

    public record CheckInRequest(LocalDateTime at) {}
    public record CheckOutRequest(LocalDateTime at, com.hospitality.mis.billing.domain.PaymentMethod paymentMethod) {}
    public record ExtendRequest(@NotNull LocalDateTime newExpectedCheckOut) {}
    public record AddServiceRequest(@NotBlank String serviceId, @NotNull @jakarta.validation.constraints.Positive Integer quantity,
                                    LocalDateTime usedAt) {}

    public record RoomLine(String roomId, LocalDateTime expectedCheckIn, LocalDateTime expectedCheckOut,
                           LocalDateTime actualCheckIn, LocalDateTime actualCheckOut) {}
    public record Response(Long id, Long guestId, String employeeId, ReservationStatus status, RentalType rentalType,
                           BigDecimal deposit, LocalDateTime bookedAt, LocalDateTime actualCheckIn,
                           LocalDateTime actualCheckOut, List<RoomLine> rooms) {}
}
