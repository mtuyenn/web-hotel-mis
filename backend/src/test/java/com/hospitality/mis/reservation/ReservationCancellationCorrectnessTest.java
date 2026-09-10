package com.hospitality.mis.reservation;

import com.hospitality.mis.dao.billing.ServiceLineRepository;
import com.hospitality.mis.dao.billing.ServiceRepository;
import com.hospitality.mis.dao.guest.GuestStore;
import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.dao.operations.InventoryMovementRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.reservation.ReservationDtos;
import com.hospitality.mis.entity.guest.Guest;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.reservation.CancellationOutcome;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.reservation.ReservationRoom;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.service.billing.BillingService;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.reservation.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCancellationCorrectnessTest {
    @org.junit.jupiter.api.BeforeEach
    void authenticateActor() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.TestingAuthenticationToken("frontdesk", "", "ROLE_FRONT_DESK"));
    }
    @org.junit.jupiter.api.AfterEach
    void clearActor() { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }

    @Mock ReservationRepository reservations;
    @Mock GuestStore guests;
    @Mock EmployeeRepository employees;
    @Mock RoomRepository rooms;
    @Mock AuditService audit;
    @Mock BillingService billing;
    @Mock ServiceRepository serviceCatalog;
    @Mock ServiceLineRepository serviceLines;
    @Mock InventoryMovementRepository inventoryMovements;
    private ReservationService service;
    private final LocalDateTime now = LocalDateTime.of(2031, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        service = new ReservationService(reservations, guests, employees, rooms, audit, billing,
                serviceCatalog, serviceLines, inventoryMovements);
        ReflectionTestUtils.setField(service, "clock", Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    @Test
    void exactlyConfiguredBoundaryIsLateAndMoreThanBoundaryIsFree() {
        Reservation atBoundary = reservation(new GuestWithId(41L), employee(), now.plusHours(48));
        when(reservations.findForUpdate(7L)).thenReturn(Optional.of(atBoundary));
        var result = service.cancel(7L, new ReservationDtos.CancelRequest("guest request"), "frontdesk", "cancel-boundary");
        assertThat(result.cancellationOutcome()).isEqualTo(CancellationOutcome.FORFEIT);
        verify(billing).settleCancellationDeposit(atBoundary, "frontdesk", true);

        Reservation outside = reservation(new GuestWithId(42L), employee(), now.plusHours(48).plusMinutes(1));
        when(reservations.findForUpdate(8L)).thenReturn(Optional.of(outside));
        var free = service.cancel(8L, new ReservationDtos.CancelRequest("changed plans"), "frontdesk", "cancel-free");
        assertThat(free.cancellationOutcome()).isEqualTo(CancellationOutcome.REFUND);
        verify(billing).settleCancellationDeposit(outside, "frontdesk", false);
    }

    @Test
    void sameKeyReturnsOneResultAndDifferentPayloadConflicts() {
        Reservation reservation = reservation(new GuestWithId(41L), employee(), now.plusHours(72));
        when(reservations.findForUpdate(7L)).thenReturn(Optional.of(reservation));
        var first = service.cancel(7L, new ReservationDtos.CancelRequest("guest request"), "frontdesk", "cancel-retry");
        var retry = service.cancel(7L, new ReservationDtos.CancelRequest("guest request"), "frontdesk", "cancel-retry");
        assertThat(retry).isEqualTo(first);
        verify(billing, times(1)).settleCancellationDeposit(any(), eq("frontdesk"), eq(false));
        assertThatThrownBy(() -> service.cancel(7L, new ReservationDtos.CancelRequest("other reason"), "frontdesk", "cancel-retry"))
                .hasMessageContaining("Idempotency key");
    }

    @Test
    void checkInRejectsMaintenanceRoomBeforeChangingReservation() {
        Reservation blocked = reservation(new GuestWithId(43L), employee(), now);
        blocked.getRooms().get(0).getRoom().setStatus(com.hospitality.mis.entity.room.RoomStatus.MAINTENANCE);
        when(reservations.findForUpdate(10L)).thenReturn(Optional.of(blocked));
        when(rooms.findAllForUpdateOrdered(List.of("101"))).thenReturn(List.of(blocked.getRooms().get(0).getRoom()));

        assertThatThrownBy(() -> service.checkIn(10L, new ReservationDtos.CheckInRequest(now), "frontdesk", "check-in-maintenance"))
                .extracting("code").isEqualTo("ROOM_NOT_AVAILABLE");
        assertThat(blocked.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(audit, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = com.hospitality.mis.entity.room.RoomStatus.class,
            names = {"OCCUPIED", "CLEANING", "OUT_OF_SERVICE"})
    void checkInRejectsUnreadyPhysicalRoom(com.hospitality.mis.entity.room.RoomStatus status) {
        Reservation blocked = reservation(new GuestWithId(43L), employee(), now);
        blocked.getRooms().get(0).getRoom().setStatus(status);
        when(reservations.findForUpdate(10L)).thenReturn(Optional.of(blocked));
        when(rooms.findAllForUpdateOrdered(List.of("101"))).thenReturn(List.of(blocked.getRooms().get(0).getRoom()));
        assertThatThrownBy(() -> service.checkIn(10L, new ReservationDtos.CheckInRequest(now), "frontdesk", "check-in-unready"))
                .extracting("code").isEqualTo("ROOM_NOT_AVAILABLE");
        assertThat(blocked.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test void checkInRechecksOverlapsBeforeChangingState() {
        Reservation blocked = reservation(new GuestWithId(43L), employee(), now);
        blocked.getRooms().get(0).getRoom().setStatus(com.hospitality.mis.entity.room.RoomStatus.READY);
        when(reservations.findForUpdate(10L)).thenReturn(Optional.of(blocked));
        when(rooms.findAllForUpdateOrdered(List.of("101"))).thenReturn(List.of(blocked.getRooms().get(0).getRoom()));
        when(reservations.hasOverlapExcludingReservation(eq(10L), eq("101"), eq(now), eq(now.plusHours(4)), any(), any()))
                .thenReturn(true);
        assertThatThrownBy(() -> service.checkIn(10L, new ReservationDtos.CheckInRequest(now), "frontdesk", "check-in-overlap"))
                .extracting("code").isEqualTo("OVERBOOKING");
        assertThat(blocked.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    private Reservation reservation(Guest guest, Employee employee, LocalDateTime checkIn) {
        Reservation result = new Reservation(); result.setGuest(guest); result.setEmployee(employee);
        result.setDepositAmount(new BigDecimal("100.00"));
        result.transitionTo(ReservationStatus.CONFIRMED);
        Room room = new Room(); room.setId("101");
        ReservationRoom line = new ReservationRoom(); line.setRoom(room); line.setCheckIn(checkIn); line.setCheckOut(checkIn.plusHours(4)); line.setStatus(com.hospitality.mis.entity.room.RoomStatus.RESERVED);
        result.addRoom(line);
        return result;
    }

    private Employee employee() { Employee employee = new Employee(); employee.setEmployeeId("frontdesk"); return employee; }

    private static class GuestWithId extends Guest {
        GuestWithId(Long id) { setId(id); setFullName("Guest"); setPhone("090000000" + id); setIdentityNumber("00100100100" + id); }
    }
}
