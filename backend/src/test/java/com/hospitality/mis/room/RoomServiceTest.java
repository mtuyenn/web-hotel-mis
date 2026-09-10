package com.hospitality.mis.room;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.room.ReservationOverlapPort;
import com.hospitality.mis.dao.room.RoomStore;
import com.hospitality.mis.dto.room.RoomDtos;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.entity.room.RoomType;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.room.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    private static final LocalDateTime FROM = LocalDateTime.of(2031, 1, 10, 14, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2031, 1, 11, 12, 0);

    @Mock RoomStore rooms;
    @Mock ReservationOverlapPort overlaps;
    @Mock AuditService audit;

    private RoomService service;

    @BeforeEach
    void setUp() {
        setActor("manager", "MANAGER");
        service = new RoomService(rooms, overlaps, audit);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchMapsCanonicalRoomAndRoomTypeWithoutLegacyTypesInTheServiceContract() {
        Room room = room("R101", RoomStatus.READY);
        when(rooms.search("STD", RoomStatus.READY)).thenReturn(List.of(room));

        RoomDtos.Response response = service.search("STD", RoomStatus.READY).getFirst();

        assertThat(response.id()).isEqualTo("R101");
        assertThat(response.name()).isEqualTo("Room 101");
        assertThat(response.roomTypeId()).isEqualTo("STD");
        assertThat(response.roomTypeName()).isEqualTo("Standard");
        assertThat(response.dailyPrice()).isEqualByComparingTo("2400.00");
        assertThat(response.floor()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(RoomStatus.READY);
    }

    @Test
    void availabilityCombinesOperationalStateWithHalfOpenOverlapProtection() {
        Room ready = room("R101", RoomStatus.READY);
        Room maintenance = room("R102", RoomStatus.MAINTENANCE);
        Room cleaning = room("R103", RoomStatus.CLEANING);
        when(rooms.search(null, null)).thenReturn(List.of(ready, maintenance, cleaning));
        when(overlaps.hasOverlap("R101", FROM, TO)).thenReturn(true);
        when(overlaps.hasOverlap("R102", FROM, TO)).thenReturn(false);
        when(overlaps.hasOverlap("R103", FROM, TO)).thenReturn(false);

        List<RoomDtos.Availability> availability = service.availability(FROM, TO, null);

        assertThat(availability).extracting(RoomDtos.Availability::available)
                .containsExactly(false, false, false);
        verify(overlaps).hasOverlap("R101", FROM, TO);
        verify(overlaps).hasOverlap("R102", FROM, TO);
        verify(overlaps).hasOverlap("R103", FROM, TO);
    }

    @Test
    void availabilityRejectsNonPositiveIntervalsBeforeTouchingPersistence() {
        DomainException exception = assertThrows(DomainException.class,
                () -> service.availability(TO, FROM, null));

        assertThat(exception.getCode()).isEqualTo("INVALID_INTERVAL");
    }

    @Test
    void technicalMaintenanceTransitionUsesTheLockedStateAndAuditActor() {
        setActor("technical", "TECHNICAL");
        Room room = room("R101", RoomStatus.READY);
        when(rooms.findForUpdate("R101")).thenReturn(Optional.of(room));

        RoomDtos.Response response = service.updateStatus("R101", RoomStatus.MAINTENANCE, "technical");

        assertThat(response.status()).isEqualTo(RoomStatus.MAINTENANCE);
        verify(rooms).findForUpdate("R101");
        verify(audit).record("technical", "ROOM_STATUS_CHANGED", "ROOM", "R101",
                "SAN_SANG", "BAO_TRI", null);
    }

    @Test
    void managerCanUseTheExplicitMaintenancePath() {
        Room room = room("R101", RoomStatus.READY);
        when(rooms.findForUpdate("R101")).thenReturn(Optional.of(room));

        service.updateStatus("R101", RoomStatus.MAINTENANCE, "manager");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.MAINTENANCE);
    }

    @Test
    void statusUpdateRejectsNullStatusWithoutChangingAStoredRoom() {
        DomainException exception = assertThrows(DomainException.class,
                () -> service.updateStatus("R101", null, "manager"));

        assertThat(exception.getCode()).isEqualTo("INVALID_ROOM_STATUS");
        verifyNoInteractions(rooms, audit);
    }

    @Test
    void occupiedRoomCannotBePatchedToReady() {
        Room room = room("R101", RoomStatus.OCCUPIED);
        when(rooms.findForUpdate("R101")).thenReturn(Optional.of(room));

        DomainException exception = assertThrows(DomainException.class,
                () -> service.updateStatus("R101", RoomStatus.READY, "manager"));

        assertThat(exception.getCode()).isEqualTo("INVALID_ROOM_TRANSITION");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        verify(audit, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void housekeepingCannotMakeRoomAvailable() {
        setActor("housekeeping", "HOUSEKEEPING");
        Room room = room("R101", RoomStatus.MAINTENANCE);
        when(rooms.findForUpdate("R101")).thenReturn(Optional.of(room));

        DomainException exception = assertThrows(DomainException.class,
                () -> service.updateStatus("R101", RoomStatus.READY, "housekeeping"));

        assertThat(exception.getCode()).isEqualTo("ROOM_STATUS_FORBIDDEN");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.MAINTENANCE);
        verify(audit, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void statusUpdateRejectsClientActorThatDiffersFromAuthenticatedActor() {
        DomainException exception = assertThrows(DomainException.class,
                () -> service.updateStatus("R101", RoomStatus.MAINTENANCE, "other"));

        assertThat(exception.getCode()).isEqualTo("ACTOR_MISMATCH");
        verifyNoInteractions(rooms, audit);
    }

    private void setActor(String actor, String role) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(actor, "test",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))));
        SecurityContextHolder.setContext(context);
    }

    private Room room(String id, RoomStatus status) {
        RoomType type = new RoomType();
        type.setId("STD");
        type.setName("Standard");
        type.setDailyPrice(new BigDecimal("2400.00"));

        Room room = new Room();
        room.setId(id);
        room.setName("Room 101");
        room.setFloor(1);
        room.setRoomType(type);
        room.setStatus(status);
        return room;
    }
}
