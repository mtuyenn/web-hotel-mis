package com.hospitality.mis.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.HousekeepingTaskRepository;
import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.operations.TechnicalWorkOrderRepository;
import com.hospitality.mis.dao.room.ReservationOverlapPort;
import com.hospitality.mis.dao.room.RoomEquipmentRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.operations.TechnicalWorkOrderDtos;
import com.hospitality.mis.entity.operations.*;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.operations.TechnicalWorkOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicalWorkOrderWorkflowTest {
    @Mock TechnicalWorkOrderRepository orders;
    @Mock RoomRepository rooms;
    @Mock RoomEquipmentRepository equipment;
    @Mock AuditService audit;
    @Mock HousekeepingTaskRepository housekeepingTasks;
    @Mock ReservationOverlapPort overlaps;
    @Mock EquipmentIncidentRepository incidents;
    private TechnicalWorkOrderService service;
    private TechnicalWorkOrder order;
    private Room room;

    @BeforeEach
    void setUp() {
        service = new TechnicalWorkOrderService(orders, rooms, equipment, audit, housekeepingTasks, overlaps, incidents,
                Clock.fixed(Instant.parse("2026-09-14T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
        room = new Room(); room.setId("101"); room.setStatus(RoomStatus.MAINTENANCE);
        order = new TechnicalWorkOrder(); ReflectionTestUtils.setField(order, "id", 5L); order.setRoom(room);
        order.setCreatedBy("technical"); order.setStatus(TechnicalWorkOrderStatus.WAITING_ACCEPTANCE);
        when(orders.findForUpdateById(5L)).thenReturn(Optional.of(order));
    }

    @Test
    void genericUpdateCannotSelfAccept() {
        DomainException error = assertThrows(DomainException.class, () -> service.update(5L,
                new TechnicalWorkOrderDtos.UpdateRequest("COMPLETED", "done", "self", null, null), "technical"));
        assertThat(error.getCode()).isEqualTo("TECHNICAL_ACCEPTANCE_COMMAND_REQUIRED");
        assertThat(order.getStatus()).isEqualTo(TechnicalWorkOrderStatus.WAITING_ACCEPTANCE);
    }

    @Test
    void managerAcceptanceRecordsIdentityBeforeRelease() {
        var accepted = service.accept(5L, new TechnicalWorkOrderDtos.AcceptanceRequest("Verified"), "manager");
        assertThat(accepted.status()).isEqualTo("COMPLETED");
        assertThat(accepted.acceptedBy()).isEqualTo("manager");
        assertThat(accepted.acceptedAt()).isNotNull();
    }

    @Test
    void releaseRequiresNoOverlapAndHousekeepingReadiness() {
        order.setStatus(TechnicalWorkOrderStatus.COMPLETED); order.setAcceptedBy("manager");
        when(rooms.findForUpdate("101")).thenReturn(Optional.of(room));
        when(overlaps.hasOverlap(eq("101"), any(), any())).thenReturn(false);
        HousekeepingTask housekeeping = new HousekeepingTask(); housekeeping.setChecklistComplete(true);
        housekeeping.setBlockingIncident(false);
        housekeeping.setStatus(HousekeepingTaskStatus.WAITING_TECHNICAL);
        when(housekeepingTasks.findFirstByRoomIdOrderByUpdatedAtDesc("101")).thenReturn(Optional.of(housekeeping));
        when(incidents.existsByRoomIdAndSeverityInAndHandoffStatusNot(eq("101"), anyList(), eq(IncidentHandoffStatus.RESOLVED)))
                .thenReturn(false);

        var released = service.release(5L, "technical");

        assertThat(released.status()).isEqualTo("ROOM_RELEASED");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.READY);
    }
}
