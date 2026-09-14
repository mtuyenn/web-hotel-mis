package com.hospitality.mis.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dao.room.RoomEquipmentRepository;
import com.hospitality.mis.dto.operations.EquipmentIncidentDtos;
import com.hospitality.mis.entity.operations.EquipmentIncident;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.reservation.ReservationRoom;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.entity.room.RoomEquipment;
import com.hospitality.mis.service.billing.PricingPolicy;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.operations.EquipmentIncidentService;
import com.hospitality.mis.service.governance.NotificationOutboxService;
import com.hospitality.mis.entity.operations.IncidentSeverity;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/** Bảo vệ incident: actor, phòng thuộc reservation và compensation/audit. */
class EquipmentIncidentCorrectnessTest {
    /** Mock ports: incident là mutation target, reservation là aggregate lock, audit là side-effect assertion. */
    @Mock EquipmentIncidentRepository incidents;
    @Mock ReservationRepository reservations;
    @Mock AuditService audit;
    @Mock RoomEquipmentRepository equipmentRegistry;
    @Mock NotificationOutboxService notifications;

    @BeforeEach
    /** Đặt actor housekeeping hợp lệ cho các case mutation. */
    void authenticateHousekeeping() {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("housekeeping", "test",
                List.of(new SimpleGrantedAuthority("ROLE_HOUSEKEEPING"))));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    /** Dọn SecurityContext sau test. */
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    /** Given phòng occupied thuộc booking, When record, Then save compensation 150 và audit actor. */
    void allowedHousekeepingActorCanRecordIncidentForOccupiedRoomInReservation() {
        Reservation reservation = checkedInReservation("101");
        when(reservations.findForUpdate(9L)).thenReturn(Optional.of(reservation));
        RoomEquipment equipment = equipment("101", "TV", new BigDecimal("1000"), LocalDate.now().minusYears(1), 2);
        when(equipmentRegistry.findByRoomIdAndActiveTrueOrderByNameAsc("101")).thenReturn(List.of(equipment));
        when(incidents.save(any(EquipmentIncident.class))).thenAnswer(invocation -> {
            EquipmentIncident incident = invocation.getArgument(0);
            ReflectionTestUtils.setField(incident, "id", 44L);
            return incident;
        });

        var service = new EquipmentIncidentService(incidents, reservations, equipmentRegistry,
                new PricingPolicy(3, 20, new BigDecimal("10")), audit);
        ReflectionTestUtils.setField(service, "notifications", notifications);
        var response = service.record(9L,
                new EquipmentIncidentDtos.CreateRequest("101", "TV", new BigDecimal("1"),
                        LocalDate.now(), 1, IncidentSeverity.HIGH), "housekeeping", "incident-9");

        assertThat(response.id()).isEqualTo(44L);
        assertThat(response.compensation()).isEqualByComparingTo("1500.00");
        verify(audit).record("housekeeping", "EQUIPMENT_INCIDENT_RECORDED", "RESERVATION", "9",
                null, "1500.00", null);
        verify(notifications).enqueue(eq("EQUIPMENT_INCIDENT"), eq("MANAGER"), anyString(),
                eq("equipment-incident-manager-44"));
    }

    @Test
    /** Given room ngoài booking, When record, Then fail trước save/audit với ROOM_NOT_IN_RESERVATION. */
    void foreignRoomIsRejectedBeforeIncidentMutation() {
        Reservation reservation = checkedInReservation("101");
        when(reservations.findForUpdate(9L)).thenReturn(Optional.of(reservation));

        DomainException exception = assertThrows(DomainException.class,
                () -> new EquipmentIncidentService(incidents, reservations, equipmentRegistry,
                        new PricingPolicy(3, 20, new BigDecimal("10")), audit).record(9L,
                        new EquipmentIncidentDtos.CreateRequest("999", "TV", new BigDecimal("100"),
                                LocalDate.of(2030, 1, 1), 1), "housekeeping", "incident-foreign-room"));

        assertThat(exception.getCode()).isEqualTo("ROOM_NOT_IN_RESERVATION");
        verify(incidents, never()).save(any());
        verifyNoInteractions(audit);
    }

    @Test
    /** Given actor request khác authenticated, When record, Then fail trước cả reservation load. */
    void incidentRejectsClientActorThatDiffersFromAuthenticatedActorBeforeReservationLoad() {
        var service = new EquipmentIncidentService(incidents, reservations, equipmentRegistry,
                new PricingPolicy(3, 20, new BigDecimal("10")), audit);

        DomainException exception = assertThrows(DomainException.class,
                () -> service.record(9L,
                        new EquipmentIncidentDtos.CreateRequest("101", "TV", new BigDecimal("100"),
                                LocalDate.of(2030, 1, 1), 1), "other", "incident-actor-mismatch"));

        assertThat(exception.getCode()).isEqualTo("ACTOR_MISMATCH");
        verifyNoInteractions(reservations, incidents, equipmentRegistry, audit);
    }

    /** Dựng reservation CHECKED_IN với một line OCCUPIED để kiểm tra membership của room. */
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

    private RoomEquipment equipment(String roomId, String name, BigDecimal value, LocalDate purchasedOn, int quantity) {
        Room room = new Room(); room.setId(roomId);
        RoomEquipment equipment = new RoomEquipment(); equipment.setRoom(room); equipment.setName(name);
        equipment.setOriginalValue(value); equipment.setPurchasedOn(purchasedOn); equipment.setQuantity(quantity);
        return equipment;
    }
}
