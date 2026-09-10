package com.hospitality.mis.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dto.operations.EquipmentIncidentDtos;
import com.hospitality.mis.entity.operations.EquipmentIncident;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.reservation.ReservationRoom;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.service.billing.PricingPolicy;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.operations.EquipmentIncidentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentIncidentCorrectnessTest {
    @Mock EquipmentIncidentRepository incidents;
    @Mock ReservationRepository reservations;
    @Mock AuditService audit;

    @BeforeEach
    void authenticateHousekeeping() {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("housekeeping", "test",
                List.of(new SimpleGrantedAuthority("ROLE_HOUSEKEEPING"))));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowedHousekeepingActorCanRecordIncidentForOccupiedRoomInReservation() {
        Reservation reservation = checkedInReservation("101");
        when(reservations.findForUpdate(9L)).thenReturn(Optional.of(reservation));
        when(incidents.save(any(EquipmentIncident.class))).thenAnswer(invocation -> {
            EquipmentIncident incident = invocation.getArgument(0);
            ReflectionTestUtils.setField(incident, "id", 44L);
            return incident;
        });

        var response = new EquipmentIncidentService(incidents, reservations,
                new PricingPolicy(3, 20, new BigDecimal("10")), audit).record(9L,
                new EquipmentIncidentDtos.CreateRequest("101", "TV", new BigDecimal("100"),
                        LocalDate.now().minusYears(1), 1), "housekeeping", "incident-9");

        assertThat(response.id()).isEqualTo(44L);
        assertThat(response.compensation()).isEqualByComparingTo("150.00");
        verify(audit).record("housekeeping", "EQUIPMENT_INCIDENT_RECORDED", "RESERVATION", "9",
                null, "150.00", null);
    }

    @Test
    void foreignRoomIsRejectedBeforeIncidentMutation() {
        Reservation reservation = checkedInReservation("101");
        when(reservations.findForUpdate(9L)).thenReturn(Optional.of(reservation));

        DomainException exception = assertThrows(DomainException.class,
                () -> new EquipmentIncidentService(incidents, reservations,
                        new PricingPolicy(3, 20, new BigDecimal("10")), audit).record(9L,
                        new EquipmentIncidentDtos.CreateRequest("999", "TV", new BigDecimal("100"),
                                LocalDate.of(2030, 1, 1), 1), "housekeeping", "incident-foreign-room"));

        assertThat(exception.getCode()).isEqualTo("ROOM_NOT_IN_RESERVATION");
        verify(incidents, never()).save(any());
        verifyNoInteractions(audit);
    }

    @Test
    void incidentRejectsClientActorThatDiffersFromAuthenticatedActorBeforeReservationLoad() {
        var service = new EquipmentIncidentService(incidents, reservations,
                new PricingPolicy(3, 20, new BigDecimal("10")), audit);

        DomainException exception = assertThrows(DomainException.class,
                () -> service.record(9L,
                        new EquipmentIncidentDtos.CreateRequest("101", "TV", new BigDecimal("100"),
                                LocalDate.of(2030, 1, 1), 1), "other", "incident-actor-mismatch"));

        assertThat(exception.getCode()).isEqualTo("ACTOR_MISMATCH");
        verifyNoInteractions(reservations, incidents, audit);
    }

    private Reservation checkedInReservation(String roomId) {
        Reservation reservation = new Reservation();
        reservation.transitionTo(ReservationStatus.CONFIRMED);
        reservation.transitionTo(ReservationStatus.CHECKED_IN);
        Room room = new Room();
        room.setId(roomId);
        room.setStatus(RoomStatus.OCCUPIED);
        ReservationRoom line = new ReservationRoom();
        line.setRoom(room);
        line.setStatus(RoomStatus.OCCUPIED);
        line.setCheckIn(LocalDateTime.of(2031, 1, 1, 14, 0));
        line.setCheckOut(LocalDateTime.of(2031, 1, 2, 12, 0));
        reservation.addRoom(line);
        return reservation;
    }
}
