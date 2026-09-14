package com.hospitality.mis.operations;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.operations.*;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.operations.HousekeepingDtos;
import com.hospitality.mis.entity.operations.*;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.operations.HousekeepingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousekeepingReadinessTest {
    @Mock HousekeepingTaskRepository tasks;
    @Mock RoomRepository rooms;
    @Mock AuditService audit;
    @Mock HousekeepingChecklistTemplateRepository templates;
    @Mock HousekeepingChecklistResultRepository results;
    @Mock EquipmentIncidentRepository incidents;
    private HousekeepingService service;
    private HousekeepingTask task;
    private Room room;

    @BeforeEach
    void setUp() {
        service = new HousekeepingService(tasks, rooms, audit, templates, results, incidents,
                Clock.fixed(Instant.parse("2026-09-14T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
        room = new Room(); room.setId("101"); room.setStatus(RoomStatus.CLEANING);
        task = new HousekeepingTask(); ReflectionTestUtils.setField(task, "id", 7L); task.setRoom(room);
        task.setStatus(HousekeepingTaskStatus.CLEANED);
        when(tasks.findById(7L)).thenReturn(Optional.of(task));
        when(rooms.findForUpdate("101")).thenReturn(Optional.of(room));
    }

    @Test
    void clientChecklistFlagCannotBypassMissingResults() {
        var template = template("Bathroom");
        when(templates.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(template));
        when(results.findByTaskIdOrderByIdAsc(7L)).thenReturn(List.of());

        DomainException error = assertThrows(DomainException.class, () -> service.update(7L,
                new HousekeepingDtos.UpdateRequest("READY", true, false, null, null), "manager"));

        assertThat(error.getCode()).isEqualTo("HOUSEKEEPING_CHECKLIST_REQUIRED");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.CLEANING);
        verifyNoInteractions(audit);
    }

    @Test
    void readyRequiresActualPassedResultsAndNoMaintenanceLock() {
        when(templates.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(template("Bathroom")));
        when(results.findByTaskIdOrderByIdAsc(7L)).thenReturn(List.of(result("Bathroom", true)));
        when(incidents.existsByRoomIdAndSeverityInAndHandoffStatusNot(eq("101"), anyList(), eq(IncidentHandoffStatus.RESOLVED)))
                .thenReturn(false);
        room.setStatus(RoomStatus.MAINTENANCE);

        DomainException error = assertThrows(DomainException.class, () -> service.update(7L,
                new HousekeepingDtos.UpdateRequest("READY", null, null, null, null), "manager"));
        assertThat(error.getCode()).isEqualTo("ROOM_MAINTENANCE_LOCKED");
    }

    @Test
    void passedChecklistMovesRoomToReady() {
        when(templates.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(template("Bathroom")));
        when(results.findByTaskIdOrderByIdAsc(7L)).thenReturn(List.of(result("Bathroom", true)));
        when(incidents.existsByRoomIdAndSeverityInAndHandoffStatusNot(eq("101"), anyList(), eq(IncidentHandoffStatus.RESOLVED)))
                .thenReturn(false);

        var response = service.update(7L,
                new HousekeepingDtos.UpdateRequest("READY", null, null, null, null), "manager");

        assertThat(response.status()).isEqualTo("READY");
        assertThat(room.getStatus()).isEqualTo(RoomStatus.READY);
    }

    private HousekeepingChecklistTemplate template(String name) {
        var template = new HousekeepingChecklistTemplate(); template.setName(name); return template;
    }
    private HousekeepingChecklistResult result(String item, boolean passed) {
        var result = new HousekeepingChecklistResult(); result.setItem(item); result.setPassed(passed); return result;
    }
}
