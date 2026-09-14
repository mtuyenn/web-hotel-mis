package com.hospitality.mis.room;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dao.room.RoomTypeRepository;
import com.hospitality.mis.dto.room.RoomAdminDtos;
import com.hospitality.mis.entity.room.*;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.service.room.RoomAdminService;
import com.hospitality.mis.service.room.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAdminStateGuardTest {
    @Mock RoomRepository rooms;
    @Mock RoomTypeRepository types;
    @Mock AuditService audit;
    @Mock RoomService roomService;

    @Test
    void roomCannotBeCreatedDirectlyAsOccupied() {
        var service = new RoomAdminService(rooms, types, audit, roomService);
        when(rooms.existsById("101")).thenReturn(false);
        when(types.findById("STD")).thenReturn(Optional.of(activeType()));

        DomainException error = assertThrows(DomainException.class, () -> service.create(
                new RoomAdminDtos.Request("101", "Room 101", "STD", 1, null, RoomStatus.OCCUPIED), "admin"));

        assertThat(error.getCode()).isEqualTo("INVALID_INITIAL_ROOM_STATUS");
        verify(rooms, never()).save(any());
    }

    @Test
    void statusChangeIsDelegatedToGuardedRoomCommand() {
        var service = new RoomAdminService(rooms, types, audit, roomService);
        RoomType type = activeType(); Room room = new Room(); room.setId("101"); room.setName("Old");
        room.setRoomType(type); room.setStatus(RoomStatus.READY);
        when(rooms.findForUpdate("101")).thenReturn(Optional.of(room));
        when(types.findById("STD")).thenReturn(Optional.of(type));

        service.update("101", new RoomAdminDtos.Request("101", "Room 101", "STD", 1, null,
                RoomStatus.MAINTENANCE), "technical");

        verify(roomService).updateStatus("101", RoomStatus.MAINTENANCE, "technical");
    }

    private RoomType activeType() {
        RoomType type = new RoomType(); type.setId("STD"); type.setName("Standard");
        type.setCatalogStatus(RoomTypeCatalogStatus.ACTIVE); return type;
    }
}
