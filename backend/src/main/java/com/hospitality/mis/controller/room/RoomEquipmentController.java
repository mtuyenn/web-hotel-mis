package com.hospitality.mis.controller.room;

import com.hospitality.mis.dto.room.RoomEquipmentDtos;
import com.hospitality.mis.service.room.RoomEquipmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomId}/equipment")
public class RoomEquipmentController {
    private final RoomEquipmentService service;
    public RoomEquipmentController(RoomEquipmentService service) { this.service = service; }
    @GetMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'EQUIPMENT_READ')")
    public List<RoomEquipmentDtos.Response> list(@PathVariable String roomId) { return service.list(roomId); }
    @PostMapping 
    @PreAuthorize("@departmentAccess.allows(authentication, 'EQUIPMENT_WRITE')")
    public RoomEquipmentDtos.Response add(@PathVariable String roomId, @Valid @RequestBody RoomEquipmentDtos.CreateRequest request) {
        if (!roomId.equals(request.roomId())) {
            throw new com.hospitality.mis.common.exception.DomainException(
                    "ROOM_PATH_MISMATCH", "room_id trong URL và nội dung request phải giống nhau");
        }
        return service.add(request);
    }
}
